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

import se.jguru.shared.algorithms.api.introspection.Introspection
import java.io.IOException
import java.io.Serializable
import java.io.StringWriter
import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.bind.Marshaller
import javax.xml.bind.SchemaOutputResolver
import javax.xml.bind.Unmarshaller
import javax.xml.transform.Result
import javax.xml.transform.stream.StreamResult

/**
 * Definition for frequently used formats for JAXB Marshalling and Unmarshalling.
 */
enum class MarshallingFormat {

    /**
     * XML-formatted string results.
     */
    XML,

    /**
     * JSON-formatted string results.
     */
    JSON
}

/**
 * Specification for how to execute marshalling and unmarshalling operations.
 * While this is normally done using JAXB, the [MarshallerAndUnmarshaller] can be
 * implemented using several different technologies.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
interface MarshallerAndUnmarshaller : Serializable {

    /**
     * Specification for how to marshal (a set of) objects, to the [MarshallingFormat] indicated.
     *
     * @param loader The [ClassLoader] used to harvest types as required for the marshalling.
     * @param format The desired output [MarshallingFormat]
     * @param toMarshal The object(s) to marshal.
     * @return The marshalled transport form of the supplied [toMarshal] objects.
     */
    @Throws(IllegalArgumentException::class)
    fun marshal(loader: ClassLoader = Thread.currentThread().contextClassLoader,
                format: MarshallingFormat = MarshallingFormat.XML,
                vararg toMarshal: Any): String

    /**
     * Specification for how to unmarshal a previously marshalled (set of) objects.
     *
     * @param loader The [ClassLoader] used to harvest types as required for the unmarshalling.
     * @param format The expected input [MarshallingFormat]
     * @param resultType The type of object which should be resurrected from the supplied [toUnmarshal] string
     * @return The fully unmarshalled object.
     */
    @Throws(IllegalArgumentException::class)
    fun <T> unmarshal(loader: ClassLoader = Thread.currentThread().contextClassLoader,
                      format: MarshallingFormat = MarshallingFormat.XML,
                      resultType: Class<T>,
                      toUnmarshal: String): T

    /**
     * Adds the supplied type information classes to this [MarshallerAndUnmarshaller] in order to perform marshalling
     * or unmarshalling operations. Typically, classes required within the [JAXBContext] should be added here.
     *
     * @param typeInformation a (set of) Classes required for proper operation.
     */
    fun add(vararg typeInformation: Class<in Any>)
}

/**
 * Abstract [MarshallerAndUnmarshaller] implementation sporting some sanity checking WRT expected [MarshallingFormat]
 */
abstract class AbstractMarshallerAndUnmarshaller(
    val typeInformation: MutableList<Class<*>> = mutableListOf(),
    val supportedFormats: List<MarshallingFormat> = listOf(MarshallingFormat.XML),
    val jaxbContextProperties: MutableMap<String, Any> = mutableMapOf()) : MarshallerAndUnmarshaller {

    override fun add(vararg typeInformation: Class<in Any>) {
        typeInformation.forEach { this.typeInformation.add(it) }
    }

    override fun marshal(loader: ClassLoader, format: MarshallingFormat, vararg toMarshal: Any): String {

        // Check sanity
        if (!supportedFormats.contains(format)) {
            throw IllegalArgumentException("Unsupported format $format. Supported formats are: " +
                supportedFormats.map { it.name }.sorted().reduce { l, r -> l + ", " + r })
        }
        if (toMarshal.isEmpty()) {
            throw IllegalArgumentException("Stubbornly refusing to marshal no Objects.")
        }

        // Delegate
        return performMarshalling(loader, format, toMarshal)
    }

    override fun <T> unmarshal(loader: ClassLoader,
                               format: MarshallingFormat,
                               resultType: Class<T>,
                               toUnmarshal: String): T {

        // Check sanity
        if (!supportedFormats.contains(format)) {
            throw IllegalArgumentException("Unsupported format $format. Supported formats are: " +
                supportedFormats.map { it.name }.sorted().reduce { l, r -> l + ", " + r })
        }
        if (toUnmarshal.isEmpty() || toUnmarshal.isBlank()) {
            throw IllegalArgumentException("Stubbornly refusing to unmarshal empty/blank String.")
        }

        // Delegate
        return performUnmarshalling(loader, format, resultType, toUnmarshal)
    }


    /**
     * Implement this method to perform actual unmarshalling using the underlying technology.
     */
    protected abstract fun <T> performUnmarshalling(loader: ClassLoader = Thread.currentThread().contextClassLoader,
                                                    format: MarshallingFormat = MarshallingFormat.XML,
                                                    resultType: Class<T>,
                                                    toUnmarshal: String): T

    /**
     * Implement this method to perform actual marshalling using the underlying technology.
     */
    protected abstract fun performMarshalling(loader: ClassLoader = Thread.currentThread().contextClassLoader,
                                              format: MarshallingFormat = MarshallingFormat.XML,
                                              vararg toMarshal: Any): String

    /**
     * Convenience method to create a Marshaller from the supplied JAXBContext, and set 2 standard
     * properties within the returned Marshaller.
     *
     * toReturn.setProperty(Marshaller.JAXB_ENCODING, "UTF-8")
     * toReturn.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
     * @param jaxbContext The [JAXBContext] from which a Marshaller should be retrieved.
     * @return The created and configured [Marshaller]
     */
    protected fun createMarshaller(jaxbContext: JAXBContext): Marshaller {

        val toReturn = jaxbContext.createMarshaller()
        toReturn.setProperty(Marshaller.JAXB_ENCODING, "UTF-8")
        toReturn.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)

        // All Done.
        return toReturn
    }

    protected fun createUnmarshaller(jaxbContext: JAXBContext) : Unmarshaller {

        val toReturn = jaxbContext.createUnmarshaller()
        toReturn.setProperty(Marshaller.JAXB_ENCODING, "UTF-8")

        // All Done.
        return toReturn
    }

    protected fun doMarshalling(toMarshal: Array<out Any>, marshaller: Marshaller): String {

        // Marshal the inbound objects
        val result = StringWriter()
        for (i in toMarshal.indices) {

            // Handle the Marshalled output of this object.
            val tmp = StringWriter()

            try {
                marshaller.marshal(toMarshal[i], tmp)
                result.write(tmp.toString())

            } catch (e: JAXBException) {

                val currentTypeName = toMarshal[i].javaClass.name
                throw IllegalArgumentException("Could not marshalToXML object [" + i
                    + "] of type [" + currentTypeName + "].", e)

            } catch (e: Exception) {
                throw IllegalArgumentException("Could not marshalToXML object [" + i + "]: " + toMarshal[i], e)
            }
        }

        // All Done
        return result.toString()
    }
}

/**
 * Simple [SchemaOutputResolver] implementation intended mainly for JSON Schema
 * generation using [JAXBContext] implementation ("Moxy").
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
