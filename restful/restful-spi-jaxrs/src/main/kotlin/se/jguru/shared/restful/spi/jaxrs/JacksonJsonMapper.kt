/*-
 * #%L
 * Nazgul Project: jguru-shared-restful-spi-jaxrs
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
package se.jguru.shared.restful.spi.jaxrs

import se.jguru.shared.json.spi.jackson.JacksonAlgorithms
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.Type
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.ext.MessageBodyReader
import javax.ws.rs.ext.MessageBodyWriter

/**
 * ## Abstract JSON-form [MessageBodyReader] and [MessageBodyWriter] implementation
 *
 * Uses the standard (de-)serialization algorithms found within the JSON SPI for Jackson to
 * convert T type objects to and from JSON transport form. The recommended approach is to
 * extend this class and remove the implied generic, which is required to let the CDI
 * implementation inject a [Provider] in any scope (not only Dependent).
 *
 * ### Note!
 *
 * According to §3.1 of the JSR-299 (CDI) specification:
 * > "If the managed bean class is a generic type, it must have scope @Dependent.
 * > If a managed bean with a parameterized bean class declares any scope other than @Dependent, the
 * > container automatically detects the problem and treats it as a definition error."
 *
 * Therefore, if a class harbours generic annotations, it may imply that the CDI implementation is
 * scope, not only Dependent which is sometimes required when the producer class has generic annotations.
 * Resolve this in a manner similar to the below:
 *
 * ```java
 * public abstract class JacksonJsonMapper<T> {...}
 *
 * @Producer
 * public class FooBarJsonMapper extends JacksonJsonMapper<FooBar> {}
 *
 * @Producer
 * public class CarJsonMapper extends JacksonJsonMapper<Car> {}
 * ```
 *
 * ## A common-for-all-classes JSON mapper
 *
 * There is a convenience implementation, which can be used in the following manner:
 *
 * ```java
 * @Producer
 * public class MyJsonProvider extends JacksonJsonAnyMapper {}
 * ```
 *
 * Deserializing objects of type Foo using the MyJsonProvider would be done using a
 * rather odd class-cast construct, namely:
 *
 * ```java
 *  val resurrected = jsonAnyMapper.readFrom(
 *      Furniture::class.java as Class<Any>,  // A bit weird, but required to coerce Kotlin's type system.
 *      String::class.java,
 *      emptyArray,
 *      jsonType,
 *      emptyMultiValuedMap,
 *      input)
 * ```
 *
 * @param T The class for which this JacksonJsonMapper should be used.
 * @see [JacksonJsonAnyMapper]
 */
abstract class JacksonJsonMapper<T> : MessageBodyReader<T>, MessageBodyWriter<T> {

    override fun isReadable(aClass: Class<*>?,
                            type: Type?,
                            annotations: Array<Annotation>?,
                            mediaType: MediaType): Boolean = isJSON(mediaType)

    override fun isWriteable(aClass: Class<*>?,
                             type: Type?,
                             annotations: Array<Annotation>?,
                             mediaType: MediaType): Boolean = isJSON(mediaType)

    override fun readFrom(
        aClass: Class<T>,
        type: Type?,
        annotations: Array<Annotation>?,
        mediaType: MediaType,
        multivaluedMap: MultivaluedMap<String, String>?,
        inputStream: InputStream): T = JacksonAlgorithms.deserializeFromStream(inputStream, aClass)

    override fun writeTo(
        o: T,
        aClass: Class<*>?,
        type: Type?,
        annotations: Array<out Annotation>?,
        mediaType: MediaType,
        multivaluedMap: MultivaluedMap<String, Any>?,
        outputStream: OutputStream) = JacksonAlgorithms.serializeToStream(o, outputStream)

    companion object {

        /**
         * Convenience constant, adding the charset information to the APPLICATION_JSON mediatype.
         */
        @JvmStatic
        val JSON_UTF8_MEDIA_TYPE = MediaType.APPLICATION_JSON + ";charset=utf-8"

        @JvmStatic
        private fun isJSON(mediaType: MediaType) = MediaType.APPLICATION_JSON_TYPE == mediaType
            || JSON_UTF8_MEDIA_TYPE == mediaType.toString()
    }
}
