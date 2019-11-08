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
 * Extend and add the Provider annotation to this JacksonJSON Mapper class to use it within a JaxRS application.
 */
open class JacksonJsonMapper<T> : MessageBodyReader<T>, MessageBodyWriter<T> {

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
