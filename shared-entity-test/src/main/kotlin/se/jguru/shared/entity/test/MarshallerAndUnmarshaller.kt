package se.jguru.shared.entity.test

import java.io.Serializable

/**
 * Definition for frequently used formats for JAXB Marshalling and Unmarshalling.
 */
enum class MarshallingFormat {
    XML,
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
    val typeInformation : MutableList<Class<Any>> = mutableListOf(),
    val supportedFormats: List<MarshallingFormat> = listOf(MarshallingFormat.XML)) : MarshallerAndUnmarshaller {

    override fun add(vararg typeInformation: Class<in Any>) {
        typeInformation.forEach { this.typeInformation.add(it) }
    }

    override fun marshal(loader: ClassLoader, format: MarshallingFormat, vararg toMarshal: Any): String {

        // Check sanity
        if(!supportedFormats.contains(format)) {
            throw IllegalArgumentException("Unsupported format $format. Supported formats are: " +
            supportedFormats.map { it.name }.sorted().reduce { l, r -> l + ", " + r })
        }
        if(toMarshal.isEmpty()) {
            throw IllegalArgumentException("Stubbornly refusing to marshal no Objects.")
        }

        // Delegate
        return performMarshalling(loader, format, toMarshal)
    }

    override fun <T> unmarshal(loader: ClassLoader, format: MarshallingFormat, resultType: Class<T>, toUnmarshal: String): T {

        // Check sanity
        if(!supportedFormats.contains(format)) {
            throw IllegalArgumentException("Unsupported format $format. Supported formats are: " +
                supportedFormats.map { it.name }.sorted().reduce { l, r -> l + ", " + r })
        }
        if(toUnmarshal.isEmpty() || toUnmarshal.isBlank()) {
            throw IllegalArgumentException("Stubbornly refusing to unmarshal empty/blank String.")
        }

        // Delegate
        return performUnmarshalling(loader, format, resultType, toUnmarshal)
    }


    /**
     * Implement this method to perform actual unmarshalling using the underlying technology.
     */
    protected abstract fun <T> performUnmarshalling(loader: ClassLoader,
                                                format: MarshallingFormat,
                                                resultType: Class<T>,
                                                toUnmarshal: String) : T

    /**
     * Implement this method to perform actual marshalling using the underlying technology.
     */
    protected abstract fun performMarshalling(loader: ClassLoader,
                                              format: MarshallingFormat,
                                              vararg toMarshal: Any): String
}