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
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Processor implementation which by default binds the exception and its stacktrace
 * to 2 exchange properties.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
open class StandardExceptionProcessor : Processor {

    override fun process(exchange: Exchange) {

        // Extract the exception from the supplied Exchange
        val exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception::class.java)
                ?: exchange.exception

        // Bind the exception type to the EXCEPTION_TYPE_HEADER
        exchange.setProperty(EXCEPTION_TYPE_HEADER, exception::class.java.name)

        // Bind the exception stacktrace to the EXCEPTION_STACKTRACE_HEADER
        val out = StringWriter()
        exception.printStackTrace(PrintWriter(out))
        exchange.setProperty(EXCEPTION_STACKTRACE_HEADER, out.toString())
    }

    companion object {

        /**
         * The header where the exception stacktrace is bound by this StandardExceptionProcessor.
         */
        const val EXCEPTION_STACKTRACE_HEADER = "JGURU_EXCEPTION_STACKTRACE"

        /**
         * The header where the exception type is bound by this StandardExceptionProcessor.
         */
        const val EXCEPTION_TYPE_HEADER = "JGURU_EXCEPTION_TYPE"
    }
}
