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

import javax.jms.Session

/**
 * Proper enumeration to wrap the only valid JMS Session acknowledge modes.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
enum class SessionAcknowledgeMode(val jmsConstant: Int) {

    /**
     * With this acknowledgment mode, the session automatically acknowledges
     * a client's receipt of a message either when the session has successfully
     * returned from a call to `receive` or when the message
     * listener the session has called to process the message successfully
     * returns.
     *
     * @see Session.AUTO_ACKNOWLEDGE
     */
    AUTO_ACKNOWLEDGE(Session.AUTO_ACKNOWLEDGE),

    /**
     * With this acknowledgment mode, the client acknowledges a consumed
     * message by calling the message's `acknowledge` method.
     * Acknowledging a consumed message acknowledges all messages that the
     * session has consumed.
     *
     * <P>When client acknowledgment mode is used, a client may build up a
     * large number of unacknowledged messages while attempting to process
     * them. A JMS provider should provide administrators with a way to
     * limit client overrun so that clients are not driven to resource
     * exhaustion and ensuing failure when some resource they are using
     * is temporarily blocked.
     *
     * @see Session.CLIENT_ACKNOWLEDGE
     */
    CLIENT_ACKNOWLEDGE(Session.CLIENT_ACKNOWLEDGE),

    /**
     * This acknowledgment mode instructs the session to lazily acknowledge
     * the delivery of messages. This is likely to result in the delivery of
     * some duplicate messages if the JMS provider fails, so it should only be
     * used by consumers that can tolerate duplicate messages. Use of this
     * mode can reduce session overhead by minimizing the work the
     * session does to prevent duplicates.
     *
     * @see Session.DUPS_OK_ACKNOWLEDGE
     */
    DUPS_OK_ACKNOWLEDGE(Session.DUPS_OK_ACKNOWLEDGE)
}