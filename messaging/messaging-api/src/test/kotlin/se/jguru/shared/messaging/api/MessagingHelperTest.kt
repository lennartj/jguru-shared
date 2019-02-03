package se.jguru.shared.messaging.api

import org.apache.activemq.artemis.junit.EmbeddedJMSResource
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import se.jguru.shared.algorithms.api.messaging.JmsCompliantMap

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class MessagingHelperTest {

    @get:Rule
    val artemis = EmbeddedJMSResource();
    lateinit var client : ArtemisTestClient

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
    fun validateSendingMessages() {

        // Assemble
        val queueName = "a.queue"
        val unitUnderTest = JmsSessionMessagingHelper(client.session!!)

        val props = JmsCompliantMap()
        props["foo"] = "bar"

        val queue = unitUnderTest.createQueue(queueName)

        // Act
        val messageID = unitUnderTest.send(props, "this is a string body", queue)

        // Assert
        Assert.assertNotNull(messageID)
    }
}