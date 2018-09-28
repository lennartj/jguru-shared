/*-
 * #%L
 * Nazgul Project: jguru-shared-service-spi-camel
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
@file:JvmName("TransportModelFactory")
package se.jguru.shared.service.spi.camel.processor

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.camel.Exchange
import org.apache.camel.Message
import org.apache.camel.Processor
import se.jguru.shared.json.spi.jackson.JacksonAlgorithms
import se.jguru.shared.json.spi.jackson.ObjectMapperBuilder

/**
 * Factory implementation for Transport Model objects, which produces
 *
 * @param dataCreator A Function which accepts an inbound Camel [Message] and emits the object to
 * be serialized to JSON by the [JacksonAlgorithms.serialize] method.
 * @param objectMapper The Jackson ObjectMapper used to serialize data. Defaults to [ObjectMapperBuilder.getDefault].
 * @param compactOutput If `true`, the JSON serialization is made compact - as opposed to human-readable.
 * Defaults to `false` implying human-readable output.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
open class TransportModelFactory @JvmOverloads constructor(
    private val objectMapper: ObjectMapper = ObjectMapperBuilder.getDefault(),
    private val compactOutput: Boolean = false,
    private val dataCreator: (inMessage: Message) -> Any) : Processor {

    /**
     * Process method implementation which invokes the dataCreator method,
     * and serializes the result to JSON using the [JacksonAlgorithms.serialize] method.
     * The object is assumed to be stashed within the body of the inbound Message.
     */
    override fun process(exchange: Exchange) {

        // Create the Transport Model data structure.
        val transformed = dataCreator.invoke(exchange.`in`)

        // JSON'ify and set the new body.
        exchange.`in`.body = JacksonAlgorithms.serialize(transformed, objectMapper, compactOutput)
    }
}
