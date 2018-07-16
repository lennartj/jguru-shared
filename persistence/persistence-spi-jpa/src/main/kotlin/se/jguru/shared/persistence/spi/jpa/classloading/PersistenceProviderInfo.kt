/*-
 * #%L
 * Nazgul Project: jguru-shared-persistence-spi-jpa
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
package se.jguru.shared.persistence.spi.jpa.classloading

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.Serializable
import java.util.TreeMap
import javax.persistence.EntityManagerFactory
import javax.persistence.Persistence
import javax.persistence.spi.PersistenceProvider
import javax.persistence.spi.PersistenceProviderResolverHolder

/**
 * Information holder for a PersistenceProvider.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
interface PersistenceProviderInfo : Serializable {

    /**
     * Retrieves the persistenceProvider class for this PersistenceProviderType.
     *
     * @return the persistenceProvider class for this PersistenceProviderType,
     * as required in a persistence.xml definition.
     */
    fun getProviderClassName(): String

    /**
     * Retrieves a Map containing the JPA provider class.
     *
     * @return a Map to be used in [EntityManagerFactory] creation, containing the [PersistenceProvider] property
     * and class name.
     */
    fun getPersistenceMap(): TreeMap<String, Any>

    /**
     * Retrieves an [EntityManagerFactory] from this [PersistenceProviderInfo].
     * Fetches the [getPersistenceMap] and merges in its properties into the overrideProperties supplied.
     */
    fun getEntityManagerFactory(persistenceUnitName: String, overrideProperties: MutableMap<String, Any>?)
        : EntityManagerFactory
}

/**
 * Enumeration of Common PersistenceProviders.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
enum class CommonPersistenceProvidersInfo(private val providerClass: String) : PersistenceProviderInfo {

    /**
     * The OpenJPA PersistenceProvider class.
     */
    OPENJPA("org.apache.openjpa.persistence.PersistenceProviderImpl"),

    /**
     * The Hibernate PersistenceProvider.
     */
    HIBERNATE("org.hibernate.jpa.HibernatePersistenceProvider"),

    /**
     * The EclipseLink PersistenceProvider.
     */
    ECLIPSELINK("org.eclipse.persistence.jpa.PersistenceProvider"),

    /**
     * The Data Nucleus PersistenceProvider.
     */
    DATANUCLEUS("org.datanucleus.api.jpa.PersistenceProviderImpl");

    override fun getProviderClassName(): String = providerClass

    override fun getPersistenceMap(): TreeMap<String, Any> {

        val toReturn = TreeMap<String, Any>()

        // Add the persistence provider value
        toReturn[PERSISTENCE_CLASS_PROPERTY] = getProviderClassName()

        // All Done.
        return toReturn
    }

    override fun getEntityManagerFactory(persistenceUnitName: String, overrideProperties: MutableMap<String, Any>?)
        : EntityManagerFactory {

        // Synthesize the override properties
        val props = overrideProperties ?: mutableMapOf()
        props.putAll(getPersistenceMap())

        if (log.isDebugEnabled) {
            log.debug("Creating EntityManagerFactory for PersistenceUnit $persistenceUnitName using: \n" +
                props.toSortedMap()
                    .map { " [${it.key}]: ${it.value}\n" }
                    .reduce { acc, current -> acc + "" + current })
        }

        // All Done.
        return Persistence.createEntityManagerFactory(persistenceUnitName, props)
    }

    companion object {

        @JvmStatic
        private val log: Logger = LoggerFactory.getLogger(CommonPersistenceProvidersInfo::class.java)

        /**
         * The standard property containing the classname (or class) of a [PersistenceProvider]
         */
        const val PERSISTENCE_CLASS_PROPERTY = "javax.persistence.provider"
    }
}
