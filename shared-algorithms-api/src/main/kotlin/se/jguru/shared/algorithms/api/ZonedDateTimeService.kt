/*-
 * #%L
 * Nazgul Project: jguru-shared-algorithms-api
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
package se.jguru.shared.algorithms.api

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.TimeZone

/**
 * Specification for how to retrieve zoned timestamps and dates in addition to local ones.
 * Should a constant value for time/timezone/date be desired (typically required within unit tests), override
 * the [getZoned] method to provide a custom implementation.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
interface ZonedDateTimeService : LocalDateTimeService {

    /**
     * Retrieves the current timestamp in the form of a [LocalDateTime], by
     * default retrieved by delegating to the [getZoned] method.
     */
    override fun getNow(): LocalDateTime = getZoned().toLocalDateTime()

    /**
     * Retrieves the current [ZonedDateTime] timestamp.
     */
    fun getZoned(): ZonedDateTime = ZonedDateTime.now()

    /**
     * Convenience method finding the [TimeZone] from the [ZonedDateTime] retrieved by the [getZoned] method.
     */
    fun getTimeZone(): TimeZone = TimeZone.getTimeZone(getZoned().zone)

    companion object {

        /**
         * Simple factory method to create a [ZonedDateTime] from a [LocalDateTime] using the supplied [ZoneOffset]
         * to calculate the actual [ZoneId] used.
         *
         * @param timestamp The [LocalDateTime] timestamp to infer into a [TimeZone]. Defaults to [LocalDateTime.now]
         * unless explicitly given.
         * @param zoneOffset The [ZoneOffset] used to define the timezone. Defaults to [ZoneOffset.UTC] unless
         * explicitly given.
         */
        @JvmOverloads
        fun create(timestamp: LocalDateTime = LocalDateTime.now(), zoneOffset: ZoneOffset = ZoneOffset.UTC)
            : ZonedDateTime = ZonedDateTime.ofLocal(timestamp, zoneOffset, zoneOffset)
    }
}
