/*-
 * #%L
 * Nazgul Project: jguru-shared-json-spi-jackson
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
package se.jguru.shared.json.spi.jackson.custom

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

/**
 * Abstract [StdSerializer] implementation, accepting a [DateTimeFormatter] to convert/serialize
 * a [TemporalAccessor] subclass.
 *
 * @param formatter The [DateTimeFormatter] to use for serializing data.
 * @param serializedType The [TemporalAccessor] (sub-)type which can be serialized by this AbstractTimeSerializer.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
abstract class AbstractDateTimeFormatterSerializer<T : TemporalAccessor>(
    open val formatter: DateTimeFormatter,
    serializedType: Class<T>) : StdSerializer<T>(serializedType) {

    override fun serialize(value: T?, gen: JsonGenerator, serializers: SerializerProvider) {

        when (value == null) {
            true -> gen.writeNull()
            else -> gen.writeString(formatter.format(value))
        }
    }
}

/**
 * A Serializer for [LocalDate] objects.
 *
 * @param formatter The DateTimeFormatter used to format the LocalDate as a String.
 */
open class LocalDateSerializer(
    override val formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
) : AbstractDateTimeFormatterSerializer<LocalDate>(formatter, LocalDate::class.java)

/**
 * A Serializer for [LocalDateTime] objects.
 *
 * @param formatter The DateTimeFormatter used to format the LocalDateTime as a String.
 */
open class LocalDateTimeSerializer(
    override val formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
) : AbstractDateTimeFormatterSerializer<LocalDateTime>(formatter, LocalDateTime::class.java)

/**
 * A Serializer for [LocalTime] objects.
 *
 * @param formatter The DateTimeFormatter used to format the LocalTime as a String.
 */
open class LocalTimeSerializer(
    override val formatter: DateTimeFormatter = ISO_WITHOUT_SECONDS
) : AbstractDateTimeFormatterSerializer<LocalTime>(formatter, LocalTime::class.java) {

    companion object {

        /**
         * Default [DateTimeFormatter] for LocalTime objects.
         */
        @JvmStatic
        val ISO_WITHOUT_SECONDS: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}

/**
 * Serializes a Duration to an ISO-8601-compliant string.
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
 * @see Duration.toString
 */
open class DurationSerializer : StdSerializer<Duration>(Duration::class.java) {

    override fun serialize(value: Duration?, gen: JsonGenerator, serializers: SerializerProvider) {

        when (value == null) {
            true -> gen.writeNull()
            else -> gen.writeString(value.toString())
        }
    }
}
