package se.jguru.shared.persistence.test.jpa.provider

import se.jguru.shared.persistence.spi.jpa.classloading.CommonPersistenceProvidersInfo
import java.util.TreeMap
import javax.persistence.EntityManagerFactory

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
open class HibernateUnitTestProvider : AbstractUnitTestPersistenceProvider() {

    // Internal state
    private val provider = CommonPersistenceProvidersInfo.HIBERNATE

    override fun getProviderClassName(): String = provider.getProviderClassName()

    override fun getPersistenceMap(): TreeMap<String, Any> = provider.getPersistenceMap()

    override fun getEntityManagerFactory(persistenceUnitName: String, overrideProperties: MutableMap<String, Any>?)
        : EntityManagerFactory = provider.getEntityManagerFactory(persistenceUnitName, overrideProperties)
}