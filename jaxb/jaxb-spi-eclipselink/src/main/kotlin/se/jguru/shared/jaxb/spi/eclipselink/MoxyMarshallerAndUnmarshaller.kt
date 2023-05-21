/*-
 * #%L
 * Nazgul Project: jguru-shared-jaxb-spi-eclipselink
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
package se.jguru.shared.jaxb.spi.eclipselink

import org.eclipse.persistence.jaxb.MarshallerProperties
import org.eclipse.persistence.jaxb.UnmarshallerProperties
import se.jguru.shared.algorithms.api.introspection.Introspection
import se.jguru.shared.algorithms.api.xml.MarshallingFormat
import se.jguru.shared.jaxb.spi.shared.AbstractMarshallerAndUnmarshaller
import java.io.StringReader
import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.JAXBException
import javax.xml.transform.stream.StreamSource

/**
 * [AbstractMarshallerAndUnmarshaller] implementation using the EclipseLink Moxy implementation.
 *
 * @param typeInformation a [MutableList] containing classes which should be made available to the [JAXBContext]
 * synthesized for marshalling and unmarshalling operations.
 * @param jaxbContextProperties A Map relating property names (as Strings) to their respective values (as Objects).
 * Valid key/value combinations are implementation-specific; please refer to the actual implementation documentation.
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 * @see <a href="http://www.eclipse.org/eclipselink">EclipseLink</a>
 * @see <a href="http://www.eclipse.org/eclipselink/#moxy">Moxy</a>
 */
open class MoxyMarshallerAndUnmarshaller @JvmOverloads constructor(

    // Types added to the JAXBContext
    typeInformation: MutableList<Class<*>> = mutableListOf(),

    // Configuration properties submitted to the JAXBContext
    jaxbContextProperties: MutableMap<String, Any> = mutableMapOf()) : AbstractMarshallerAndUnmarshaller(
    typeInformation,
    listOf(MarshallingFormat.XML, MarshallingFormat.JSON),
    jaxbContextProperties) {

    override fun <T> performUnmarshalling(loader: ClassLoader,
                                          format: MarshallingFormat,
                                          resultType: Class<T>,
                                          toUnmarshal: String): T {

        // Create the unmarshaller
        val initialUnmarshaller = createUnmarshaller(getJaxbContext(setOf(resultType)))

        val unmarshaller = when (format) {
            MarshallingFormat.XML -> initialUnmarshaller
            MarshallingFormat.JSON -> {

                // Configure the emitted JSON
                initialUnmarshaller.setProperty(UnmarshallerProperties.JSON_WRAPPER_AS_ARRAY_NAME, true)
                // initialUnmarshaller.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, false)
                // initialUnmarshaller.setProperty(UnmarshallerProperties.JSON_USE_XSD_TYPES_WITH_PREFIX, false)
                initialUnmarshaller.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json")

                // All Done.
                initialUnmarshaller
            }
        }

        try {

            return unmarshaller.unmarshal(StreamSource(StringReader(toUnmarshal)), resultType).value

        } catch (e: JAXBException) {
            throw IllegalArgumentException("Could not unmarshal ${format.name} into [${resultType.name}]", e)
        }
    }

    override fun performMarshalling(loader: ClassLoader,
                                    format: MarshallingFormat,
                                    toMarshal: Array<Any>): String {

        // Find the type information, by shallow extraction
        val typesToMarshal = Introspection.getTypesFrom(toMarshal)

        // Get the Marshaller
        val initialMarshaller = createMarshaller(getJaxbContext(typesToMarshal))
        initialMarshaller.setProperty(MarshallerProperties.NAMESPACE_PREFIX_MAPPER, namespacePrefixResolver.toMap())

        // Decorate the Marshaller as required
        val marshaller = when (format) {
            MarshallingFormat.XML -> initialMarshaller
            MarshallingFormat.JSON -> {

                // Configure the emitted JSON
                initialMarshaller.setProperty(MarshallerProperties.JSON_WRAPPER_AS_ARRAY_NAME, true)
                initialMarshaller.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json")
                initialMarshaller.setProperty(MarshallerProperties.JSON_MARSHAL_EMPTY_COLLECTIONS, false)

                // All Done.
                initialMarshaller
            }
        }

        // All Done.
        return doMarshalling(marshaller, toMarshal)
    }

    /**
     * Retrieves the EclipseLink JAXBContext.
     */
    protected fun getJaxbContext(classes: Set<Class<*>>): JAXBContext {

        // Join with previously given types
        val allClasses = mutableListOf<Class<*>>()
        allClasses.addAll(typeInformation)
        classes.stream()
            .filter { it != null }
            .filter { !it.isArray }
            .filter { it != Object::class.java }
            .forEach { allClasses.add(it) }

        // Ensure that we actually use MOXy
        val origFactory : String? = System.getProperty(JAXBContext.JAXB_CONTEXT_FACTORY)
        setupSystemPropertiesForMOXy()

        try {

            // All Done
            return org.eclipse.persistence.jaxb.JAXBContext.newInstance(allClasses.toTypedArray(), jaxbContextProperties)
            
        } finally {

            // Restore the original JaxbContextFactory.
            when(origFactory == null) {
                true -> System.clearProperty(JAXBContext.JAXB_CONTEXT_FACTORY)
                else -> System.setProperty(JAXBContext.JAXB_CONTEXT_FACTORY, origFactory)
            }
        }
    }

    companion object {

        /**
         * The JAXBContextFactory implementation class exposed by MOXy.
         */
        @JvmStatic
        val MOXY_JAXB_FACTORY_CLASS = "org.eclipse.persistence.jaxb.JAXBContextFactory"

        /**
         * # Note
         * Note that using this method is not normally required - instead the [getJaxbContext] method
         * will return the correct type. Simply invoke getJaxbContext.
         *
         * ### This function
         * Convenience function which assigns the [JAXBContext.JAXB_CONTEXT_FACTORY] system property
         * to the value found in [MOXY_JAXB_FACTORY_CLASS].
         *
         * @return the (pre-)existing value for the JAXBContextFactory system property.
         */
        @JvmStatic
        fun setupSystemPropertiesForMOXy() : String? =
            System.setProperty(JAXBContext.JAXB_CONTEXT_FACTORY, MOXY_JAXB_FACTORY_CLASS)
    }
}
