package se.jguru.shared.entity.test

import com.sun.xml.bind.v2.ContextFactory
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
        return doMarshalling(toMarshal, marshaller)
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