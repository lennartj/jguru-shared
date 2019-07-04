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

import org.junit.After
import org.junit.Before
import org.slf4j.LoggerFactory
import javax.jms.Connection
import javax.jms.ConnectionFactory
import javax.jms.JMSException
import javax.jms.Session

/**
 * Abstract superclass for JMS-based tests.
 *
 * @param useTransactions if `true`, all retrieved sessions will be transacted by default.
 * @param broker The [MessageBroker] implementation used within this AbstractJmsTest.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
abstract class AbstractJmsTest(
    val useTransactions: Boolean = false,
    val broker: MessageBroker) {

    // Our Logger
    private val log = LoggerFactory.getLogger(AbstractJmsTest::class.java)

    // Internal state
    private var connectionFactory: ConnectionFactory? = null

    /**
     * Checks if this AbstractJmsTest is initialized, meaning that JMS objects can be used properly.
     */
    val isInitialized: Boolean get() = connectionFactory != null

    /**
     * Starts the JMS BrokerService.
     *
     * @throws JMSException if any of the underlying JMS methods does.
     */
    @Before
    @Throws(JMSException::class)
    fun startJmsBroker() {

        try {

            broker.startBroker()

        } catch (e: Exception) {
            log.error("Could not start Broker [${broker.name}]. Bailing out.")
            throw IllegalStateException(e)
        }

        // Acquire the ConnectionFactory
        this.connectionFactory = broker.getConnectionFactory(broker.getMessageServerURI())

        // Delegate to normal set up.
        afterStartJmsBroker()

        // Now setup JMS service-side clients.
        setupServices()
    }

    /**
     * Stops the JMS BrokerService.
     *
     * @throws JMSException if any of the underlying JMS methods does.
     */
    @After
    @Throws(JMSException::class)
    fun stopJmsBroker() {

        // Tear down JMS service-side clients.
        tearDownServices()

        // Delegate to normal teardown.
        beforeStopJmsBroker()

        try {
            broker.stopBroker()
        } catch (e: Exception) {
            log.error("Could not stop Broker [${broker.name}].", e)
            throw IllegalStateException(e)
        }
    }

    /**
     * @return a started JMS Connection to the Broker using the known connectionFactory.
     * @throws JMSException if the connection could not be created or started.
     */
    fun createConnection(): Connection {

        // Check sanity
        validateInitialized("Connection")

        // Create *and start* the connection
        val toReturn = connectionFactory!!.createConnection()
        toReturn.start()

        return toReturn
    }

    /**
     * Creates a JMS session from the supplied connection.
     *
     * @param connection The connection used to create a session.
     * @param transacted if `true`, the retrieved session is transacted. Defaults to the `useTransactions` value.
     * @return A JMS session from the supplied connection.
     * @throws JMSException if the createSession operation failed.
     */
    @Throws(JMSException::class)
    @JvmOverloads
    fun createSession(connection: Connection, transacted: Boolean = useTransactions): Session {

        // Check sanity
        validateInitialized("Session")

        // Apply the appropriate acknowledgement.
        val acknowledge = when (transacted) {
            true -> Session.SESSION_TRANSACTED
            else -> Session.AUTO_ACKNOWLEDGE
        }

        // All done.
        return connection.createSession(transacted, acknowledge)
    }

    /**
     * Override this method to perform any normal setup
     * after the Broker is launched. For example, any classes
     * which should be registered with the broker should be
     * instantiated here.
     */
    protected open fun afterStartJmsBroker() {
        // Override this method to create the JMS server
        // implementation and register it into the broker.
    }

    /**
     * Override this method to perform any normal tear-down
     * before the Broker is stopped. You might cleanup any
     * instances which should be de-registered from the broker.
     */
    protected open fun beforeStopJmsBroker() {
        // Override this method to destroy the JMS server
        // implementation and de-register it from the broker.
    }

    /**
     * Implement this method to setup any Services (i.e. server-side
     * listeners) that should be active and connected for the test.
     *
     * @throws JMSException if the underlying operations throws a JMSException
     */
    abstract fun setupServices()

    /**
     * Implement this method to tear down any Services
     * that have been active and connected during the test.
     *
     * @throws JMSException if the underlying operations throws a JMSException
     */
    abstract fun tearDownServices()

    /**
     * Validates that the state of this AbstractJmsTest is initialized, implying that the ConnectionFactory is not null.
     */
    private fun validateInitialized(description: String) {
        if (!isInitialized) {
            throw IllegalStateException("Cannot acquire $description before starting MessageBroker [${broker.name}].")
        }
    }
}
