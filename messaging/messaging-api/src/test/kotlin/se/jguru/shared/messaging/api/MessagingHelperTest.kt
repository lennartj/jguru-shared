package se.jguru.shared.messaging.api

import org.apache.activemq.artemis.api.core.TransportConfiguration
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient
import org.apache.activemq.artemis.api.jms.JMSFactoryType
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMAcceptorFactory
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory
import org.apache.activemq.artemis.core.remoting.impl.invm.TransportConstants
import org.apache.activemq.artemis.core.server.ActiveMQServer
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger
import javax.jms.Connection
import javax.jms.JMSContext
import javax.jms.Session


/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class MessagingHelperTest {
    /*

    private val log : Logger = LoggerFactory.getLogger(MessagingHelperTest::class.java)

    private val testIndex = AtomicInteger(2000)

    lateinit var service : ActiveMQServer
    lateinit var connectionFactory: ActiveMQConnectionFactory

    lateinit var connection: Connection
    lateinit var session : Session
    lateinit var jmsContext : JMSContext

    @BeforeEach
    fun setupTestArtemisService() {

        val vmURL = "vm://${testIndex.incrementAndGet()}"

        // #1) Setup transport configuration
        //
        val params: MutableMap<String, Any> = HashMap()
        params[TransportConstants.SERVER_ID_PROP_NAME] = "${testIndex.get()}"

        val transportConfig = TransportConfiguration(InVMConnectorFactory::class.java.name, params)

        connectionFactory = ActiveMQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, transportConfig)
        connectionFactory.clientID = "testClient-${testIndex.get()}"

        jmsContext = connectionFactory.createContext()
        connection = connectionFactory.createConnection()
        session = connection.createSession()
        connection.start()
        jmsContext.start()
    }

    @AfterEach
    fun teardownTestArtemisService() {
        session.close()
        connection.close()
        jmsContext.close()
        connectionFactory.close()
    }

    @Test
    fun validateCreatingDestinations() {

        // Assemble
        val queueName = "some.queue"
        val topicName = "some.topic"
        val unitUnderTest = JmsSessionMessagingHelper(session)

        // Act
        val someQueue = unitUnderTest.createQueue(queueName)
        val someTopic = unitUnderTest.createTopic(topicName)

        // Assert
        assertThat(someQueue).isNotNull
        assertThat(someTopic).isNotNull

        assertThat(someQueue.queueName).isEqualTo(queueName)
        assertThat(someTopic.topicName).isEqualTo(topicName)
    }

    @Test
    fun validateCreatingDestinationsFromJmsContext() {

        // Assemble
        val queueName = "some.queue"
        val topicName = "some.topic"
        val unitUnderTest = JmsContextMessagingHelper(jmsContext)

        // Act
        val someQueue = unitUnderTest.createQueue(queueName)
        val someTopic = unitUnderTest.createTopic(topicName)

        // Assert
        assertThat(someQueue).isNotNull
        assertThat(someTopic).isNotNull

        assertThat(someQueue.queueName).isEqualTo(queueName)
        assertThat(someTopic.topicName).isEqualTo(topicName)
    }

    @Test
    fun validateSendingMessages() {

        // Assemble
        val queueName = "a.queue"
        val unitUnderTest = JmsSessionMessagingHelper(session)

        val props = JmsCompliantMap()
        props["foo"] = "bar"

        val queue = unitUnderTest.createQueue(queueName)

        // Act
        val messageID = unitUnderTest.sendMessage(props, "this is a string body", queue)

        // Assert
        assertThat(messageID).isNotNull
    }

    @Test
    fun validateSendingMessagesUsingJmsContext() {

        // Assemble
        val queueName = "a.queue"
        val unitUnderTest = JmsContextMessagingHelper(jmsContext)

        val props = JmsCompliantMap()
        props["foo"] = "bar"

        val queue = unitUnderTest.createQueue(queueName)

        // Act
        val messageID = unitUnderTest.sendMessage(props, "this is a string body", queue)

        // Assert
        assertThat(messageID).isNotNull
    }
     */
}