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
package se.jguru.shared.persistence.spi.jpa

import se.jguru.shared.persistence.spi.jpa.classloading.PersistenceProviderInfo
import java.util.TreeMap
import javax.persistence.EntityManagerFactory
import javax.persistence.Persistence
import javax.persistence.spi.PersistenceProvider
import javax.persistence.spi.PersistenceProviderResolverHolder

/**
 * Singleton object containing some helpful/useful/neat algorithms for JPA use.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
object JpaHelper {

    /**
     * Creates an EntityManagerFactory with or without any overriding JPA Provider Properties.
     *
     * @return an [EntityManagerFactory] created from the Persistence facade.
     */
    @JvmStatic
    @JvmOverloads
    fun createEntityManagerFactory(persistenceUnitName: String,
                                   overrideProperties: MutableMap<String, Any> = mutableMapOf())
        : EntityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnitName, overrideProperties)

    /**
     * Retrieves all known/registered [PersistenceProvider]s.
     */
    @JvmStatic
    val persistenceProviders: List<PersistenceProvider>
        get() = PersistenceProviderResolverHolder.getPersistenceProviderResolver().persistenceProviders

    /**
     * Extracts the DDL (and DML, depending on the JPA provider) schema from the given/named PersistenceUnit.
     *
     * @param persistenceUnitName The name of the Persistence unit from which schema should be extracted.
     * @param persistenceProviderInfo The provider used to extract the data.
     * @param overrideProperties an optional Map containing any properties to configure the underlying
     * PersistenceProvider.
     */
    @JvmStatic
    @JvmOverloads
    fun extractSchema(persistenceUnitName: String,
                      persistenceProviderInfo: PersistenceProviderInfo,
                      overrideProperties: MutableMap<String, Any> = mutableMapOf()) {

        // Synthesize the properties
        val props = TreeMap(overrideProperties)
        props.putAll(persistenceProviderInfo.getPersistenceMap())

        // All Done.
        Persistence.generateSchema(persistenceUnitName, props);
    }
}
