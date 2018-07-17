package se.jguru.shared.persistence.test.jpa.provider

import org.slf4j.LoggerFactory
import se.jguru.shared.persistence.spi.jpa.classloading.PersistenceProviderInfo
import se.jguru.shared.persistence.spi.jpa.classloading.PersistenceRedirectionClassLoader
import java.util.SortedMap
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
interface UnitTestPersistenceProvider : PersistenceProviderInfo {

    fun redirectPersistenceXmlTo(resourcePath: String)

    fun createEntityManager(persistenceUnitName : String,
                            overrideProperties: SortedMap<String, String>): EntityManager

    fun cleanupState()
}

abstract class AbstractUnitTestPersistenceProvider() : UnitTestPersistenceProvider {

    // Internal state
    private var originalClassLoader: ClassLoader? = null
    var factory : EntityManagerFactory? = null

    override fun redirectPersistenceXmlTo(resourcePath: String) {

        // #1) Stash the original ThreadContext ClassLoader
        //
        val loader = Thread.currentThread().contextClassLoader

        // #2) Inject a PersistenceRedirectionClassLoader
        //
        val redirectionClassLoader = PersistenceRedirectionClassLoader(loader, resourcePath)
        if (log.isDebugEnabled) {
            log.debug("Re-assigning Thread Context ClassLoader. Using ${redirectionClassLoader::class.java.name} " +
                "with redirection resource [${redirectionClassLoader.redirectTo}]")
        }
        Thread.currentThread().contextClassLoader = redirectionClassLoader
    }

    override fun createEntityManager(persistenceUnitName: String, overrideProperties: SortedMap<String, String>): EntityManager {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun cleanupState() {

        if (originalClassLoader != null) {

            if (log.isDebugEnabled) {
                log.debug("Restoring original Thread Context ClassLoader")
            }

            Thread.currentThread().contextClassLoader = originalClassLoader
        }
    }

    companion object {

        // Our log
        @JvmStatic
        private val log = LoggerFactory.getLogger(AbstractUnitTestPersistenceProvider::class.java)
    }
}