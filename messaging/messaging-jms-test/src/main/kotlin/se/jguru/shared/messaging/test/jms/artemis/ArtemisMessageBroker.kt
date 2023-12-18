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

import org.apache.activemq.artemis.api.core.TransportConfiguration
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl
import org.apache.activemq.artemis.core.registry.MapBindingRegistry
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptorFactory
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory
import org.apache.activemq.artemis.jms.server.config.impl.ConnectionFactoryConfigurationImpl
import org.apache.activemq.artemis.jms.server.config.impl.JMSConfigurationImpl
import org.apache.activemq.artemis.jms.server.config.impl.JMSQueueConfigurationImpl
import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS
import org.apache.activemq.artemis.spi.core.naming.BindingRegistry
import se.jguru.shared.messaging.test.jms.MessageBroker
import java.io.File
import java.util.HashSet
import java.util.SortedMap
import java.util.TreeMap
import jakarta.jms.ConnectionFactory

/**
 * MessageBroker implementation for Apache Artemis.
 */
class ArtemisMessageBroker @JvmOverloads constructor(
    brokerName: String = DEFAULT_BROKERNAME,
    configurationDirectory: String = DEFAULT_CONFIGURATION_DIRECTORY) : MessageBroker {

  // Internal state
  val jmsServer: EmbeddedJMS
  val brokerName: String
  val registry: BindingRegistry
  val isStarted: Boolean get() = (jmsServer as EmbeddedJMS?)?.jmsServerManager?.isStarted ?: false

  init {

    // a) Create the Artemis configuration
    //
    this.registry = MapBindingRegistry()
    val configuration = ConfigurationImpl()
    configuration.isPersistenceEnabled = false
    configuration.isSecurityEnabled = false
    configuration.journalDirectory = getTargetDirectory().absolutePath + File.separatorChar + configurationDirectory

    val journalDirectory = File(configuration.journalDirectory)
    if(!journalDirectory.exists()) {
      journalDirectory.mkdirs()
    }

    // b) Add a transport. Map it as an acceptor and a connector (using the same id).
    //
    val connectorId = "connector"
    val transportConfigurations = HashSet<TransportConfiguration>()
    transportConfigurations.add(TransportConfiguration(NettyAcceptorFactory::class.java.name))

    configuration.acceptorConfigurations = transportConfigurations
    configuration.connectorConfigurations[connectorId] = TransportConfiguration(NettyConnectorFactory::class.java.name)

    // c) Create the JMS configuration
    //
    val jmsConfig = JMSConfigurationImpl()
    jmsConfig.connectionFactoryConfigurations.add(ConnectionFactoryConfigurationImpl()
                                                    .setName(CONFIGURATION_FACTORY_ID)
                                                    .setHA(false)
                                                    .setConnectorNames(connectorId)
                                                    .setBindings("/connection/$CONFIGURATION_FACTORY_ID"))


    val queueConfigurations = jmsConfig.queueConfigurations
    UNIT_TEST_QUEUES.entries.forEach {

      // Create a non-persistent queue without any selectors.
      queueConfigurations.add(
        JMSQueueConfigurationImpl().setName(it.key).setSelector(null).setDurable(false).setBindings(it.value)
      )
    }

    // d) Create the server and assign the broker name
    jmsServer = EmbeddedJMS()
    jmsServer.registry = this.registry
    jmsServer.setConfiguration(configuration)
    jmsServer.setJmsConfiguration(jmsConfig)

    this.brokerName = brokerName
  }

  override val name: String = "Artemis"

  override fun startBroker() {
    jmsServer.start()
    jmsServer.jmsServerManager.activeMQServer.identity = brokerName
  }

  override fun stopBroker() {
    when (isStarted && jmsServer.jmsServerManager != null) {
      true -> jmsServer.stop()
      else -> throw IllegalStateException("Cowardly refusing to stop Broker before starting it.")
    }
  }

  override fun getMessageServerURI(): String = "Irrelevant for Artemis"

  override fun getConnectionFactory(configuration: String): ConnectionFactory {
    return jmsServer.lookup("/connection/$CONFIGURATION_FACTORY_ID") as ConnectionFactory
  }

  companion object {

    /**
     * Default Broker identity.
     */
    @JvmStatic
    val DEFAULT_BROKERNAME = "AbstractArtemisJmsTest_Broker"

    /**
     * Default configuration directory.
     */
    @JvmStatic
    val DEFAULT_CONFIGURATION_DIRECTORY = "artemis/plainconfig"

    /**
     * Default registry name of the hornetq jms ConfigurationFactory.
     */
    @JvmStatic
    val CONFIGURATION_FACTORY_ID = "UnitTestConnectionFactory"

    /**
     * A map relating all pre-defined queue names/ids to their bindings (i.e. JNDI key).
     */
    @JvmStatic
    val UNIT_TEST_QUEUES: SortedMap<String, String> = TreeMap()
      get() {

        if (field.isEmpty()) {
          field["clientRequestQueue"] = "/queue/client/outboundRequest"
          field["clientResponseQueue"] = "/queue/client/inboundResponse"
          field["serverRequestQueue"] = "/queue/server/inboundRequest"
          field["serverResponseQueue"] = "/queue/server/outboundResponse"
        }

        // All Done.
        return field
      }

    /**
     * Retrieves a File to the target directory.
     *
     * @return the project target directory path, wrapped in a File object.
     */
    @JvmStatic
    fun getTargetDirectory(): File {

      // Use CodeSource
      val location = ArtemisMessageBroker::class.java.protectionDomain.codeSource.location
        ?: throw NullPointerException("CodeSource location not found for " +
                                        "class [${ArtemisMessageBroker::class.java.simpleName}]")

      return File(location.path).parentFile
    }
  }
}
