/*-
 * #%L
 * Nazgul Project: jguru-shared-messaging-api
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
package se.jguru.shared.messaging.api

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import se.jguru.codestyle.annotations.UseOpenMembers
import java.time.Duration
import java.time.temporal.ChronoUnit
import javax.jms.CompletionListener
import javax.jms.Message

/**
 * ## [CompletionListener] with time measurements capabilities
 *
 * Used to simplify tracking sending durations when using an async lifecycle.
 */
interface DurationAwareCompletionListener : CompletionListener {

    /**
     * Starts the time measurement; assigns a start timestamp
     */
    fun noteStart()

    /**
     * Ends the time measurement; assign a stop timestamp.
     *
     * @return This [DurationAwareCompletionListener], for chaining.
     * @throws IllegalStateException if this DurationAwareCompletionListener was not started.
     */
    @Throws(IllegalStateException::class)
    fun noteStop() : DurationAwareCompletionListener

    /**
     * @return The duration between the noted start and stop timestamps.
     */
    fun getDuration() : Duration
}

/**
 * ## Logging [CompletionListener]
 *
 * Used to simplify tracking sending durations when using an async lifecycle.
 */
@UseOpenMembers
open class SimpleDurationMeasuringCompletionListener : DurationAwareCompletionListener {

    private var startedAt : Long? = null
    private var stoppedAt : Long? = null

    override fun noteStart() {
        startedAt = System.nanoTime()
    }

    override fun noteStop(): DurationAwareCompletionListener {

        stoppedAt = System.nanoTime()

        if(startedAt == null || (startedAt != null && startedAt!! > stoppedAt!!)) {
            throw IllegalStateException("Start time must exist, and be before stop time.")
        }

        return this
    }

    override fun getDuration(): Duration {

        if(startedAt == null) {
            throw IllegalStateException("Start time must exist")
        } else if(stoppedAt == null) {
            throw IllegalStateException("Stop time must exist")
        }

        // All Done.
        return Duration.ofNanos(stoppedAt!! - startedAt!!)
    }

    override fun onCompletion(message: Message?) {

        noteStop()

        if(log.isDebugEnabled) {

            log.debug("Message [${message?.jmsMessageID?:"<no ID>"}] of " +
                "type ${message?.jmsType?:"<Unknown>"} successfully delivered " +
                "in ${toHumanReadableDuration(getDuration())}")
        }
    }

    override fun onException(message: Message?, exception: Exception?) {

        noteStop()

        if(log.isDebugEnabled) {

            val messageDescription = when(message == null) {
                true -> "."
                else -> "[${message.jmsMessageID?:"<no ID>"}] of type ${message.jmsType?:"<Unknown>"}."
            }

            log.debug("Could not deliver Message$messageDescription in ${toHumanReadableDuration(getDuration())}")
        }
    }

    companion object {

        @JvmStatic
        internal val log : Logger = LoggerFactory.getLogger(SimpleDurationMeasuringCompletionListener::class.java)

        @JvmStatic
        internal fun toHumanReadableDuration(duration: Duration) : String = when {
            duration.seconds < 0 -> "${duration.get(ChronoUnit.MILLIS)} ms"
            else -> "${duration.get(ChronoUnit.SECONDS)} s, ${duration.get(ChronoUnit.MILLIS)} ms"
        }
    }
}