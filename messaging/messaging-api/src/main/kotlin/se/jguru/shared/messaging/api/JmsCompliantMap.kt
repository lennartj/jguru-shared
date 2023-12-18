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

import java.util.TreeMap

/**
 * Map implementation which validates that keys and values supplied are in compliance with the JMS specification.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@Suppress("RemoveRedundantQualifierName")
class JmsCompliantMap : TreeMap<String, Any>() {

    /**
     * Validates that the value is one of the accepted types, before putting the key/value pair into this Map.
     *
     * @param key the map key
     * @param value the map entry value. Must be one of the permitted types [ACCEPTED_PROPERTY_TYPES]
     * @throws IllegalArgumentException if the value was not accepted (i.e. not one of the [ACCEPTED_PROPERTY_TYPES])
     */
    @Throws(IllegalArgumentException::class)
    override fun put(key: String, value: Any): Any? {

        val valueType = value.javaClass.name

        if (!isCompliantValue(value)) {

            val permittedTypes = ACCEPTED_PROPERTY_TYPES
                .map { c -> c.name }
                .sorted()
                .reduce { acc, s -> "$acc, $s" }

            throw IllegalArgumentException("JMS specification does not permit adding [$valueType] values. " +
                "Accepted types: $permittedTypes")
        }

        // All done.
        return super.put(key, value)
    }

    companion object {

        /**
         * Property value types which are compliant with the JMS specificiation.
         */
        @JvmStatic
        val ACCEPTED_PROPERTY_TYPES = listOf<Class<*>>(
            java.lang.Boolean::class.java,
            java.lang.Boolean.TYPE,
            java.lang.Byte::class.java,
            java.lang.Byte.TYPE,
            java.lang.Short::class.java,
            java.lang.Short.TYPE,
            java.lang.Character::class.java,
            java.lang.Character.TYPE,
            java.lang.Integer::class.java,
            java.lang.Integer.TYPE,
            java.lang.Long::class.java,
            java.lang.Long.TYPE,
            java.lang.Double::class.java,
            java.lang.Double.TYPE,
            java.lang.String::class.java)

        /**
         * Indicates if the supplied value object is compliant with the JMS specification's
         * definition for JMS property values.
         *
         * @param value The value to check for JMS type compliance.
         * @return `true` if the value is compliant with the JMS specification's permitted types
         * for JMS property values.
         *
         * @see ACCEPTED_PROPERTY_TYPES
         */
        @JvmStatic
        fun isCompliantValue(value: Any?): Boolean = when (value == null) {
            true -> false
            else -> ACCEPTED_PROPERTY_TYPES.stream().anyMatch { current -> value::class.java == current }
        }
    }
}
