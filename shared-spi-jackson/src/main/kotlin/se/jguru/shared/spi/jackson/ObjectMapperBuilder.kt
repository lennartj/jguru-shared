/*-
 * #%L
 * Nazgul Project: jguru-shared-spi-jackson
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
package se.jguru.shared.spi.jackson

import com.fasterxml.jackson.core.PrettyPrinter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import java.text.DateFormat
import java.util.TimeZone

/**
 * Builder to create Jackson ObjectMapper objects.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class ObjectMapperBuilder(private val toReturn: ObjectMapper = ObjectMapper()) {

    /**
     * Builder method which retrieves the ObjectMapper result.
     *
     * @return The [ObjectMapper] to return.
     */
    fun build(): ObjectMapper = toReturn

    /**
     * Assigns the supplied PrettyPrinter to the ObjectMapper.
     *
     * @param prettyPrinter The [PrettyPrinter] to assign.
     * @return This builder, for chaining.
     */
    @JvmOverloads
    fun withPrettyPrinter(prettyPrinter: PrettyPrinter = DefaultPrettyPrinter()): ObjectMapperBuilder {

        // Assign the PrettyPrinter
        toReturn.setDefaultPrettyPrinter(prettyPrinter)

        // All Done.
        return this
    }

    /**
     * Registers the supplied Module within the ObjectMapper.
     *
     * @param module The module instance to assign.
     * @return This builder, for chaining.
     */
    fun withModule(module: Module): ObjectMapperBuilder {

        // Register the Module
        toReturn.registerModule(module)

        // All Done.
        return this
    }

    /**
     * Assigns the supplied TimeZone to the ObjectMapper.
     *
     * @param timeZone The TimeZone to assign.
     * @return This builder, for chaining.
     */
    fun withTimeZone(timeZone: TimeZone): ObjectMapperBuilder {

        // Set the timezone
        toReturn.setTimeZone(timeZone)

        // All Done.
        return this
    }

    /**
     * Assigns the supplied DateFormat to the ObjectMapper.
     *
     * @param dateFormat The DateFormat to assign. Defaults to Defaults to `DateFormat.getDateTimeInstance()`
     * @return This builder, for chaining.
     */
    @JvmOverloads
    fun withDateFormat(dateFormat: DateFormat = DateFormat.getDateTimeInstance()): ObjectMapperBuilder {

        toReturn.dateFormat = dateFormat

        // All Done.
        return this
    }

    companion object {

        /**
         * Retrieves a default ObjectMapper, with Kotlin, Java8 and ParameterNames module activated.
         * For Kotlin classes, use a `data class` target, to reduce the required amount of
         * annotations to `@JsonPropertyOrder`. A typical class definition is something like:
         * `@JsonPropertyOrder(value = ["name", "age"]) data class Person(val name : String, val age : Int)`
         */
        @JvmStatic
        fun getDefault(): ObjectMapper = ObjectMapperBuilder()
            .withPrettyPrinter()
            .withModule(ParameterNamesModule())
            .withModule(Jdk8Module())
            .withModule(JavaTimeModule())
            .withModule(KotlinModule())
            .withTimeZone(TimeZone.getDefault())
            .build()
    }
}
