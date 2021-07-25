package se.jguru.shared.messaging.api

import org.apache.activemq.artemis.junit.EmbeddedJMSResource
import org.junit.Assert
import org.junit.Rule
import org.junit.jupiter.api.Test
import java.util.TreeMap

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class MessagesTest {

    @get:Rule
    val artemis = EmbeddedJMSResource();

    @Test
    fun validateCopyingDataToAndFromMessage() {

        // Assemble
        val props = JmsCompliantMap()
        props["foo"] = "bar"
        props["meaningOfLife"] = 42L

        // Act
        val textMessage = artemis.createTextMessage()
        props.copyPropertiesTo(textMessage)

        // Assert
        val result = Messages.getPropertyMap(textMessage)
        Assert.assertEquals(result["foo"], "bar")
        Assert.assertEquals(result["meaningOfLife"], 42L)

        // println("Got: $result")

        /*
        Got: {
        JMSDeliveryMode=2,
        JMSDeliveryTime=0,
        JMSExpiration=0,
        JMSPriority=4,
        JMSRedelivered=false,
        JMSTimestamp=1549128518621,
        JMSXDeliveryCount=0,
        foo=bar,
        meaningOfLife=42
        }
         */
    }

    @Test
    fun validateExtractingPropertiesFromMapMessage() {

        // Assemble
        val props = TreeMap<String, Any>()
        props["string"] = "bar"
        props["long"] = 42L
        props["char"] = 'r'
        props["bytes"] = "foonbar".toByteArray()

        // Act
        val mapMessage = artemis.createMapMessage()
        Messages.writeToBody(props, mapMessage)

        val result = Messages.readFromBody(mapMessage)

        // Assert
        result.forEach { k, v -> Assert.assertEquals(v, props[k]) }
    }
}