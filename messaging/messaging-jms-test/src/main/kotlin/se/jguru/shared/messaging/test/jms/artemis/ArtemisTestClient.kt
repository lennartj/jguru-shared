/*-
 * #%L
 * Nazgul Project: jguru-shared-messaging-jms-test
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
package se.jguru.shared.messaging.test.jms.artemis

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory
import org.apache.activemq.artemis.jms.client.ActiveMQDestination
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import jakarta.jms.BytesMessage
import jakarta.jms.Connection
import jakarta.jms.ConnectionFactory
import jakarta.jms.JMSException
import jakarta.jms.MapMessage
import jakarta.jms.Message
import jakarta.jms.MessageProducer
import jakarta.jms.ObjectMessage
import jakarta.jms.Session
import jakarta.jms.StreamMessage
import jakarta.jms.TextMessage

private val log: Logger = LoggerFactory.getLogger(ArtemisTestClient::class.java)

/**
 * Simple client implementation which can be used to talk to an Artemis Broker in unit test scope.
 *
 * @param vmURL The URL used by the [ActiveMQConnectionFactory], such as "vm://0" or "tcp://localhost:61616".
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
open class ArtemisTestClient @JvmOverloads constructor(vmURL: String, transactedSession: Boolean = false) {

  var connectionFactory: ConnectionFactory = ActiveMQConnectionFactory(vmURL)
  var connection: Connection?
  var session: Session?
  var producer: MessageProducer?

  init {
    try {

      connection = connectionFactory.createConnection()
      session = when (transactedSession) {
        false -> connection!!.createSession()
        else -> connection!!.createSession(true, Session.AUTO_ACKNOWLEDGE)
      }
      producer = session!!.createProducer(null)
      connection!!.start()

    } catch (jmsEx: JMSException) {
      throw IllegalStateException("InternalClient creation failure", jmsEx)
    }
  }

  /**
   * Shuts down the MessageProducer, Session and Connection.
   */
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

  /**
   * Creates a BytesMessage without any body.
   */
  fun createBytesMessage(): BytesMessage {
    checkSession()

    try {
      return session!!.createBytesMessage()
    } catch (jmsEx: JMSException) {
      throw IllegalStateException("Failed to create BytesMessage", jmsEx)
    }

  }

  @Suppress("KotlinConstantConditions")
  fun <T : Message> createEmptyMessage(typeToCreate: Class<T>): T {

    checkSession()

    val permittedTypes = arrayOf(TextMessage::class.java,
                                 MapMessage::class.java,
                                 ObjectMessage::class.java,
                                 BytesMessage::class.java,
                                 StreamMessage::class.java)

    val theSession = session!!

    return when (typeToCreate) {
      is TextMessage -> theSession.createTextMessage()
      is MapMessage -> theSession.createMapMessage()
      is ObjectMessage -> theSession.createObjectMessage()
      is BytesMessage -> theSession.createBytesMessage()
      is StreamMessage -> theSession.createStreamMessage()
      else -> {

        val okTypes = permittedTypes
          .map { it.name }
          .reduce { acc, s -> "$acc, $s" }

        throw IllegalArgumentException("Cannot create a JMS message of type [${typeToCreate::class.java.name}." +
                                         "Permitted types: $okTypes")
      }
    } as T
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

  @JvmOverloads
  fun pushMessage(destination: ActiveMQDestination, message: Message, commitSession: Boolean = true) {
    if (producer == null) {
      throw IllegalStateException("JMS MessageProducer is null - has the InternalClient been started?")
    }

    try {
      producer!!.send(destination, message)
    } catch (jmsEx: JMSException) {
      throw IllegalStateException("Failed to push ${message::class.java.simpleName} to $destination.", jmsEx)
    }

    if (session != null && session!!.transacted && commitSession) {
      try {
        session!!.commit()
      } catch (e: Exception) {
        throw IllegalStateException("Failed to commit JMS Session", e)
      }
    }
  }

  private fun checkSession() {
    if (session == null) {
      throw IllegalStateException("JMS Session is null - has the InternalClient been started?")
    }
  }
}
