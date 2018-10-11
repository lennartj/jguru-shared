/*-
 * #%L
 * Nazgul Project: jguru-shared-algorithms-api
 * %%
 * Copyright (C) 2018 jGuru Europe AB
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
package se.jguru.shared.algorithms.api.messaging

import org.slf4j.LoggerFactory
import se.jguru.shared.algorithms.api.Validate
import java.io.Serializable
import java.util.SortedMap
import javax.naming.Context
import javax.naming.InitialContext
import javax.naming.NamingException

/**
 * Algorithms for performing frequently occurring operations on JNDI Contexts.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
object Contexts {

    // Our logger
    private val log = LoggerFactory.getLogger(Contexts::class.java)

    /**
     * Retrieves the (initial) JNDI Context of the current container - or null in case it was not retrieved.
     *
     * @param contextParameters The parameters to the InitialContext constructor.
     * @return The (initial) Context, or null if none could be acquired.
     * @see InitialContext
     */
    @JvmStatic
    @Throws(IllegalArgumentException::class)
    fun getJndiContext(contextParameters: SortedMap<String, Any> = sortedMapOf()): Context {

        try {

            if (log.isDebugEnabled) {
                log.debug("Retrieving InitialContext configured by: " + contextParameters.entries
                    .stream()
                    .map { e -> "[" + e.key + "]: " + e.value }
                    .reduce { l, r -> l + ", " + r }
                    .orElse("<no parameters>"))
            }

            return if (contextParameters.isEmpty()) {

                // Use the parameter-less mode of getting the initial context.
                InitialContext()

            } else {

                // Copy all inbound parameters into a Hashmap,
                // since the InitialContext class requires a Hashmap
                // as argument.
                val params = java.util.Hashtable<String, Any>()
                contextParameters.putAll(params)

                // Use the parameter-injected mode of getting the initial context.
                InitialContext(params)
            }

        } catch (e: Exception) {
            throw IllegalArgumentException("Could not acquire JNDI Context", e)
        }
    }

    /**
     * Retrieves the (initial) JNDI Context of the current container - or null in case it was not retrieved.
     *
     * @return The (initial) Context, or null if none could be acquired.
     */
    @JvmStatic
    fun getDefaultJndiContext(): Context = getJndiContext(sortedMapOf())

    /**
     * Convenience method which looks up an object at the supplied JNDI lookup string, and casts the result to the
     * supplied class before returning.
     *
     * @param context      The JNDI Context, or null to use the default JNDI Context.
     * @param jndiLookupString The JNDI lookup string to use.
     * @param expectedType     The expected type of the object retrieved from the JNDI Context.
     * @param [T]              The type expected.
     * @return The object - of type T - if found within the supplied JNDI Context at the given jndiLookupString
     */
    @JvmStatic
    fun <T> lookup(context: Context = getDefaultJndiContext(),
                   jndiLookupString: String,
                   expectedType: Class<T>): T? {

        // #0) Check sanity
        Validate.notEmpty(jndiLookupString, "jndiLookupString")
        Validate.notNull(expectedType, "expectedType")

        try {

            // Find the object bound at the supplied lookup String.
            val lookup = context.lookup(jndiLookupString)

            if (log.isDebugEnabled) {

                val objectType: String = when (lookup) {
                    null -> "<null>"
                    else -> lookup::class.java.name
                }

                log.debug("Found object of type [$objectType] at key [$jndiLookupString]")
            }

            // All Done.
            return expectedType.cast(lookup)

        } catch (e: NamingException) {
            log.error("JNDI lookup failed for [$jndiLookupString].", e)
        } catch (e: Exception) {
            log.error("Could not find an object of type [${expectedType.name}] at lookup [$jndiLookupString]", e)
        }

        // Nah.
        return null
    }

    /**
     * Binds the supplied Serializable object directly into the supplied JNDI context.
     *
     * @param jndiContext The Context in which to bind the object.
     * @param key         The JDNI key
     * @param value       The value to bind
     * @param <T>         The type of value to bind.
     */
    @JvmStatic
    fun <T : Serializable> put(jndiContext: Context, key: String, value: T) {

        // Check sanity
        val ctx = Validate.notNull(jndiContext, "jndiContext")
        val toBind = Validate.notNull(value, "value")
        val theKey = Validate.notEmpty(key, "key")

        try {
            ctx.bind(theKey, toBind)
        } catch (e: NamingException) {
            throw IllegalArgumentException("Could not bind object of type [${value.javaClass.name}] " +
                "to key [$theKey]", e)
        }
    }
}
