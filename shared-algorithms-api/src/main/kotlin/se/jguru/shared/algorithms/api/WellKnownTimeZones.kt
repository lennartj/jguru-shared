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

import java.util.Locale

/**
 * An enum containing commonly available, and well-known, TimeZoneWrappers.
 *
 * @param id The identifier of the [java.util.TimeZone] for this [WellKnownTimeZones] object.
 * @param preferredLanguage A [Locale] object containing only the language which is preferred within the
 * [WellKnownTimeZones] instance.
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
enum class WellKnownTimeZones(override val id: String,
                              override val preferredLanguage: Locale) : TimeZoneWrapper {

    /**
     * Swedish timezone and utilities.
     */
    SWEDISH("Europe/Stockholm", "sv"),

    /**
     * Danish timezone and utilities.
     */
    DANISH("Europe/Copenhagen", "da"),

    /**
     * Finnish timezone and utilities.
     */
    FINNISH("Europe/Helsinki", "fi"),

    /**
     * Norwegian timezone and utilities.
     */
    NORWEGIAN("Europe/Oslo", "no");

    /**
     * Compound constructor wrapping a call to the [Locale] constructor before delegating.
     */
    constructor(id: String, languageCode: String) : this(id, Locale(languageCode))
}
