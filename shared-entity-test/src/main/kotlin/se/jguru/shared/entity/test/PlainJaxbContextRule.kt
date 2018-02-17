/*-
 * #%L
 * Nazgul Project: jguru-shared-entity-test
 * %%
 * Copyright (C) 2018 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * 
 *       http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package se.jguru.shared.entity.test

import org.junit.rules.TestWatcher
import org.slf4j.LoggerFactory
import org.w3c.dom.ls.LSResourceResolver
import org.xml.sax.SAXException
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.StringReader
import java.io.StringWriter
import java.util.Arrays
import java.util.Collections
import java.util.SortedMap
import java.util.SortedSet
import java.util.TreeMap
import java.util.TreeSet
import java.util.function.Predicate
import java.util.stream.Collectors
import javax.xml.XMLConstants
import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.bind.Marshaller
import javax.xml.bind.PropertyException
import javax.xml.bind.SchemaOutputResolver
import javax.xml.transform.Result
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory

/**
 *
 * jUnit rule simplifying working with JAXB marshalling and unmarshalling during tests.
 * Typically, this rule is invoked in 3 steps for marshalling and 2 steps for unmarshalling.
 * 
 * ## Marshalling Objects to XML Strings
 *
 *  1. Call `add(class1, class2, ...);` to add any classes that should be bound into the
 * JAXBContext for marshalling or unmarshalling.
 *  1. (Optional): Call `mapXmlNamespacePrefix(anXmlURI, marshalledXmlPrefix)`
 * to control the XML namespace prefix in the marshalled structure.
 *  1. Call `marshalToXML(classLoader, anObject)` to marshalToXML the objects into XML
 *
 * ## Unmarshalling Objects from XML Strings
 *
 *  1. Call `add(class1, class2, ...);` to add any classes that should be bound into the
 * JAXBContext for marshalling or unmarshalling.
 *  1. (Optional): Call `mapXmlNamespacePrefix(anXmlURI, marshalledXmlPrefix)`
 * to control the XML namespace prefix in the marshalled structure.
 *  1. Call `unmarshal(classLoader, ResultClass.class, xmlString);` to unmarshal the XML String into
 * Java Objects.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class PlainJaxbContextRule(val jaxbAnnotatedClasses: SortedSet<Class<*>> = ) : TestWatcher() {

    // Internal state
    private val classPatternsToIgnore: SortedSet<String>
    private var jaxbContext: JAXBContext? = null
    private val namespacePrefixResolver: JaxbNamespacePrefixResolver
    private var performXsdValidation = true
    private var useEclipseLinkMOXyIfAvailable = true

    /**
     * Retrieves the set of properties used within the Marshaller.
     *
     * @return the properties assigned to the Marshaller before use.
     */
    val marshallerProperties: SortedMap<String, Any>

    /**
     * Retrieves the set of properties used within the Unmarshaller.
     *
     * @return the properties assigned to the Unmarshaller before use.
     */
    val unMarshallerProperties: SortedMap<String, Any>

    private val ignoredClassFilter = when(aClass : Class<in Any>) {

        null -> false
        else ->

        // Don't accept classes whose names contain any of the classPatternsToIgnore.
        val className = aClass!!.getName()
        for (current in classPatternsToIgnore) {
            if (className.contains(current)) {
                return false
            }
        }

        // Accept the aClass.
        true
    }

    /**
     * Default constructor, setting up a clean internal state.
     */
    init {

        this.jaxbAnnotatedClasses = TreeSet(CLASS_COMPARATOR)
        this.namespacePrefixResolver = JaxbNamespacePrefixResolver()
        this.classPatternsToIgnore = TreeSet()
        this.classPatternsToIgnore.addAll(STD_IGNORED_CLASSPATTERNS)

        // Assign standard properties for the Marshaller
        marshallerProperties = TreeMap()
        marshallerProperties[Marshaller.JAXB_ENCODING] = "UTF-8"
        marshallerProperties[Marshaller.JAXB_FORMATTED_OUTPUT] = true
        marshallerProperties[RI_NAMESPACE_PREFIX_MAPPER_PROPERTY] = namespacePrefixResolver
        marshallerProperties[MarshallerProperties.JSON_INCLUDE_ROOT] = false
        marshallerProperties[MarshallerProperties.JSON_WRAPPER_AS_ARRAY_NAME] = true
        marshallerProperties[MarshallerProperties.JSON_MARSHAL_EMPTY_COLLECTIONS] = true

        // Assign standard properties for the Unmarshaller
        unMarshallerProperties = TreeMap()
        unMarshallerProperties[RI_NAMESPACE_PREFIX_MAPPER_PROPERTY] = namespacePrefixResolver
        unMarshallerProperties[UnmarshallerProperties.JSON_INCLUDE_ROOT] = false
        unMarshallerProperties[UnmarshallerProperties.JSON_WRAPPER_AS_ARRAY_NAME] = true
    }

    /**
     * If `false`, the JAXB reference implementation will be used for JAXB operations,
     * and otherwise the MOXy implementation from EclipseLink.
     *
     * @param useEclipseLinkMOXyIfAvailable if `false`, the JAXB reference implementation is used.
     * @see .useEclipseLinkMOXyIfAvailable
     */
    fun setUseEclipseLinkMOXyIfAvailable(useEclipseLinkMOXyIfAvailable: Boolean) {
        this.useEclipseLinkMOXyIfAvailable = useEclipseLinkMOXyIfAvailable
    }

    /**
     * Assigns the perform XSD validation flag. By default, the value of this flag is `true`, implying that
     * validation is always done before marshalling and after unmarshalling.
     *
     * @param performXsdValidation if `false`, XSD validation will not be performed before marshalling and
     * after unmarshalling data.
     */
    fun setPerformXsdValidation(performXsdValidation: Boolean) {
        this.performXsdValidation = performXsdValidation
    }

    /**
     *
     * Adds a set of classes to be used within the JAXBContext for marshalling or unmarshalling.
     * Normally, these classes would need to be annotated with JAXB annotations, and would be injected into the
     * construction of the JAXBContext normally:
     * <pre>
     * `
     * // Create an array of all added classes.
     * final Class[] classesToBeBound = ... all add-ed Classes ...
     *
     * // Create the JAXBContext containing/binding all added classes.
     * JAXBContext jaxbContext = JAXBContext.newInstance(classesToBeBound);
    ` *
    </pre> *
     *
     * @param jaxbAnnotatedClasses The classes to add to any JAXBContext used within this PlainJaxbContextRule, for
     * marshalling or unmarshalling. The supplied classes are given to the JAXBContext
     * during creation.
     */
    fun add(vararg jaxbAnnotatedClasses: Class<*>) {
        if (jaxbAnnotatedClasses != null) {
            Collections.addAll(this.jaxbAnnotatedClasses, *jaxbAnnotatedClasses)
        }
    }

    /**
     * Adds patterns for classes to ignore in creating a JAXBContext.
     *
     * @param clearExistingPatterns if `true`, any existing patterns are cleared before adding the supplied
     * ignore patterns. Typically something like `org.aspectj`.
     * @param ignorePattern         A set of patterns to ignore if present within JAXContext classes.
     */
    fun addIgnoreClassPatterns(clearExistingPatterns: Boolean, vararg ignorePattern: String) {

        // Handle clearing existing patterns
        if (clearExistingPatterns) {
            classPatternsToIgnore.clear()
        }

        // ... and add the supplied ones.
        if (ignorePattern != null && ignorePattern.size > 0) {
            Collections.addAll(classPatternsToIgnore, *ignorePattern)
        }
    }

    /**
     * Adds the supplied patterns for classes to ignore in creating a JAXBContext, without clearing any existing
     * patterns.
     *
     * @param ignorePattern A set of patterns to ignore if present within JAXContext classes.
     * @see .addIgnoreClassPatterns
     */
    fun addIgnoreClassPatterns(vararg ignorePattern: String) {
        addIgnoreClassPatterns(false, *ignorePattern)
    }

    /**
     * Marshals the supplied objects into an XML String, or throws an IllegalArgumentException
     * containing a wrapped JAXBException indicating why the marshalling was unsuccessful.
     *
     * @param loader   The ClassLoader to use in order to load all classes previously added
     * by calls to the `add` method.
     * @param emitJSON if `true`, the method will attempt to output JSON instead of XML.
     * This requires the EclipseLink MOXy implementation as the JAXBContextFactory.
     * @param objects  The objects to Marshal into XML.
     * @return An XML-formatted String containing
     * @throws IllegalArgumentException if the marshalling operation failed.
     * The `cause` field in the IllegalArgumentException contains
     * the JAXBException thrown by the JAXB framework.
     * @see .add
     */
    @Throws(IllegalArgumentException::class)
    fun marshal(loader: ClassLoader,
                emitJSON: Boolean,
                vararg objects: Any): String {

        // Create an EntityTransporter, to extract the types as required by the plain JAXBContext.
        val transporter = EntityTransporter()
        for (current in objects) {
            transporter.addItem(current)
        }

        // Use EclipseLink?
        if (emitJSON) {
            setUseEclipseLinkMOXyIfAvailable(true)
        }
        if (useEclipseLinkMOXyIfAvailable) {
            System.setProperty(JAXB_CONTEXTFACTORY_PROPERTY, ECLIPSELINK_JAXB_CONTEXT_FACTORY)
        } else {
            System.clearProperty(JAXB_CONTEXTFACTORY_PROPERTY)
        }

        // Extract class info as required by the JAXBContext.
        val clsInfo = transporter.getClassInformation()
        try {
            jaxbContext = JAXBContext.newInstance(getClasses<SortedSet<String>>(loader, clsInfo), marshallerProperties)

            log.info("Got JAXBContext of type " + jaxbContext!!.javaClass.name + ", with classes")

        } catch (e: JAXBException) {
            throw IllegalArgumentException("Could not create JAXB context.", e)
        }

        // Handle the namespace mapper
        handleNamespacePrefixMapper()

        var marshaller: Marshaller? = null
        try {
            marshaller = jaxbContext!!.createMarshaller()

            // Should we validate what we write?
            if (performXsdValidation) {

                if ("org.eclipse.persistence.jaxb.JAXBContext" == jaxbContext!!.javaClass.name) {

                    // Cast to the appropriate JAXBContext
                    val eclipseLinkJaxbContext = jaxbContext as org.eclipse.persistence.jaxb.JAXBContext?
                    if (emitJSON) {

                        val simpleResolver = SimpleSchemaOutputResolver()
                        Arrays.stream(objects)
                            .filter { c -> c != null }
                            .forEach { c ->

                                val currentClass = c.javaClass

                                if (log.isDebugEnabled) {
                                    log.debug("Generating JSON schema for " + currentClass.name)
                                }
                                try {
                                    eclipseLinkJaxbContext.generateJsonSchema(simpleResolver, currentClass)
                                } catch (e: Exception) {
                                    log.error("Could not generate JSON schema", e)
                                }
                            }
                    } else {
                        val schema2LSResolver = generateTransientXSD(jaxbContext)
                        marshaller!!.schema = schema2LSResolver.getKey()
                    }
                }
            }

        } catch (e: Exception) {

            try {
                marshaller = jaxbContext!!.createMarshaller()
            } catch (e1: JAXBException) {

                throw IllegalStateException("Could not create non-validating JAXB Marshaller", e)
            }

        }

        // Should we emit JSON instead of XML?
        if (emitJSON) {
            try {
                marshaller!!.setProperty(ECLIPSELINK_MEDIA_TYPE, JSON_CONTENT_TYPE)
                marshaller.setProperty(ECLIPSELINK_JSON_MARSHAL_EMPTY_COLLECTIONS, java.lang.Boolean.FALSE)
            } catch (e: PropertyException) {

                // This is likely not the EclipseLink Marshaller.
                log.error("Could not assign EclipseLink properties to Marshaller of type "
                    + marshaller!!.javaClass.name + "]. Proceeding, but results may be unexpected.",
                    e)
            }

        }

        // Assign all other Marshaller properties.
        try {
            for ((key, value) in marshallerProperties) {
                marshaller!!.setProperty(key, value)
            }
        } catch (e: PropertyException) {
            val builder = StringBuilder("Could not assign Marshaller properties.")
            marshallerProperties.entries.stream().forEach { c ->
                builder.append("\n  ["
                    + c.key + "]: " + c.value)
            }

            throw IllegalStateException(builder.toString(), e)
        }

        // Marshal the objects
        val result = StringWriter()
        for (i in objects.indices) {
            val tmp = StringWriter()
            try {
                marshaller!!.marshal(objects[i], tmp)
                result.write(tmp.toString())
            } catch (e: JAXBException) {
                val currentTypeName = if (objects[i] == null) "<null>" else objects[i].javaClass.name
                throw IllegalArgumentException("Could not marshalToXML object [" + i + "] of type ["
                    + currentTypeName + "].", e)
            } catch (e: Exception) {
                throw IllegalArgumentException("Could not marshalToXML object [" + i + "]: " + objects[i], e)
            }

        }

        // All done.
        return result.toString()
    }

    /**
     *
     * Unmarshals the supplied xmlToUnmarshal into a result of the supplied type, using the given
     * ClassLoader to load all relevant types into the JAXBContext. Typical unarshalling use case involves
     * 2 calls on this PlainJaxbContextRule:
     * <pre>
     * `
     * // 1) add all types you expect to unmarshal
     * rule.add(Foo.class, Bar.class, Gnat.class);
     *
     * // 2) unmarshal your XML string
     * Foo unmarshalled = rule.unmarshal(getClass().getClassLoader(), Foo.class, aFooXml);
    ` *
    </pre> *
     *
     * @param loader          The ClassLoader to use in order to load all classes previously added
     * by calls to the `add` method.
     * @param assumeJSonInput If `true`, the input is assumed to be JSON.
     * This requires the EclipseLink MOXy JAXBContextFactory to succeed.
     * @param resultType      The type of the resulting object.
     * @param toUnmarshal     The XML string to unmarshal into a T object.
     * @param <T>             The expected type to unmarshal into.
     * @return The resulting, unmarshalled object.
     * @see .add
    </T> */
    fun <T> unmarshal(loader: ClassLoader,
                      assumeJSonInput: Boolean,
                      resultType: Class<T>,
                      toUnmarshal: String): T {

        // Check sanity
        org.apache.commons.lang3.Validate.notNull(resultType, "Cannot handle null 'resultType' argument.")
        org.apache.commons.lang3.Validate.notEmpty(toUnmarshal, "Cannot handle null or empty 'xmlToUnmarshal' argument.")

        val source = StreamSource(StringReader(toUnmarshal))

        // Use EclipseLink?
        if (assumeJSonInput || useEclipseLinkMOXyIfAvailable) {
            System.setProperty(JAXB_CONTEXTFACTORY_PROPERTY, ECLIPSELINK_JAXB_CONTEXT_FACTORY)
        } else {
            System.clearProperty(JAXB_CONTEXTFACTORY_PROPERTY)
        }

        try {
            jaxbContext = JAXBContext.newInstance(getClasses<Collection<String>>(loader, null), unMarshallerProperties)

            handleNamespacePrefixMapper()

        } catch (e: JAXBException) {
            throw IllegalArgumentException("Could not create JAXB context.", e)
        }

        try {
            val unmarshaller = jaxbContext!!.createUnmarshaller()

            // Assign all unMarshallerProperties to the Unmarshaller
            unMarshallerProperties.entries.forEach { c ->
                try {
                    unmarshaller.setProperty(c.key, c.value)
                } catch (e: PropertyException) {
                    throw IllegalStateException("Could not assign Unmarshaller property [" + c.key
                        + "] with value [" + c.value + "]", e)
                }
            }

            if (assumeJSonInput) {
                try {
                    unmarshaller.setProperty(ECLIPSELINK_MEDIA_TYPE, JSON_CONTENT_TYPE)
                    // unmarshaller.setProperty(ECLIPSELINK_JSON_MARSHAL_EMPTY_COLLECTIONS, Boolean.FALSE);
                } catch (e: PropertyException) {
                    // This is likely not the EclipseLink Marshaller.
                }

            }

            // All Done.
            return unmarshaller.unmarshal(source, resultType).value
        } catch (e: JAXBException) {
            val dataType = if (assumeJSonInput) "json" else "xml"
            throw IllegalArgumentException("Could not unmarshal " + dataType + " into ["
                + resultType.name + "]", e)
        }

    }

    /**
     * Unmarshals without type information resulting in an Object, when no resulting type information
     * has been (or can be) given.
     *
     * @param loader          The ClassLoader to use in order to load all classes previously added
     * by calls to the `add` method.
     * @param assumeJSonInput If `true`, assume that the input to the unmarshaller is provided in JSON - rather
     * than XML - form.
     * @param xmlToUnmarshal  The XML string to unmarshal into a T object.
     * @return The resulting, unmarshalled object.
     * @see .unmarshal
     */
    fun unmarshal(loader: ClassLoader, assumeJSonInput: Boolean, xmlToUnmarshal: String): Any {
        return unmarshal(loader, assumeJSonInput, Any::class.java, xmlToUnmarshal)
    }

    /**
     * Maps an XML URI to a given XML namespace prefix, to yield a better/more user-friendly
     * marshalling of an XML structure.
     *
     * @param uri       The XML URI to map, such as "http://jguru.se/some/url" or "urn:mithlond:data".
     * @param xmlPrefix The XML prefix to use when marshalling types using the uri for namespace.
     */
    fun mapXmlNamespacePrefix(uri: String, xmlPrefix: String) {
        this.namespacePrefixResolver.put(uri, xmlPrefix)
    }

    //
    // Private helpers
    //

    private fun <C : Collection<String>> getClasses(loader: ClassLoader?, input: C?): Array<Class<*>> {

        val effectiveClassLoader = loader ?: PlainJaxbContextRule::class.java.classLoader
        val name2ClassMap = TreeMap<String, Class<*>>()

        if (input != null) {
            for (current in input) {
                try {
                    val aClass = effectiveClassLoader.loadClass(current)
                    if (aClass != null) {
                        name2ClassMap[aClass.name] = aClass
                    }
                } catch (e: ClassNotFoundException) {
                    throw IllegalArgumentException("Could not load class for [$current]", e)
                }

            }
        }

        // Add any explicitly added classes to the JAXBContext
        for (current in this.jaxbAnnotatedClasses) {
            name2ClassMap[current.name] = current
        }

        // Remove any ignored classes.
        val classList = name2ClassMap
            .values
            .stream()
            .filter(ignoredClassFilter)
            .collect<List<Any>, *>(Collectors.toList())

        // All done.
        return classList.toTypedArray<Class<*>>()
    }

    /**
     * Simple [SchemaOutputResolver] implementation intended for JSON Schema generation using EclipseLink's
     * JAXBContext implementation ("Moxy").
     */
    class SimpleSchemaOutputResolver : SchemaOutputResolver() {

        // Internal state
        private val stringWriter = StringWriter()

        /**
         * Retrieves the Schema source in String form.
         *
         * @return the Schema source in String form.
         */
        val schema: String
            get() = stringWriter.toString()

        /**
         * {@inheritDoc}
         */
        @Throws(IOException::class)
        override fun createOutput(namespaceURI: String, suggestedFileName: String): Result {

            // Delegate to a StreamResult.
            val result = StreamResult(stringWriter)
            result.systemId = suggestedFileName
            return result
        }
    }

    private fun handleNamespacePrefixMapper() {

        if (jaxbContext is org.eclipse.persistence.jaxb.JAXBContext) {

            // Create an EclipseLink-compliant NamespacePrefix mapper.
            val uri2PrefixMap = TreeMap<String, String>()
            val eclipseLinkMapper = MapNamespacePrefixMapper(uri2PrefixMap)

            // Copy each URI to Prefix entry.
            namespacePrefixResolver.getRegisteredNamespaceURIs().forEach { c -> uri2PrefixMap.put(c, namespacePrefixResolver.getXmlPrefix(c)) }

            // Replace the RI namespace mapping properties with the EclipseLink equivalents.
            marshallerProperties.remove(RI_NAMESPACE_PREFIX_MAPPER_PROPERTY)
            marshallerProperties[ECLIPSELINK_NAMESPACE_PREFIX_MAPPER_PROPERTY] = eclipseLinkMapper

            unMarshallerProperties.remove(RI_NAMESPACE_PREFIX_MAPPER_PROPERTY)
            unMarshallerProperties[ECLIPSELINK_NAMESPACE_PREFIX_MAPPER_PROPERTY] = eclipseLinkMapper
        }
    }

    companion object {

        // Our Log
        private val log = LoggerFactory.getLogger(PlainJaxbContextRule::class.java)

        private val CLASS_COMPARATOR = { class1, class2 ->

            // Deal with nulls.
            val className1 = if (class1 == null) "" else class1!!.getName()
            val className2 = if (class2 == null) "" else class2!!.getName()

            // All done
            className1.compareTo(className2)
        }

        /**
         * The JAXBContextFactory implementation within EclipseLink (i.e. the MOXy implementation).
         */
        val ECLIPSELINK_JAXB_CONTEXT_FACTORY = "org.eclipse.persistence.jaxb.JAXBContextFactory"
        private val JAXB_CONTEXTFACTORY_PROPERTY = "javax.xml.bind.context.factory"
        private val JSON_CONTENT_TYPE = "application/json"
        private val ECLIPSELINK_MEDIA_TYPE = "eclipselink.media-type"
        private val ECLIPSELINK_JSON_MARSHAL_EMPTY_COLLECTIONS = "eclipselink.json.marshal-empty-collections"
        private val STD_IGNORED_CLASSPATTERNS: SortedSet<String>
        private val RI_NAMESPACE_PREFIX_MAPPER_PROPERTY = "com.sun.xml.bind.namespacePrefixMapper"
        private val ECLIPSELINK_NAMESPACE_PREFIX_MAPPER_PROPERTY = JAXBContextProperties.NAMESPACE_PREFIX_MAPPER

        init {
            STD_IGNORED_CLASSPATTERNS = TreeSet()
            STD_IGNORED_CLASSPATTERNS.add("org.aspectj")
            STD_IGNORED_CLASSPATTERNS.add("ch.")
            STD_IGNORED_CLASSPATTERNS.add("org.slf4j")
        }

        /**
         * Acquires a JAXB Schema from the provided JAXBContext.
         *
         * @param ctx The context for which am XSD should be constructed.
         * @return A tuple holding the constructed XSD from the provided JAXBContext, and
         * the LSResourceResolver synthesized during the way.
         * @throws NullPointerException     if ctx was `null`.
         * @throws IllegalArgumentException if a JAXB-related exception occurred while extracting the schema.
         */
        @Throws(NullPointerException::class, IllegalArgumentException::class)
        fun generateTransientXSD(ctx: JAXBContext): Tuple<Schema, LSResourceResolver> {

            // Check sanity
            Validate.notNull(ctx, "Cannot handle null ctx argument.")

            val namespace2SchemaMap = TreeMap<String, ByteArrayOutputStream>()

            try {
                ctx.generateSchema(object : SchemaOutputResolver() {

                    /**
                     * {@inheritDoc}
                     */
                    @Throws(IOException::class)
                    override fun createOutput(namespaceUri: String, suggestedFileName: String): Result {

                        // The types should really be annotated with @XmlType(namespace = "... something ...")
                        // to avoid using the default ("") namespace.
                        if (namespaceUri.isEmpty()) {
                            log.warn("Received empty namespaceUri while resolving a generated schema. "
                                + "Did you forget to add a @XmlType(namespace = \"... something ...\") annotation "
                                + "to your class?")
                        }

                        // Create the result ByteArrayOutputStream
                        val out = ByteArrayOutputStream()
                        val toReturn = StreamResult(out)
                        toReturn.systemId = ""

                        // Map the namespaceUri to the schemaResult.
                        namespace2SchemaMap[namespaceUri] = out

                        // All done.
                        return toReturn
                    }
                })
            } catch (e: IOException) {
                throw IllegalArgumentException("Could not acquire Schema snippets.", e)
            }

            // Convert to an array of StreamSource.
            val resourceResolver = MappedSchemaResourceResolver()
            val schemaSources = arrayOfNulls<StreamSource>(namespace2SchemaMap.size)
            var counter = 0
            for ((key, value) in namespace2SchemaMap) {

                val schemaSnippetAsBytes = value.toByteArray()
                resourceResolver.addNamespace2SchemaEntry(key, String(schemaSnippetAsBytes))

                if (log.isDebugEnabled) {
                    log.info("Generated schema [" + (counter + 1) + "/" + schemaSources.size + "]:\n "
                        + String(schemaSnippetAsBytes))
                }

                // Copy the schema source to the schemaSources array.
                schemaSources[counter] = StreamSource(ByteArrayInputStream(schemaSnippetAsBytes), "")

                // Increase the counter
                counter++
            }

            try {

                // All done.
                val schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                schemaFactory.resourceResolver = resourceResolver
                val transientSchema = schemaFactory.newSchema(schemaSources)

                // All done.
                return Tuple<Schema, MappedSchemaResourceResolver>(transientSchema, resourceResolver)

            } catch (e: SAXException) {
                throw IllegalArgumentException("Could not create Schema from snippets.", e)
            }

        }
    }
}
