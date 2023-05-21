/*-
 * #%L
 * Nazgul Project: jguru-shared-jaxb-spi-shared
 * %%
 * Copyright (C) 2018 - 2019 jGuru Europe AB
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
package se.jguru.shared.jaxb.spi.shared

import se.jguru.shared.algorithms.api.xml.MarshallerAndUnmarshaller
import se.jguru.shared.algorithms.api.xml.MarshallingFormat
import se.jguru.shared.algorithms.api.xml.NamespacePrefixResolver
import se.jguru.shared.algorithms.api.xml.SimpleNamespacePrefixResolver
import java.io.IOException
import java.io.StringWriter
import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.JAXBException
import jakarta.xml.bind.Marshaller
import jakarta.xml.bind.SchemaOutputResolver
import jakarta.xml.bind.Unmarshaller
import javax.xml.transform.Result
import javax.xml.transform.stream.StreamResult

/**
 * Abstract [MarshallerAndUnmarshaller] implementation sporting some sanity checking WRT expected [MarshallingFormat]
 *
 * @param typeInformation a [MutableList] containing classes which should be made available to
 * the [JAXBContext] synthesized for marshalling and unmarshalling operations.
 * @param supportedFormats A List containing all [MarshallingFormat]s supported
 * by this [AbstractMarshallerAndUnmarshaller]
 * @param jaxbContextProperties A Map relating property names (as Strings) to their respective
 * values (as Objects). Valid key/value combinations are implementation-specific; please refer to the actual
 * implementation documentation.
 * @param namespacePrefixResolver The [NamespacePrefixResolver] used to map XML namespaces to Prefix strings.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
abstract class AbstractMarshallerAndUnmarshaller @JvmOverloads constructor(

    // Types added to the JAXBContext
    val typeInformation: MutableList<Class<*>> = mutableListOf(),

    // Formats supported by this AbstractMarshallerAndUnmarshaller
    val supportedFormats: List<MarshallingFormat> = listOf(MarshallingFormat.XML),

    // Configuration properties submitted to the JAXBContext
    val jaxbContextProperties: MutableMap<String, Any> = mutableMapOf(),

    // The NamespacePrefixResolver used to map URIs to Prefixes
    val namespacePrefixResolver: NamespacePrefixResolver = SimpleNamespacePrefixResolver()

) : MarshallerAndUnmarshaller() {

    override fun add(vararg typeInformation: Class<in Any>) {
        typeInformation.forEach { this.typeInformation.add(it) }
    }

    override fun marshal(loader: ClassLoader, format: MarshallingFormat, toMarshal: Array<Any>): String {

        // Check sanity
        if (!supportedFormats.contains(format)) {
            throw IllegalArgumentException("Unsupported format $format. Supported formats are: " +
                supportedFormats.map { it.name }.sorted().reduce { l, r -> "$l, $r" })
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
                supportedFormats.map { it.name }.sorted().reduce { l, r -> "$l, $r" })
        }
        if (toUnmarshal.isEmpty() || toUnmarshal.isBlank()) {
            throw IllegalArgumentException("Stubbornly refusing to unmarshal empty/blank String.")
        }

        // Delegate
        return performUnmarshalling(loader, format, resultType, toUnmarshal)
    }

    /**
     * Implement this method to perform unmarshalling using the actual implementation.
     *
     * @param loader The [ClassLoader] used to load classes required to unmarshal the supplied [toUnmarshal] String.
     * Defaults to `Thread.currentThread().contextClassLoader`
     * @param format The [MarshallingFormat] to unmarshal to. Defaults to [MarshallingFormat.XML].
     * @param resultType The type of result expected
     * @param toUnmarshal The string to unmarshal into an object of type T.
     */
    protected abstract fun <T> performUnmarshalling(loader: ClassLoader = Thread.currentThread().contextClassLoader,
                                                    format: MarshallingFormat = MarshallingFormat.XML,
                                                    resultType: Class<T>,
                                                    toUnmarshal: String): T

    /**
     * Implement this method to perform marshalling using the actual implementation.
     *
     * @param loader The [ClassLoader] used to load classes required to marshal the supplied [toMarshal] Objects.
     * Defaults to `Thread.currentThread().contextClassLoader`
     * @param format The [MarshallingFormat] to marshal to. Defaults to [MarshallingFormat.XML].
     * @param toMarshal An Array of Objects (assumed to be JAXB-annotated) to marshal into a String.
     */
    protected abstract fun performMarshalling(loader: ClassLoader = Thread.currentThread().contextClassLoader,
                                              format: MarshallingFormat = MarshallingFormat.XML,
                                              toMarshal: Array<Any>): String

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

    protected open fun createUnmarshaller(jaxbContext: JAXBContext): Unmarshaller = jaxbContext.createUnmarshaller()

    protected open fun doMarshalling(marshaller: Marshaller, toMarshal: Array<Any>): String {

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
 * Simple [SchemaOutputResolver] implementation intended mainly for JSON Schema generation.
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
