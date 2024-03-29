/*-
 * #%L
 * Nazgul Project: jguru-shared-json-spi-jackson
 * %%
 * Copyright (C) 2018 - 2023 jGuru Europe AB
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
package se.jguru.shared.json.spi.jackson

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.PrettyPrinter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import se.jguru.shared.json.spi.jackson.custom.SimplifiedFormatModule
import java.text.DateFormat
import java.util.TimeZone

/**
 * Factory class to create an [ObjectMapper] for use with Jackson/JSON Serialization
 * and Deserialization.
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
     * Assigns the supplied Include specification to the ObjectMapper.
     *
     * @param includeSpec The Include definition to assign. Defaults to `JsonInclude.Include.NON_NULL`
     * @return This builder, for chaining.
     */
    @JvmOverloads
    fun withNullFieldInclusion(includeSpec: JsonInclude.Include = JsonInclude.Include.NON_NULL): ObjectMapperBuilder {

        toReturn.setSerializationInclusion(includeSpec)

        // All Done.
        return this
    }

    /**
     * Assigns the supplied NamingStrategy to the ObjectMapper.
     *
     * @param namingStrategy The PropertyNamingStrategy definition to assign.
     * Defaults to `PropertyNamingStrategy.UpperCamelCaseStrategy`.
     *
     * @return This builder, for chaining.
     */
    @JvmOverloads
    fun withNamingStrategy(namingStrategy: PropertyNamingStrategy = PropertyNamingStrategy.LOWER_CAMEL_CASE)
        : ObjectMapperBuilder {

        toReturn.propertyNamingStrategy = namingStrategy

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
         * Emits a KotlinModule.Builder with default settings.
         */
        @JvmStatic
        fun defaultKotlinModuleBuilder(): KotlinModule.Builder = KotlinModule.Builder()
            .withReflectionCacheSize(512)
            .configure(KotlinFeature.NullToEmptyCollection, false)
            .configure(KotlinFeature.NullToEmptyMap, false)
            .configure(KotlinFeature.NullIsSameAsDefault, false)
            .configure(KotlinFeature.SingletonSupport, false)
            .configure(KotlinFeature.StrictNullChecks, false)

        /**
         * Retrieves a default ObjectMapper, with Kotlin, Java8 and ParameterNames module activated.
         * For Kotlin classes, use a `data class` target, to reduce the required amount of
         * annotations to `@JsonPropertyOrder`. A typical class definition is something like:
         * `@JsonPropertyOrder(value = ["name", "age"]) data class Person(val name : String, val age : Int)`
         */
        @JvmStatic
        @JvmOverloads
        fun getDefault(kotlinModuleBuilder: KotlinModule.Builder = defaultKotlinModuleBuilder()): ObjectMapper = ObjectMapperBuilder()
            .withPrettyPrinter()
            .withNullFieldInclusion()
            .withNamingStrategy()
            .withModule(ParameterNamesModule())
            .withModule(Jdk8Module())
            .withModule(JavaTimeModule())
            .withModule(SimplifiedFormatModule())
            .withModule(kotlinModuleBuilder.build())
            .withTimeZone(TimeZone.getDefault())
            .build()
    }
}
