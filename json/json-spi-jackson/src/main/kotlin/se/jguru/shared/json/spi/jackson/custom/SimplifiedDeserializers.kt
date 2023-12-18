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
package se.jguru.shared.json.spi.jackson.custom

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.TemporalAccessor

/**
 * Abstract [StdDeserializer] implementation, accepting a [DateTimeFormatter] to
 * convert/deserialize a [TemporalAccessor] subclass.
 *
 * @param formatter The [DateTimeFormatter] to use for serializing data.
 * @param handledType The [TemporalAccessor] (sub-)type which can be de-serialized by this AbstractTimeSerializer.
 * @param parseMethod The method to parse a string into the relevant subtype of [TemporalAccessor].
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
abstract class AbstractDateTimeFormatterDeserializer<T : TemporalAccessor>(
    open val formatter: DateTimeFormatter,
    handledType: Class<T>,
    private val parseMethod: (toParse: String, formatter: DateTimeFormatter) -> T
) : StdDeserializer<T>(handledType) {

    override fun deserialize(parser: JsonParser, context: DeserializationContext): T? {

        if (parser.hasToken(JsonToken.VALUE_STRING)) {
            val string = parser.text.trim { it <= ' ' }

            try {
                return when (string.isEmpty()) {
                    true -> null
                    else -> parseMethod.invoke(string, formatter)
                }
            } catch (e: DateTimeException) {
                rethrowDateTimeTypeException<Any, T>(handledType() as Class<T>, context, e, string)
            }
        }

        // Nopes.
        throw context.wrongTokenException(parser, handledType(), JsonToken.VALUE_STRING, "Expected string.")
    }

    companion object {

        /**
         * Re-throws an inbound DateTimeException as a [JsonMappingException].
         *
         * @param handledType The type handled by this [AbstractDateTimeFormatterDeserializer]
         * @param context The active [DeserializationContext]
         * @param ex The emitted DateTimeException
         * @param value The json token value which was parsed.
         */
        @JvmStatic
        fun <NeverMind, T> rethrowDateTimeTypeException(
            handledType: Class<T>,
            context: DeserializationContext,
            ex: DateTimeException,
            value: String): NeverMind {

            val msg: String = ex.message ?: ""

            if (ex is DateTimeParseException || msg.contains("invalid format")) {
                val reThrown: JsonMappingException = context.weirdStringException(value, handledType, ex.message)
                reThrown.initCause(ex)

                // All Done.
                throw reThrown
            }

            // Fallback
            return context.reportInputMismatch(
                handledType,
                "Failed to deserialize ${handledType.name}: (${ex::class.java.name}) %s",
                msg)
        }
    }
}

/**
 * A Deserializer for [LocalDateTime] objects.
 *
 * @param formatter The DateTimeFormatter used to format the LocalDateTime as a String.
 */
open class LocalDateTimeDeserializer(
    override val formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
) : AbstractDateTimeFormatterDeserializer<LocalDateTime>(
    formatter,
    LocalDateTime::class.java,
    { toParse, ldFormatter -> LocalDateTime.parse(toParse, ldFormatter)})

/**
 * A Deserializer for [LocalDate] objects.
 *
 * @param formatter The DateTimeFormatter used to format the LocalDate as a String.
 */
open class LocalDateDeserializer(
    override val formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
) : AbstractDateTimeFormatterDeserializer<LocalDate>(
    formatter,
    LocalDate::class.java,
    { toParse, ldFormatter -> LocalDate.parse(toParse, ldFormatter)})

/**
 * A Deserializer for [LocalTime] objects.
 *
 * @param formatter The DateTimeFormatter used to format the LocalTime as a String.
 */
open class LocalTimeDeserializer(
    override val formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_TIME
) : AbstractDateTimeFormatterDeserializer<LocalTime>(
    formatter,
    LocalTime::class.java,
    { toParse, ldFormatter -> LocalTime.parse(toParse, ldFormatter)})

/**
 * A Deserializer for [ZonedDateTime] objects.
 *
 * @param formatter The DateTimeFormatter used to format the ZonedDateTime as a String.
 */
open class ZonedDateTimeDeserializer(
    override val formatter: DateTimeFormatter = ZonedDateTimeSerializer.ZONED_HUMAN_READABLE_FORM
) : AbstractDateTimeFormatterDeserializer<ZonedDateTime>(
    formatter,
    ZonedDateTime::class.java,
    { toParse, ldFormatter -> ZonedDateTime.parse(toParse, ldFormatter)})


/**
 * Deserializes a Duration from an ISO-8601-compliant string.
 *
 * ### Examples:
 *
 * ```
 *    "PT20.345S" -- parses as "20.345 seconds"
 *    "PT15M"     -- parses as "15 minutes" (where a minute is 60 seconds)
 *    "PT10H"     -- parses as "10 hours" (where an hour is 3600 seconds)
 *    "P2D"       -- parses as "2 days" (where a day is 24 hours or 86400 seconds)
 *    "P2DT3H4M"  -- parses as "2 days, 3 hours and 4 minutes"
 *    "PT-6H3M"    -- parses as "-6 hours and +3 minutes"
 *    "-PT6H3M"    -- parses as "-6 hours and -3 minutes"
 *    "-PT-6H+3M"  -- parses as "+6 hours and -3 minutes"
 * ```
 *
 * @see Duration.parse
 */
open class DurationDeserializer : StdDeserializer<Duration>(Duration::class.java) {

    override fun deserialize(parser: JsonParser, context: DeserializationContext): Duration? {

        if (parser.hasToken(JsonToken.VALUE_STRING)) {
            val string = parser.text.trim { it <= ' ' }

            try {
                return when (string.isEmpty()) {
                    true -> null
                    else -> Duration.parse(string)
                }
            } catch (e: DateTimeException) {
                AbstractDateTimeFormatterDeserializer.rethrowDateTimeTypeException<Any, Duration>(
                    handledType() as Class<Duration>, context, e, string)
            }
        }

        // Nopes.
        throw context.wrongTokenException(parser, handledType(), JsonToken.VALUE_STRING, "Expected string.")
    }
}

/**
 * Deserializes a MonthDay from a string on the format {@code --MM-dd}.
 *
 * ### Examples:
 *
 * ```
 *    "--01-09" -- parses as "Month.JANUARI, 9"
 *    "--09-10" -- parses as "Month.SEPTEMBER, 10"
 * ```
 *
 * @see MonthDay.parse
 */
open class MonthDayDeserializer : StdDeserializer<MonthDay>(MonthDay::class.java) {

    override fun deserialize(parser: JsonParser, context: DeserializationContext): MonthDay? {

        if (parser.hasToken(JsonToken.VALUE_STRING)) {
            val string = parser.text.trim { it <= ' ' }

            try {
                return when (string.isEmpty()) {
                    true -> null
                    else -> MonthDay.parse(string)
                }
            } catch (e: DateTimeException) {
                AbstractDateTimeFormatterDeserializer.rethrowDateTimeTypeException<Any, MonthDay>(
                    handledType() as Class<MonthDay>, context, e, string)
            }
        }

        // Nopes.
        throw context.wrongTokenException(parser, handledType(), JsonToken.VALUE_STRING, "Expected string.")
    }
}

/**
 * Deserializes a Period from a string as retrieved when serializing a Period toString.
 *
 * ### Examples:
 *
 * ```
 *    "P1D" -- parses as "1 day"
 *    "P7D" -- parses as "7 days (or 1 week)"
 *    "P1M7D" -- parses as "1 month and 7 days (or 1 month and 1 week)"
 *    "P1Y1M1D" -- parses as "1 year, 1 month and 1 day"
 * ```
 *
 * @see Period.parse
 */
open class PeriodDeserializer : StdDeserializer<Period>(Period::class.java) {

    override fun deserialize(parser: JsonParser, context: DeserializationContext): Period? {

        if (parser.hasToken(JsonToken.VALUE_STRING)) {
            val string = parser.text.trim { it <= ' ' }

            try {
                return when (string.isEmpty()) {
                    true -> null
                    else -> Period.parse(string)
                }
            } catch (e: DateTimeException) {
                AbstractDateTimeFormatterDeserializer.rethrowDateTimeTypeException<Any, Period>(
                    handledType() as Class<Period>, context, e, string)
            }
        }

        // Nopes.
        throw context.wrongTokenException(parser, handledType(), JsonToken.VALUE_STRING, "Expected string.")
    }
}
