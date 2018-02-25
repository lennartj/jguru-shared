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

import org.eclipse.persistence.jaxb.MarshallerProperties
import org.eclipse.persistence.jaxb.UnmarshallerProperties
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import se.jguru.shared.algorithms.api.introspection.Introspection
import java.io.StringReader
import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.transform.stream.StreamSource

/**
 * [AbstractMarshallerAndUnmarshaller] implementation using the EclipseLink implementation.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
open class EclipseLinkMarshallerAndUnmarshaller : AbstractMarshallerAndUnmarshaller(
    mutableListOf(),
    listOf(MarshallingFormat.XML, MarshallingFormat.JSON)) {

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
                                    vararg toMarshal: Any): String {

        // Find the type information, by shallow extraction
        val typesToMarshal = Introspection.getTypesFrom(loader, toMarshal)

        // Get the Marshaller
        val initialMarshaller = createMarshaller(getJaxbContext(typesToMarshal))

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
        allClasses.addAll(classes)

        // All Done
        return org.eclipse.persistence.jaxb.JAXBContext.newInstance(allClasses.toTypedArray(), jaxbContextProperties)
    }
}

/**
 * jUnit Rule for running JAXB tests under Kotlin.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class EclipseLinkMarshallerUnmarshallerRule(val delegate: MarshallerAndUnmarshaller) : TestWatcher() {

    // Internal state

    override fun starting(description: Description?) {
        System.setProperty(JAXBContext.JAXB_CONTEXT_FACTORY, "org.eclipse.persistence.jaxb.JAXBContextFactory")
        super.starting(description)
    }

    override fun finished(description: Description?) {
        super.finished(description)
    }

    override fun failed(e: Throwable?, description: Description?) {
        super.failed(e, description)
    }
}
