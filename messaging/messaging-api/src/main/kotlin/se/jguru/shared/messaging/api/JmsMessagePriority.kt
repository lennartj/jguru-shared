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

/**
 * The JMS API defines ten levels of priority value, with 0 as the lowest
 * priority and 9 as the highest. Clients should consider priorities 0-4 as
 * gradations of normal priority and priorities 5-9 as gradations of
 * expedited priority. Priority is set to 4 by default.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
enum class JmsMessagePriority(val jmsStandardValue : Int) {

    LOWEST(0),

    LOWER(1),

    LOW(2),

    DEFAULT(4),

    HIGH(7),

    HIGHER(8),

    HIGHEST(9)
}
