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

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.ArrayList
import jakarta.jms.Connection
import jakarta.jms.DeliveryMode
import jakarta.jms.JMSException
import jakarta.jms.Message
import jakarta.jms.MessageProducer
import jakarta.jms.Session

/**
 * Abstract superclass for Request-Response-based JMS tests.
 *
 * @param useTransactions if `true`, all retrieved sessions will be transacted by default.
 * @param broker The [MessageBroker] implementation used within this AbstractJmsTest.
 */
abstract class AbstractRequestResponseJmsTest(
    useTransactions: Boolean = false,
    broker: MessageBroker) : AbstractJmsTest(useTransactions, broker) {

    // Shared state
    protected var serverSideReceivedMessages = mutableListOf<Message>()
    protected lateinit var serverSideConnection: Connection

    /**
     * {@inheritDoc}
     */
    @Throws(JMSException::class)
    override fun tearDownServices() {
        // Do nothing.
    }

    /**
     * Override this method to perform any normal tear-down
     * before the Broker is stopped. You might cleanup any
     * instances which should be de-registered from the broker.
     */
    override fun beforeStopJmsBroker() {

        // Close the serverSideConnection.
        try {
            serverSideConnection.close()
        } catch (e: JMSException) {
            throw IllegalStateException("Could not close the serverSideConnection.", e)
        }
    }

    /**
     * {@inheritDoc}
     */
    @Throws(JMSException::class)
    override fun setupServices() {

        if (log.isInfoEnabled) {
            log.debug("Service-side setup starting.")
        }

        serverSideReceivedMessages = ArrayList()

        // This is where we set up JMS objects on the server side.
        // These objects are created before any test cases are launched.

        this.serverSideConnection = createConnection()

        // Consumer on Service side, reading messages from the Broker.
        //
        val serverSideRequestSession = createSession(serverSideConnection)
        val serviceSideInboundQueue = serverSideRequestSession.createQueue(
            SERVER_SIDE_INBOUND_REQUEST)
        val requestMessageConsumer = serverSideRequestSession.createConsumer(serviceSideInboundQueue)

        // Producer on Service side, writing messages to the Broker.
        //
        val serverSideResponseSession = createSession(serverSideConnection)
        val serviceSideOutboundQueue = serverSideResponseSession.createQueue(
            SERVER_SIDE_OUTBOUND_RESPONSE)
        val responseMessageProducer = serverSideResponseSession.createProducer(serviceSideOutboundQueue)
        responseMessageProducer.deliveryMode = DeliveryMode.NON_PERSISTENT

        // MessageListener on Service side.
        // Reads messages from the requestMessageConsumer and write messages to the responseMessageProducer.
        //
        requestMessageConsumer.messageListener = getServiceSideListener(
            serverSideReceivedMessages,
            serverSideResponseSession,
            responseMessageProducer)

        // Start the server-side Connection
        serverSideConnection.start()

        if (log.isInfoEnabled) {
            log.debug("Service-side setup complete.")
        }

    }

    /**
     * Creates a new AbstractTransactionalMessageListener instance for service-side use
     * in this AbstractRequestResponseJmsTest.
     *
     * @param serverSideReceivedMessages The non-null List to which this AbstractTransactionalMessageListener will
     * copy all inbound messages for test tracking purposes.
     * @param serverSideResponseSession  The non-null session used to create outbound (i.e. response) messages from
     * this MessageListener. Also used to commit JMS transactions.
     * @param responseMessageProducer    The non-null MessageProducer, created from the supplied
     * serverSideResponseSession, used to send response messages from the
     * @return The service-side listener used to handle inbound messages and send out responses.
     */
    protected abstract fun getServiceSideListener(
        serverSideReceivedMessages: MutableList<Message>,
        serverSideResponseSession: Session,
        responseMessageProducer: MessageProducer): AbstractTransactionalMessageListener

    companion object {

        @JvmStatic
        private val log: Logger = LoggerFactory.getLogger(
            AbstractRequestResponseJmsTest::class.java)

        @JvmStatic
        val SERVER_SIDE_INBOUND_REQUEST = "service.inbound.request"

        @JvmStatic
        val SERVER_SIDE_OUTBOUND_RESPONSE = "service.outbound.response"

        @JvmStatic
        val CLIENT_SIDE_OUTBOUND_REQUEST = SERVER_SIDE_INBOUND_REQUEST

        @JvmStatic
        val CLIENT_SIDE_INBOUND_RESPONSE = SERVER_SIDE_OUTBOUND_RESPONSE
    }
}
