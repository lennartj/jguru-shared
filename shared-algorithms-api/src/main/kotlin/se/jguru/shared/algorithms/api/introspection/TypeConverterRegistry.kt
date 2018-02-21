package se.jguru.shared.algorithms.api.introspection

import java.lang.annotation.Inherited

@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Inherited
@Target(AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.EXPRESSION)
annotation class Converter(
    /**
     * Parameter to indicate that this converter method or constructor accepts `null` values.
     * Defaults to `false`.
     * If this parameter is set to `true`, your converter [method or constructor] indicates that it
     * should be able to provide a default value in the case of null input values. A typical example is
     * provided below:
     * <pre>
     *
     * &#64;Converter(acceptsNullValues = true)
     * public StringBuffer convert(final String aString) {
     * final String bufferValue = aString == null ? "nothing!" : aString;
     * return new StringBuffer(bufferValue);
     * }
    </pre> *
     *
     * @return `true` if this Converter accepts `null`s as values for conversion.
     */
    val acceptsNullValues: Boolean = false,

    /**
     *
     * Parameter to indicate which priority this converter method or constructor should have.
     * A lower (but positive, until minimum 0) priority implies that this converter will be
     * attempted **before** a converter with higher priority value.
     * In that sense, the priority should be regarded as the execution index of several converters.
     *
     * A typical example for defining a would be:
     * <pre>
     * class AnotherFooConverter {
     *
     * &#64;Converter(priority = 200)
     * public String convert(Foo aFoo) {
     * ...
     * }
     *
     * public boolean checkFoo(Foo aFoo) {
     * // Find out if the aFoo can be converted by this FooConverter instance.
     * }
     * }
    </pre> *
     *
     * @return the priority of this Converter method or constructor. Members with lower priorities are
     * attempted **before** higher priority ones.
     */
    val priority: Int = DEFAULT_PRIORITY,

    /**
     *
     * Name of a method with a single parameter of the same source type as the `@Converter`-annotated
     * method, and returning a `boolean`.
     * If present, this conditionalConversionMethod value supplies the name of a method [within the
     * same class as this Converter] which should be invoked to find out if the supplied source object
     * can be converted by this method.
     *
     * This attribute is ignored for Constructor Converters. A typical example would be:
     * <pre>
     * class FooConverter {
     *
     * &#64;Converter(conditionalConversionMethod = "checkFoo")
     * public String convert(Foo aFoo) {
     * ...
     * }
     *
     * public boolean checkFoo(Foo aFoo) {
     * // Find out if the aFoo can be converted by this FooConverter instance.
     * }
     * }
    </pre> *
     *
     * @return Name of a method with a single parameter of the same source type as the `@Converter`-annotated
     * method, and returning a `boolean`.
     */
    val conditionalConversionMethod: String = NO_CONVERTER_METHOD;

    companion object {

        /**
         * The default priority value, used if no other priority has been supplied.
         */
        const val DEFAULT_PRIORITY = 100

        /**
         * The default converterMethod value, used to indicate that no converter method name has been supplied.
         */
        val NO_CONVERTER_METHOD = "##NONE##"
    }
}


/**
 * Specification for a type converter registry.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
interface TypeConverterRegistry {

    /**
     * Adds the supplied converters to this DefaultJaxbConverterRegistry.
     *
     * @param converters The converter instances to add.
     * @throws IllegalArgumentException if any of the supplied converters had no Converter-annotated methods or
     * constructors.
     */
    @Throws(IllegalArgumentException::class)
    fun addConverters(vararg converters: Any)

    /**
     * Acquires the TransportType for the supplied originalType.
     *
     * @param originalType The original (i.e. non-transport) type for which we want to acquire the corresponding
     * TransportType.
     * @param <TransportType> The JAXB-annotated TransportType corresponding to the supplied originalType;
     * @param <OriginalType>  The OriginalType for which we would like to acquire the JAXB-annotated TransportType.
     * @return The Class of the TransportType for the supplied OriginalType.
     */
    fun <TransportType, OriginalType> getTransportType(originalType: Class<OriginalType>): Class<TransportType>

    /**
     * Acquires the OriginalType for the supplied transportType.
     *
     * @param transportType   The [JAXB annotated] TransportType type for which we want to acquire
     * the corresponding OriginalType.
     * @param <TransportType> The JAXB-annotated TransportType corresponding to the supplied originalType;
     * @param <OriginalType>  The type previously converted to the supplied TransportType.
     * @return The Class of the OriginalType for the supplied TransportType.
     * @throws IllegalArgumentException if the transportType was `JaxbAnnotatedNull`.
     */
    @Throws(IllegalArgumentException::class)
    fun <OriginalType, TransportType> getOriginalType(transportType: Class<TransportType>): Class<OriginalType>

    /**
     * Converts the provided instance to a transport type, ready for transmission in serialized form.
     *
     * @param source          The object to convert.
     * @param <TransportType> The JAXB-annotated TransportType corresponding to the supplied originalType;
     * @param <OriginalType>  The OriginalType for which we would like to acquire the JAXB-annotated TransportType.
     * @return The converted object.
     * @throws IllegalArgumentException if the conversion failed.
     */
    @Throws(IllegalArgumentException::class)
    fun <OriginalType, TransportType> packageForTransport(source: OriginalType): TransportType

    /**
     * Converts the provided transport type instance (back) to its original type, ready for normal use.
     *
     * @param toConvert       The transport type to be converted.
     * @param <TransportType> The JAXB-annotated TransportType corresponding to the supplied originalType;
     * @param <OriginalType>  The OriginalType for which we would like to acquire the JAXB-annotated TransportType.
     * @return The resurrected object.
     * @throws IllegalArgumentException if the conversion failed.
     */
    @Throws(IllegalArgumentException::class)
    fun <OriginalType, TransportType> resurrectAfterTransport(toConvert: TransportType): OriginalType
}

/**
 * Simple TypeConverterRegistry implementation.
 * 
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class SimpleTypeConverterRegistry : TypeConverterRegistry {

    override fun addConverters(vararg converters: Any) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <TransportType, OriginalType> getTransportType(originalType: Class<OriginalType>): Class<TransportType> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <OriginalType, TransportType> getOriginalType(transportType: Class<TransportType>): Class<OriginalType> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <OriginalType, TransportType> packageForTransport(source: OriginalType): TransportType {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <OriginalType, TransportType> resurrectAfterTransport(toConvert: TransportType): OriginalType {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

