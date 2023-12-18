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

import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Specification for how to retrieve local timestamps and dates.
 * Should a constant value for time/date be desired (typically required within unit
 * tests), override the [getNow] method to provide a custom implementation.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
interface LocalDateTimeService : Serializable {

    /**
     * Retrieves the current timestamp in the form of a [LocalDateTime]
     */
    fun getNow(): LocalDateTime = LocalDateTime.now()

    /**
     * Retrieves the current [LocalDate] (i.e. "today").
     */
    fun getToday(): LocalDate = getNow().toLocalDate()
}
