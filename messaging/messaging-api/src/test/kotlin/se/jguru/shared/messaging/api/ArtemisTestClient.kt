package se.jguru.shared.messaging.api

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import jakarta.jms.BytesMessage
import jakarta.jms.Connection
import jakarta.jms.ConnectionFactory
import jakarta.jms.DeliveryMode
import jakarta.jms.JMSException
import jakarta.jms.MapMessage
import jakarta.jms.Message
import jakarta.jms.MessageProducer
import jakarta.jms.ObjectMessage
import jakarta.jms.Session
import jakarta.jms.StreamMessage
import jakarta.jms.TextMessage
import org.apache.activemq.artemis.jms.client.ActiveMQDestination
import org.apache.activemq.artemis.jms.client.ActiveMQJMSConnectionFactory

private val log : Logger = LoggerFactory.getLogger(ArtemisTestClient::class.java)

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
open class ArtemisTestClient(vmURL : String) {

    var connectionFactory: ConnectionFactory = ActiveMQJMSConnectionFactory(vmURL)
    var connection: Connection?
    var session: Session?
    var producer: MessageProducer?

    init {
        try {

            connection = connectionFactory.createConnection()
            session = connection!!.createSession(Session.AUTO_ACKNOWLEDGE)

            producer = session!!.createProducer(null)
            producer!!.deliveryMode = DeliveryMode.NON_PERSISTENT

            connection!!.start()

        } catch (jmsEx: JMSException) {
            throw IllegalStateException("InternalClient creation failure", jmsEx)
        }
    }

    fun stop() {
        try {
            producer!!.close()
        } catch (jmsEx: JMSException) {
            log.warn("JMSException encounter closing InternalClient Session - MessageProducer", jmsEx)
        } finally {
            producer = null
        }

        try {
            session!!.close()
        } catch (jmsEx: JMSException) {
            log.warn("JMSException encounter closing InternalClient Session - ignoring", jmsEx)
        } finally {
            session = null
        }

        if (null != connection) {
            try {
                connection!!.close()
            } catch (jmsEx: JMSException) {
                log.warn("JMSException encounter closing InternalClient Connection - ignoring", jmsEx)
            } finally {
                connection = null
            }
        }
    }

    fun createBytesMessage(): BytesMessage {
        checkSession()

        try {
            return session!!.createBytesMessage()
        } catch (jmsEx: JMSException) {
            throw IllegalStateException("Failed to create BytesMessage", jmsEx)
        }

    }

    fun createTextMessage(): TextMessage {
        checkSession()

        try {
            return session!!.createTextMessage()
        } catch (jmsEx: JMSException) {
            throw IllegalStateException("Failed to create TextMessage", jmsEx)
        }

    }

    fun createMapMessage(): MapMessage {
        checkSession()

        try {
            return session!!.createMapMessage()
        } catch (jmsEx: JMSException) {
            throw IllegalStateException("Failed to create MapMessage", jmsEx)
        }

    }

    fun createObjectMessage(): ObjectMessage {
        checkSession()

        try {
            return session!!.createObjectMessage()
        } catch (jmsEx: JMSException) {
            throw IllegalStateException("Failed to create ObjectMessage", jmsEx)
        }

    }

    fun createStreamMessage(): StreamMessage {
        checkSession()
        try {
            return session!!.createStreamMessage()
        } catch (jmsEx: JMSException) {
            throw IllegalStateException("Failed to create StreamMessage", jmsEx)
        }

    }

    fun pushMessage(destination: ActiveMQDestination, message: Message) {
        if (producer == null) {
            throw IllegalStateException("JMS MessageProducer is null - has the InternalClient been started?")
        }

        try {
            producer!!.send(destination, message)
        } catch (jmsEx: JMSException) {
            throw IllegalStateException(String.format("Failed to push %s to %s", message.javaClass.simpleName, destination.toString()), jmsEx)
        }

    }

    fun checkSession() {
        if (session == null) {
            throw IllegalStateException("JMS Session is null - has the InternalClient been started?")
        }
    }
}