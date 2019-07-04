/*-
 * #%L
 * Nazgul Project: jguru-shared-messaging-jms-test
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
package se.jguru.shared.messaging.test.jms

import javax.jms.JMSException
import javax.jms.Message
import javax.jms.MessageListener
import javax.jms.MessageProducer
import javax.jms.Session

/**
 * Abstract [MessageListener] implementation which provides transactional mechanics.
 * Use this as a superclass when creating transactional clients.
 */
abstract class AbstractTransactionalMessageListener(
    val serverSideResponseSession: Session,
    val responseMessageProducer: MessageProducer) : MessageListener {

    // Internal state
    private val serverSideReceivedMessages: MutableList<Message> = mutableListOf()

    /**
     * Template onMessage method, delegating all real processing to `generateResponse`.
     *
     * @see .generateResponse
     */
    override fun onMessage(message: Message) {

        // Stash the received message for test purposes
        serverSideReceivedMessages.add(message)

        try {

            // Generate a response.
            val toReturn = generateResponse<Message>(message)

            // Send the error message back to the client.
            responseMessageProducer.send(toReturn)
            serverSideResponseSession.commit()

        } catch (e: JMSException) {
            throw IllegalStateException("Could not send message.", e)
        }
    }

    /**
     * Override this method to produce a response from an inbound request.
     *
     * @param inboundRequestMessage     The inbound request message, sent from the client side.
     * @param <T>                       The explicit Message type.
     * @return The response Message to be sent out by the JMS server side in response to the client request.
     * @throws javax.jms.JMSException If the creating of JMS messages fails.
     */
    @Throws(JMSException::class)
    protected abstract fun <T : Message> generateResponse(inboundRequestMessage: Message): T
}
