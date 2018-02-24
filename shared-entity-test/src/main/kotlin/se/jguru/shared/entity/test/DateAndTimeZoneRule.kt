/*-
 * #%L
 * Nazgul Project: jguru-shared-entity-test
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
package se.jguru.shared.entity.test

import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.TimeZone

/**
 * jUnit rule implementation to manage Date and DateTimeZone during a Test Case.
 * This has significance for all tests dealing with dates and time zones.
 *
 * @author [Lennart Jörelid](mailto:lj@jguru.se), jGuru Europe AB
 */
class DateAndTimeZoneRule(val desiredTimeZone: TimeZone? = null,
                          val desiredDateTime: LocalDateTime? = null) : TestWatcher() {

    /**
     * Convenience constructor assigning the [desiredTimeZone] by converting from the supplied [zoneId].
     * Defaults to [ZoneOffset.UTC].
     */
    constructor(zoneId: ZoneId = ZoneOffset.UTC, desiredDateTime: LocalDateTime? = null)
        : this(TimeZone.getTimeZone(zoneId), desiredDateTime)

    // Internal state
    private var originalTimeZone: TimeZone = TimeZone.getDefault()
    private var originalDateTime: LocalDateTime = LocalDateTime.now()

    /**
     * Invoked when a test is about to start
     */
    override fun starting(description: Description?) {

        // Override the timezone, if a desired value is provided
        if (desiredTimeZone != null) {
            TimeZone.setDefault(desiredTimeZone)
        }

        if(desiredDateTime != null) {
        }
    }

    /**
     * Invoked when a test is about to start
     */
    override fun finished(description: Description?) {

        // Reset the timezone if
        TimeZone.setDefault(originalTimeZone)
    }
}
