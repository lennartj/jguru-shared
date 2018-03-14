/*-
 * #%L
 * Nazgul Project: jguru-shared-algorithms-api
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
package se.jguru.shared.algorithms.api

import java.io.Serializable
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Locale
import java.util.TimeZone

/**
 * TimeZone container with a simplified API for accessing related TimeZone/Offset/Id types.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
interface TimeZoneWrapper : Serializable {

    /**
     * The TimeZone identifier for this TimeZoneWrapper.
     */
    val id: String

    /**
     * The preferred language for this TimeZoneWrapper, implying
     * a [Locale] containing only a language - and not a Country.
     */
    val preferredLanguage : Locale

    /**
     * Retrieves the TimeZone for the [id] of this TimeZoneWrapper.
     *
     * @return The [TimeZone] corresponding to the ID of this [TimeZoneWrapper].
     * @see id
     */
    fun getTimeZone(): TimeZone = TimeZone.getTimeZone(id)

    /**
     * Retrieves the [ZoneId] for this TimeZoneWrapper.
     *
     * @return The [ZoneId] corresponding to the [TimeZone] of this [TimeZoneWrapper].
     * @see id
     */
    fun getZoneId(): ZoneId = getTimeZone().toZoneId()

    /**
     * Retrieves the [ZoneOffset] for the [TimeZone] of this [TimeZoneWrapper] at the given [ZonedDateTime].
     *
     * @param at The [ZonedDateTime] timestamp when the [ZoneOffset] should be calculated.
     * @return The [ZoneOffset] at the supplied [LocalDateTime] within the [TimeZone] of this [TimeZoneWrapper].
     */
    fun getTimeZoneOffset(at: LocalDateTime): ZoneOffset = getZoneId().rules.getOffset(at)
}