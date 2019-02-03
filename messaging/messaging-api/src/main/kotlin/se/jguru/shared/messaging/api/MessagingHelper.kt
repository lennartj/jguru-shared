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

import se.jguru.shared.algorithms.api.messaging.JmsCompliantMap
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.Serializable
import java.util.SortedMap
import java.util.TreeMap
import javax.jms.Destination
import javax.jms.JMSContext
import javax.jms.Queue
import javax.jms.Session
import javax.jms.Topic

/**
 * Simplified specification for how to create destinations and
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
     * Sends a message consisting of the supplied properties.
     *
     * @param props The property map to be copied into the JMS Message as header properties.
     * @param body An optional body, which is inserted as the JMS Message payload if non-null.
     * @param destination An optional destination to which the message should be sent.
     */
    abstract fun sendMessage(props: JmsCompliantMap, body: Any, destination: Destination): String
}

/**
 * DestinationHelper using a [Session] to acquire JMS objects from the broker.
 *
 * @param jmsSession The JMS Session from which to acquire the JMS bound objects.
 */
class JmsSessionMessagingHelper(val jmsSession: Session) : MessagingHelper() {

    override fun createQueue(name: String): Queue = jmsSession.createQueue(name)

    override fun createTopic(name: String): Topic = jmsSession.createTopic(name)

    override fun sendMessage(props: JmsCompliantMap, body: Any, destination: Destination): String =
        send(props, body, destination)

    /**
     * Main send method which uses the contained jmsSession to create all required properties.
     */
    @JvmOverloads
    fun send(props: JmsCompliantMap,
             body: Any? = null,
             destination: Destination,
             mode: JmsDeliveryMode = JmsDeliveryMode.NON_PERSISTENT,
             priority: JmsMessagePriority = JmsMessagePriority.DEFAULT): String {

        val toSend = when (body == null) {
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

                else -> throw IllegalArgumentException("Cannot create a JMS-compliant message or " +
                    "body of type [${body::class.java.name}]")
            }
        }

        // Copy the properties to the Message
        props.copyPropertiesTo(toSend)

        // Configure the producer
        val producer = jmsSession.createProducer(destination)
        producer.deliveryMode = mode.jmsStandardValue
        producer.priority = priority.jmsStandardValue

        // Send the message
        producer.send(toSend)

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

    override fun sendMessage(props: JmsCompliantMap, body: Any, destination: Destination): String =
        send(props, body, destination)

    /**
     * Main send method which uses the contained jmsContext to create all required properties.
     */
    @JvmOverloads
    fun send(props: JmsCompliantMap,
             body: Any? = null,
             destination: Destination,
             mode: JmsDeliveryMode = JmsDeliveryMode.NON_PERSISTENT,
             priority: JmsMessagePriority = JmsMessagePriority.DEFAULT): String {

        val toSend = when (body == null) {
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

                else -> throw IllegalArgumentException("Cannot create a JMS-compliant message or " +
                    "body of type [${body::class.java.name}]")
            }
        }

        // Copy the properties to the Message
        props.copyPropertiesTo(toSend)

        // Configure the producer
        val producer = jmsContext.createProducer()
        producer.deliveryMode = mode.jmsStandardValue
        producer.priority = priority.jmsStandardValue

        // Send the message
        producer.send(destination, toSend)

        // ... and return the MessageID
        return toSend.jmsMessageID
    }
}
