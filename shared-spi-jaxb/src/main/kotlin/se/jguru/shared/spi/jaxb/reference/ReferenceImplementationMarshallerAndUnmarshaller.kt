/*-
 * #%L
 * Nazgul Project: jguru-shared-spi-jaxb
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
package se.jguru.shared.spi.jaxb.reference

import com.sun.xml.internal.bind.v2.ContextFactory
import se.jguru.shared.algorithms.api.introspection.Introspection
import se.jguru.shared.algorithms.api.xml.AbstractMarshallerAndUnmarshaller
import se.jguru.shared.algorithms.api.xml.MarshallingFormat
import java.io.StringReader
import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.transform.stream.StreamSource

/**
 *[AbstractMarshallerAndUnmarshaller] implementation using the EclipseLink Moxy implementation.
 *
 * @param typeInformation a [MutableList] containing classes which should be made available to the [JAXBContext]
 * synthesized for marshalling and unmarshalling operations.
 * @param jaxbContextProperties A Map relating property names (as Strings) to their respective values (as Objects).
 * Valid key/value combinations are implementation-specific; please refer to the actual implementation documentation.
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 * @see <a href="http://www.eclipse.org/eclipselink">EclipseLink</a>
 * @see <a href="http://www.eclipse.org/eclipselink/#moxy">Moxy</a>
 */
open class ReferenceImplementationMarshallerAndUnmarshaller : AbstractMarshallerAndUnmarshaller(
    mutableListOf(),
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
    protected fun getJaxbContext(classes: Set<Class<*>>): JAXBContext {

        // Join with previously given types
        val allClasses = mutableListOf<Class<*>>()
        allClasses.addAll(typeInformation)
        allClasses.addAll(classes)

        // All Done
        return ContextFactory.createContext(allClasses.toTypedArray(), jaxbContextProperties)
    }
}
