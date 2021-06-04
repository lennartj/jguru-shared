package se.jguru.shared.messaging.api

import org.apache.activemq.artemis.junit.EmbeddedActiveMQResource
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.jms.Message

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class MessagingHelperTest {

    private val log : Logger = LoggerFactory.getLogger(MessagingHelperTest::class.java)

    @get:Rule
    val artemis = EmbeddedActiveMQResource();
    lateinit var client: ArtemisTestClient

    @Before
    fun setupSharedState() {
        client = ArtemisTestClient(artemis.vmURL)
    }

    @After
    fun teardownSharedState() {
        client.stop()
    }

    @Test
    fun validateCreatingDestinations() {

        // Assemble
        val queueName = "some.queue"
        val topicName = "some.topic"
        val unitUnderTest = JmsSessionMessagingHelper(client.session!!)

        // Act
        val someQueue = unitUnderTest.createQueue(queueName)
        val someTopic = unitUnderTest.createTopic(topicName)

        // Assert
        Assert.assertNotNull(someQueue)
        Assert.assertNotNull(someTopic)

        Assert.assertEquals(queueName, someQueue.queueName)
        Assert.assertEquals(topicName, someTopic.topicName)
    }

    @Test
    fun validateCreatingDestinationsFromJmsContext() {

        // Assemble
        val queueName = "some.queue"
        val topicName = "some.topic"
        val unitUnderTest = JmsContextMessagingHelper(client.connectionFactory.createContext())

        // Act
        val someQueue = unitUnderTest.createQueue(queueName)
        val someTopic = unitUnderTest.createTopic(topicName)

        // Assert
        Assert.assertNotNull(someQueue)
        Assert.assertNotNull(someTopic)

        Assert.assertEquals(queueName, someQueue.queueName)
        Assert.assertEquals(topicName, someTopic.topicName)
    }

    @Test
    fun validateSendingMessages() {

        // Assemble
        val queueName = "a.queue"
        val unitUnderTest = JmsSessionMessagingHelper(client.session!!)

        val props = JmsCompliantMap()
        props["foo"] = "bar"

        val queue = unitUnderTest.createQueue(queueName)

        // Act
        val messageID = unitUnderTest.sendMessage(props, "this is a string body", queue)

        // Assert
        Assert.assertNotNull(messageID)
    }

    @Test
    fun validateSendingMessagesUsingJmsContext() {

        // Assemble
        val queueName = "a.queue"
        val unitUnderTest = JmsContextMessagingHelper(client.connectionFactory.createContext())

        val props = JmsCompliantMap()
        props["foo"] = "bar"

        val queue = unitUnderTest.createQueue(queueName)

        // Act
        val messageID = unitUnderTest.sendMessage(props, "this is a string body", queue)

        // Assert
        Assert.assertNotNull(messageID)
    }
}