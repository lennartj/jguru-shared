/*-
 * #%L
 * Nazgul Project: jguru-shared-jaxb-spi-metro
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
package se.jguru.shared.jaxb.spi.metro

import se.jguru.shared.algorithms.api.introspection.Introspection
import se.jguru.shared.algorithms.api.xml.MarshallingFormat
import se.jguru.shared.algorithms.api.xml.NamespacePrefixResolver
import se.jguru.shared.algorithms.api.xml.SimpleNamespacePrefixResolver
import se.jguru.shared.jaxb.spi.shared.AbstractMarshallerAndUnmarshaller
import java.io.StringReader
import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.JAXBException
import org.glassfish.jaxb.runtime.marshaller.NamespacePrefixMapper
import org.glassfish.jaxb.runtime.v2.ContextFactory
import javax.xml.transform.stream.StreamSource

/**
 * [AbstractMarshallerAndUnmarshaller] implementation using the Reference (Metro) implementation.
 *
 * @param typeInformation a [MutableList] containing classes which should be made available to the [JAXBContext]
 * synthesized for marshalling and unmarshalling operations.
 * @param jaxbContextProperties A Map relating property names (as Strings) to their respective values (as Objects).
 * Valid key/value combinations are implementation-specific; please refer to the actual implementation documentation.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 *
 * @see <a href="http://www.eclipse.org/eclipselink">EclipseLink</a>
 * @see <a href="http://www.eclipse.org/eclipselink/#moxy">Moxy</a>
 */
open class ReferenceImplementationMarshallerAndUnmarshaller @JvmOverloads constructor(
    typeInformation: MutableList<Class<*>> = mutableListOf()) : AbstractMarshallerAndUnmarshaller(
    typeInformation,
    listOf(MarshallingFormat.XML)) {

    override fun <T> performUnmarshalling(loader: ClassLoader,
                                          format: MarshallingFormat,
                                          resultType: Class<T>,
                                          toUnmarshal: String): T {

        // Create the unmarshaller
        val initialUnmarshaller = createUnmarshaller(getJaxbContext(setOf(resultType)))

        val unmarshaller = when (format) {
            MarshallingFormat.XML -> initialUnmarshaller
            MarshallingFormat.JSON -> throw IllegalArgumentException("Cannot handle JSON MarshallingFormat.")
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
        val typesToMarshal = Introspection.getTypesFrom(loader, toMarshal)

        // Get the Marshaller
        val initialMarshaller = createMarshaller(getJaxbContext(typesToMarshal))
        initialMarshaller.setProperty("org.glassfish.jaxb.namespacePrefixMapper",
            NamespacePrefixMapperWrapper(namespacePrefixResolver))

        // Decorate the Marshaller as required
        val marshaller = when (format) {
            MarshallingFormat.XML -> initialMarshaller
            else -> throw IllegalArgumentException("Cannot handle JSON MarshallingFormat.")
        }

        // All Done.
        return doMarshalling(marshaller, toMarshal)
    }

    /**
     * Retrieves the EclipseLink JAXBContext.
     */
    protected open fun getJaxbContext(classes: Set<Class<*>>): JAXBContext {

        // Join with previously given types
        val allClasses = mutableListOf<Class<*>>()
        allClasses.addAll(typeInformation)
        classes.stream()
            .filter { it != null }
            .filter { !it.isArray }
            .filter { it != Object::class.java }
            .forEach { allClasses.add(it) }

        // All Done
        return ContextFactory.createContext(allClasses.toTypedArray(), jaxbContextProperties)
    }

    companion object {

        /**
         * The [JAXBContextFactory] implementation class exposed by Metro.
         */
        @JvmStatic
        val METRO_JAXB_FACTORY_CLASS = "org.glassfish.jaxb.runtime.v2.ContextFactory"

        /**
         * # Note
         * Note that using this method is not normally required - instead the [getJaxbContext] method
         * will return the correct type. Simply invoke getJaxbContext.
         *
         * ### This function
         * Convenience function which assigns the [JAXBContext.JAXB_CONTEXT_FACTORY] system property
         * to the value found in [METRO_JAXB_FACTORY_CLASS].
         *
         * @return the (pre-)existing value for the JAXBContextFactory system property.
         */
        @JvmStatic
        fun setupSystemPropertiesForMetro(): String? =
            System.setProperty(JAXBContext.JAXB_CONTEXT_FACTORY, METRO_JAXB_FACTORY_CLASS)
    }
}

/**
 * [NamespacePrefixMapper] implementation which delegates all relevant calls to a [NamespacePrefixResolver].
 * The wrapping is required for the JAXB ReferenceImplementation, which requires a [NamespacePrefixMapper] instance
 * to relate XML Namespace URIs to their corresponding prefixes.
 *
 * @param resolver The [NamespacePrefixResolver] which will actually perform the NS resolving. Defaults to
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 *
 * @see [NamespacePrefixResolver]
 */
class NamespacePrefixMapperWrapper(val resolver: NamespacePrefixResolver = SimpleNamespacePrefixResolver())
    : NamespacePrefixMapper() {

    override fun getPreferredPrefix(namespaceUri: String?, suggestion: String?, requirePrefix: Boolean): String? =
        when (namespaceUri) {
            null -> null
            else -> resolver.getXmlPrefix(namespaceUri)
        }
}
