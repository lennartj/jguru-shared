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

import se.jguru.shared.messaging.test.jms.AbstractJmsTest

/**
 * Abstract JMS test which uses an [ArtemisMessageBroker].
 */
abstract class AbstractArtemisTest @JvmOverloads constructor(
    useTransactions: Boolean = false,
    brokerName: String = ArtemisMessageBroker.DEFAULT_BROKERNAME,
    configurationDirectory: String = ArtemisMessageBroker.DEFAULT_CONFIGURATION_DIRECTORY
) : AbstractJmsTest(useTransactions, ArtemisMessageBroker(brokerName, configurationDirectory))
