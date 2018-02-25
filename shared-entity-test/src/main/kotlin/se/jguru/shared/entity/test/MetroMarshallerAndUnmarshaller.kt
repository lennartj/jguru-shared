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

import com.sun.xml.bind.v2.ContextFactory
import org.junit.rules.TestWatcher
import se.jguru.shared.algorithms.api.introspection.Introspection
import java.io.StringReader
import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.transform.stream.StreamSource

/**
 * [AbstractMarshallerAndUnmarshaller] implementation using the standard ("Metro") implementation.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
open class MetroMarshallerAndUnmarshaller : AbstractMarshallerAndUnmarshaller(
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
                                    vararg toMarshal: Any): String {

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

/**
 * jUnit Rule for running JAXB tests under Kotlin.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class MetroMarshallerUnmarshallerRule(val delegate: MarshallerAndUnmarshaller) : TestWatcher() {

    // Internal state
}
