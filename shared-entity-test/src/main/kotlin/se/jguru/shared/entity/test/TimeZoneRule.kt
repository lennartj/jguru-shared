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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.TimeZone

/**
 * jUnit rule implementation to manage Date and DateTimeZone during a Test Case.
 * This has significance for all tests dealing with dates and time zones.
 *
 * @author [Lennart Jörelid](mailto:lj@jguru.se), jGuru Europe AB
 */
class TimeZoneRule @JvmOverloads constructor(val desiredTimeZone: TimeZone? = null) : TestWatcher() {

    // Internal state
    private var originalTimeZone: TimeZone = TimeZone.getDefault()

    /**
     * Invoked when a test is about to start
     */
    override fun starting(description: Description?) {

        // Override the timezone, if a desired value is provided
        if (desiredTimeZone != null) {

            if (log.isDebugEnabled) {
                log.debug("Setting default TimeZone: ${desiredTimeZone.displayName}")
            }

            TimeZone.setDefault(desiredTimeZone)
        }
    }

    /**
     * Invoked when a test is about to start
     */
    override fun finished(description: Description?) {

        // Reset the timezone if it was originally altered
        if (desiredTimeZone != null) {

            if (log.isDebugEnabled) {
                log.debug("Restoring default TimeZone: ${originalTimeZone.displayName} " +
                    "(was: ${originalTimeZone.displayName})")
            }

            TimeZone.setDefault(originalTimeZone)
        }
    }

    companion object {

        @JvmStatic
        private val log: Logger = LoggerFactory.getLogger(TimeZoneRule::class.java.name)
    }
}
