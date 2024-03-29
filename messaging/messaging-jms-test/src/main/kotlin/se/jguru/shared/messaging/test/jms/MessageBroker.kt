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
package se.jguru.shared.messaging.test.jms

import jakarta.jms.ConnectionFactory

/**
 * Wrapper specification for a MessageBroker instance,
 * to be controlled for the purposes of automated tests.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
interface MessageBroker {

    /**
     * @return The human-readable name of this MessageBroker.
     */
    val name: String

    /**
     * Starts the MessageBroker.
     *
     * @throws Exception if the broker could not be properly started.
     */
    @Throws(Exception::class)
    fun startBroker()

    /**
     * Stops the MessageBroker.
     *
     * @throws Exception if the broker could not be properly stopped.
     */
    @Throws(Exception::class)
    fun stopBroker()

    /**
     * @return a broker connection URI, suited for unit tests or integration tests as required.
     */
    fun getMessageServerURI(): String

    /**
     * Retrieves a fully configured ConnectionFactory from the wrapped MessageBroker.
     *
     * @param configuration A configuration parameter to the broker for creating a ConnectionFactory.
     * @return a fully configured ConnectionFactory from the wrapped MessageBroker.
     */
    fun getConnectionFactory(configuration: String): ConnectionFactory
}
