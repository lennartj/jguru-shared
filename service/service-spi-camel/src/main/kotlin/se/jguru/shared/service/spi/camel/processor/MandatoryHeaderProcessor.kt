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
package se.jguru.shared.service.spi.camel.processor

import org.apache.camel.Exchange
import org.apache.camel.Processor

/**
 * The message header containing a requested SemanticVersion
 */
const val HEADER_VERSION = "version"

/**
 * Processor implementation which verifies that a header is present on the
 *
 * @param requiredHeader The name of the required header
 * @param onInboundMessage If true, the required parameter must be present on the inbound Message - and otherwise on
 * the Exchange.
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
open class MandatoryHeaderProcessor @JvmOverloads constructor(
        val requiredHeader: String,
        private val onInboundMessage: Boolean = true) : Processor {

    @Throws(IllegalStateException::class)
    override fun process(exchange: Exchange) {

        val theHeader = when (onInboundMessage) {
            true -> exchange.getIn().getHeader(requiredHeader)
            else -> exchange.getProperty(requiredHeader)
        }

        if (theHeader == null) {

            val routeID = exchange.fromRouteId ?: "<unknown>"
            val exception = IllegalStateException("Required header [$requiredHeader] not present on ${getSource()}. "
                    + "Please check RouteBuilder configuration for route [$routeID].")

            exchange.setException(exception);
            throw exception
        }
    }

    override fun toString(): String {

        // All Done.
        return "MandatoryHeaderProcessor (header $requiredHeader on ${getSource()})"
    }

    private fun getSource() = when (onInboundMessage) {
        true -> "Message"
        false -> "Exchange"
    }
}

/**
 * Validates that a version header is present, or throws an Exception.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
open class VersionHeaderPresentProcessor @JvmOverloads constructor(onInboundMessage: Boolean = true)
    : MandatoryHeaderProcessor(HEADER_VERSION, onInboundMessage)
