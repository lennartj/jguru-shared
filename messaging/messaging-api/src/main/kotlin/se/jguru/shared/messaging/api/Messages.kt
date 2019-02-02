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

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import se.jguru.shared.algorithms.api.messaging.JmsCompliantMap
import java.beans.IntrospectionException
import java.beans.Introspector
import java.util.Collections
import javax.jms.JMSException
import javax.jms.Message

private val log: Logger = LoggerFactory.getLogger(Messages::class.java)

/**
 * Copies all properties from the supplied JmsCompliantMap to the given target Message.
 *
 * @see Messages.copyProperties
 */
fun JmsCompliantMap.copyPropertiesTo(target: Message) = Messages.copyProperties(target, this)

/**
 * Helper algorithms for manipulating JMS Messages.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
object Messages {

    /**
     * Copies all properties from the supplied JmsCompliantMap to the given target Message.
     *
     * @param target     A JMS outbound Message (i.e. created by the JMSContext for sending).
     * @param properties The JmsCompliantMap containing the JMS properties to copy into the supplied target [Message].
     */
    @JvmStatic
    fun copyProperties(target: Message, properties: JmsCompliantMap) {

        // Set all available properties as JMS Object Properties.
        properties.forEach { key, value ->

            try {
                target.setObjectProperty(key, value)
            } catch (e: JMSException) {
                log.warn("Could not set a JMS Property [$key] of type [${value::class.java.simpleName}]. Ignoring it.", e)
            }
        }

        // Copy default JMS properties by firing all Setters within
        // the Message class where we have a non-null property.
        try {

            Introspector.getBeanInfo(Message::class.java)
                .propertyDescriptors
                .filter { it.writeMethod != null }
                .filter { !"class".equals(it.name, true) } // Ignore 'setClass' PropertyDescriptors.
                .forEach { pd ->

                    val value = properties[pd.name]
                    if (value != null) {

                        // Only attempt to set a non-null value
                        val setter = pd.writeMethod
                        try {
                            setter.invoke(target, value)
                        } catch (e: Exception) {
                            log.warn("Could not assign value [" + value + "] to Message using setter [" + setter.name + "]", e)
                        }
                    }
                }

        } catch (e: IntrospectionException) {
            log.error("Could not perform Introspection of Message class", e)
        }
    }

    /**
     * Extracts a JmsCompliantMap containing all (relevant) JMS Properties within the supplied jmsMessage.
     *
     * @param jmsMessage A non-null JMS Message from which all JMS properties should be extracted.
     * @return a JmsCompliantMap containing all properties found within the supplied Message.
     */
    @JvmStatic
    fun getPropertyMap(jmsMessage: Message): JmsCompliantMap {

        val toReturn = JmsCompliantMap()

        try {

            // Extract all non-standard JMS headers
            val nonDefaultHeaderNames = Collections.list<Any>(jmsMessage.propertyNames)
            for (current in nonDefaultHeaderNames) {
                val currentKey = current as String
                toReturn[currentKey] = jmsMessage.getObjectProperty(currentKey)
            }

            Introspector.getBeanInfo(Message::class.java)
                .propertyDescriptors
                .filter { it.readMethod != null }
                .filter { !"class".equals(it.name, ignoreCase = true) } // Ignore 'getClass' PropertyDescriptors.
                .forEach { pd ->

                    val getter = pd.readMethod
                    val result = getter.invoke(jmsMessage)

                    // Add the property only if it is not null and compliant with the JMS specification.
                    if (JmsCompliantMap.isCompliantValue(result)) {
                        toReturn[pd.name] = result
                    }
                }

        } catch (e: Exception) {
            throw IllegalArgumentException("Could not extract properties from JMS message", e)
        }

        // All Done.
        return toReturn
    }
}
