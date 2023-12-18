/*-
 * #%L
 * Nazgul Project: jguru-shared-messaging-api
 * %%
 * Copyright (C) 2018 - 2023 jGuru Europe AB
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

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.Serializable
import java.util.SortedMap
import java.util.TreeMap
import jakarta.jms.Destination
import jakarta.jms.JMSContext
import jakarta.jms.Message
import jakarta.jms.Queue
import jakarta.jms.Session
import jakarta.jms.Topic

/**
 * Simplified helper assisting in creating destinations and sending messages.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
abstract class MessagingHelper {

    /**
     * Creates a Queue with the supplied name.
     *
     * @param name The name of the Queue to create.
     */
    abstract fun createQueue(name: String): Queue

    /**
     * Creates a Topic with the supplied name.
     *
     * @param name The name of the Topic to create.
     */
    abstract fun createTopic(name: String): Topic

    /**
     * Specification of how to create a JMS Message able to harbour the supplied body.
     *
     * @param body The body intended for transport within the resulting message.
     * @throws IllegalArgumentException if the implementation could not figure out how to
     * transport the supplied body within a JMS Message.
     */
    @Throws(IllegalArgumentException::class)
    protected abstract fun createMessage(body: Any?): Message

    /**
     * Main send method which uses the contained jmsContext or jmsSession to send the message.
     *
     * @param props The property map to be copied into the JMS Message as header properties.
     * @param body An optional body, which is inserted as the JMS Message payload if non-null.
     * @param destination An optional destination to which the message should be sent.
     * @param mode The JmsDeliveryMode of the outbound Message.
     * @param priority The JmsMessagePriority of the outbound Message.
     * @param commitSession Attempt to perform a commit of the JMS Session if appropriate.
     * @return The JMS Message ID.
     */
    abstract fun sendMessage(
        props: JmsCompliantMap,
        body: Any? = null,
        destination: Destination,
        mode: JmsDeliveryMode = JmsDeliveryMode.NON_PERSISTENT,
        priority: JmsMessagePriority = JmsMessagePriority.DEFAULT,
        commitSession: Boolean = true,
        completionListener: DurationAwareCompletionListener = SimpleDurationMeasuringCompletionListener()
    ): String
}

/**
 * DestinationHelper using a [Session] to acquire JMS objects from the broker.
 *
 * @param jmsSession The JMS Session from which to acquire the JMS bound objects.
 */
class JmsSessionMessagingHelper(val jmsSession: Session) : MessagingHelper() {

    override fun createQueue(name: String): Queue = jmsSession.createQueue(name)

    override fun createTopic(name: String): Topic = jmsSession.createTopic(name)

    override fun createMessage(body: Any?): Message = when (body == null) {

        true -> jmsSession.createTextMessage()
        else -> when (body) {

            is String -> jmsSession.createTextMessage(body)

            is ByteArray -> {

                val toReturn = jmsSession.createBytesMessage()
                toReturn.writeBytes(body)
                toReturn
            }

            is InputStream -> {

                val toReturn = jmsSession.createStreamMessage()

                val tmpByteStream = ByteArrayOutputStream()
                body.copyTo(tmpByteStream)
                toReturn.writeBytes(tmpByteStream.toByteArray())

                toReturn
            }

            is Serializable -> jmsSession.createObjectMessage(body)

            is Map<*, *> -> {

                val toReturn = jmsSession.createMapMessage()
                val properMap: SortedMap<String, Any> = TreeMap()

                body.entries
                    .filter { it.key is String }
                    .forEach {
                        val key = it.key as String
                        properMap[key] = it.value
                    }

                // Write the map properties.
                Messages.writeToBody(properMap, toReturn)

                toReturn
            }

            else -> throw IllegalArgumentException(
                "Cannot create a JMS-compliant message or " +
                    "body of type [${body::class.java.name}]"
            )
        }
    }

    override fun sendMessage(
        props: JmsCompliantMap,
        body: Any?,
        destination: Destination,
        mode: JmsDeliveryMode,
        priority: JmsMessagePriority,
        commitSession: Boolean,
        completionListener: DurationAwareCompletionListener
    ): String {

        val toSend = createMessage(body)

        // Copy the properties to the Message
        props.copyPropertiesTo(toSend)

        // Configure the producer
        val producer = jmsSession.createProducer(destination)
        producer.deliveryMode = mode.jmsStandardValue
        producer.priority = priority.jmsStandardValue

        // Send the message
        completionListener.noteStart()
        producer.send(toSend, completionListener)

        // Commit the JMS Session if asked to.
        if (jmsSession.transacted && commitSession) {
            jmsSession.commit()
        }

        // ... and return the MessageID
        return toSend.jmsMessageID
    }
}

/**
 * DestinationHelper using a [JMSContext] to acquire JMS objects from the broker.
 *
 * @param jmsContext The JMSContext from which to acquire the JMS bound objects.
 */
class JmsContextMessagingHelper(val jmsContext: JMSContext) : MessagingHelper() {

    override fun createQueue(name: String): Queue = jmsContext.createQueue(name)

    override fun createTopic(name: String): Topic = jmsContext.createTopic(name)

    override fun createMessage(body: Any?): Message = when (body == null) {

        true -> jmsContext.createTextMessage()
        else -> when (body) {

            is String -> jmsContext.createTextMessage(body)

            is ByteArray -> {

                val toReturn = jmsContext.createBytesMessage()
                toReturn.writeBytes(body)
                toReturn
            }

            is InputStream -> {

                val toReturn = jmsContext.createStreamMessage()

                val tmpByteStream = ByteArrayOutputStream()
                body.copyTo(tmpByteStream)
                toReturn.writeBytes(tmpByteStream.toByteArray())

                toReturn
            }

            is Serializable -> jmsContext.createObjectMessage(body)

            is Map<*, *> -> {

                val toReturn = jmsContext.createMapMessage()
                val properMap: SortedMap<String, Any> = TreeMap()

                body.entries
                    .filter { it.key is String }
                    .forEach {
                        val key = it.key as String
                        properMap[key] = it.value
                    }

                // Write the map properties.
                Messages.writeToBody(properMap, toReturn)

                toReturn
            }

            else -> throw IllegalArgumentException(
                "Cannot create a JMS-compliant message or " +
                    "body of type [${body::class.java.name}]"
            )
        }
    }

    override fun sendMessage(
        props: JmsCompliantMap,
        body: Any?,
        destination: Destination,
        mode: JmsDeliveryMode,
        priority: JmsMessagePriority,
        commitSession: Boolean,
        completionListener: DurationAwareCompletionListener
    ): String {

        val toSend = createMessage(body)

        // Copy the properties to the Message
        props.copyPropertiesTo(toSend)

        // Configure the producer
        val producer = jmsContext.createProducer()
        producer.deliveryMode = mode.jmsStandardValue
        producer.priority = priority.jmsStandardValue
        producer.async = completionListener

        // Send the message
        completionListener.noteStart()
        producer.send(destination, toSend)

        // Commit the JMS Session if asked to.
        if (jmsContext.transacted && commitSession) {
            jmsContext.commit()
        }

        // ... and return the MessageID
        return toSend.jmsMessageID
    }
}
