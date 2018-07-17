package se.jguru.shared.persistence.test.jpa

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestName
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.slf4j.LoggerFactory
import se.jguru.codestyle.annotations.UseOpenMembers
import se.jguru.shared.persistence.spi.jpa.classloading.PersistenceRedirectionClassLoader
import se.jguru.shared.persistence.test.jpa.provider.HibernateUnitTestProvider
import se.jguru.shared.persistence.test.jpa.provider.UnitTestPersistenceProvider
import java.io.Serializable
import java.sql.Connection
import java.util.SortedMap
import java.util.TreeMap
import javax.persistence.Cache
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import javax.persistence.EntityTransaction
import javax.persistence.Persistence

// Our log
private val log = LoggerFactory.getLogger(AbstractJpaTest::class.java)

@UseOpenMembers
abstract class JpaTestRule @JvmOverloads constructor(

    val persistenceUnitName : String, 

    val provider: UnitTestPersistenceProvider = HibernateUnitTestProvider(),

    val persistenceXmlResourcePath: String = PersistenceRedirectionClassLoader.PERSISTENCE_XML,

    val entityManagerFactoryProperties : TreeMap<String, String> = TreeMap()

) : TestWatcher() {

    // Internal state
    var entityManager : EntityManager? = null

    override fun starting(description: Description) {

        // #1) Stash original classloader if applicable.
        //
        if (persistenceXmlResourcePath != PersistenceRedirectionClassLoader.PERSISTENCE_XML) {
            provider.redirectPersistenceXmlTo(persistenceXmlResourcePath)
        }

        // #2) Get the EntityManager
        //
        entityManager = provider.createEntityManager(persistenceUnitName, entityManagerFactoryProperties)
    }

    override fun finished(description: Description) {

        // Close objects, if applicable
        entityManager?.close()

        // Clean up
        provider.cleanupState()
    }
}

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@UseOpenMembers
abstract class AbstractJpaTest(val provider: UnitTestPersistenceProvider) : Serializable {

    /**
     * Convenience rule providing the currently executing test method name.
     */
    @Rule
    var activeTestName = TestName()

    /**
     * The currently active EntityTransaction.
     */
    protected var transaction: EntityTransaction? = null

    /**
     * The active JPA EntityManager.
     */
    protected var entityManager: EntityManager? = null

    /**
     * The active JPA persistence Cache.
     */
    protected lateinit var jpaCache: Cache

    /**
     * This flag controls if the database should be shut down during the teardown method.
     */
    protected var shutdownDatabaseInTeardown = true

    /**
     * Setting up the JPA framework which setup the Persistence Entity Manager.
     * The framework begins a database transaction before each Test.
     *
     * @throws Exception if an error occurred.
     */
    @Before
    @Throws(Exception::class)
    fun setUp() {

        // #1) Stash original classloader;
        //
        provider.redirectPersistenceXmlTo(getPersistenceXmlFile())

        // Create a JpaPersistenceOperations and a corresponding Transaction.
        val props = getEntityManagerFactoryProperties()

        if (log.isDebugEnabled) {

            // Log the EntityManagerFactory properties used.
            val builder = StringBuilder()
            for ((key, value) in props) {
                builder.append("  [").append(key).append("]: ").append(value).append("\n")
            }

            log.debug("Test Class " + javaClass.simpleName + " - EntityManagerFactoryProperties:\n"
                + builder.toString() + "\n")
        }

        var factory: EntityManagerFactory? = null
        try {
            factory = Persistence.createEntityManagerFactory(getPersistenceUnitName(), props)

            // Clean out the JPA cache, to prevent phantom reads from earlier tests.
            jpaCache = factory!!.cache
            jpaCache.evictAll()

            entityManager = factory.createEntityManager()
        } catch (e: Exception) {

            // Could not create the entityManager
            val factoryType = if (factory == null) "<unknown>" else factory.javaClass.name
            throw IllegalStateException("Could not create EntityManager from factory of type ["
                + factoryType + "]", e)
        }

        transaction = entityManager!!.transaction
    }

    /**
     * Retrieves/unwraps the SQL Connection used by the EntityManager. Note that for some JPA implementations -
     * notably EclipseLink - unwrapping a Connection from the EntityManager will retrieve `null` unless the
     * EntityManager is in a transaction.
     * To avoid any followup problems caused by this, an IllegalStateException will be thrown unless a non-null
     * Connection can properly be retrieved.
     *
     * @param startTransactionIfRequired If the entityManager does not have an active EntityTransaction, and the
     * startNewTransaction parameter is `true`,
     * the EntityManager's Transaction will be started. Otherwise,
     * an Exception will be thrown.
     * @return The SQL Connection used by the entityManager.
     * @throws java.lang.IllegalStateException if `entityManager.getTransaction().isActive()` was `false`.
     */
    @Throws(IllegalStateException::class)
    protected fun getJpaUnitTestConnection(startTransactionIfRequired: Boolean): Connection? {

        // Check sanity
        if (!entityManager!!.transaction.isActive) {

            if (startTransactionIfRequired) {
                entityManager!!.transaction.begin()
            } else {
                throw IllegalStateException("EclipseLink - and perhaps other JPA implementations - considers it "
                    + "an Exception to unwrap the DB Connection from the JPA EntityManager unless there is an "
                    + "active Transaction. Fix your testcase [" + javaClass.simpleName
                    + "], method [" + activeTestName.methodName + "] to adhere to these mechanics.")
            }
        }

        // All done.
        val toReturn = entityManager!!.unwrap(Connection::class.java)
        if (toReturn == null) {
            log.warn("EntityManager unwrapped a null JDBC Connection. Proceeding anyways; insane states may occur.")
        }
        return toReturn
    }

    /**
     * Override to supply any additional EntityManagerFactory properties.
     * The properties are supplied as the latter argument to the
     * `Persistence.createEntityManagerFactory` method.
     * The properties supplied within this Map override property definitions
     * given in the persistence.xml file.
     *
     * @return Properties supplied to the EntityManagerFactory, implying they do not
     * need to be declared within the persistence.xml file.
     * @see Persistence.createEntityManagerFactory
     */
    protected open fun getEntityManagerFactoryProperties(): SortedMap<String, String> {
        return TreeMap()
    }

    /**
     * Tear down the JPA framework and rollback any started transactions.
     */
    @After
    fun tearDown() {

        try {
            // Clean up the test schema
            cleanupTestSchema(shutdownDatabaseInTeardown)

            // Ensure that EclipseLink actually reloads its persistence unit,
            // by ensuring that we always close the PersistenceContextFactoryProvider.
            //
            // As indicated in https://bugs.eclipse.org/bugs/show_bug.cgi?id=408015, this does not always happen.
            /*
            val persistenceContextFactoryProviderLoader = ServiceLoader.load<PersistenceContextFactoryProvider>(PersistenceContextFactoryProvider::class.java!!,
                Thread.currentThread().contextClassLoader)
            for (current in persistenceContextFactoryProviderLoader) {
                val persistenceContextFactory = current.getPersistenceContextFactory(null)
                if (persistenceContextFactory != null) {
                    persistenceContextFactory!!.close()
                }
            }
            */

            // rollback
            if (transaction != null && transaction!!.isActive) {
                try {
                    transaction!!.rollback()
                } catch (e: Exception) {
                    throw IllegalStateException("Could not rollback active EntityTransaction.", e)
                }

            }
            transaction = null

            // Close the database, and the EntityManager
            if (entityManager != null && entityManager!!.isOpen) {
                try {
                    entityManager!!.close()

                    if (log.isDebugEnabled) {
                        log.debug("Closed EntityManager.")
                    }

                } catch (e: Exception) {
                    throw IllegalStateException("Could not close EntityManager.", e)
                }

            }
        } finally {

            provider.cleanupState()
        }
    }

    /**
     * Invoked during teardown to clean up the schema used for test.
     *
     * @param shutdownDatabase if `true`, the database should be shutdown after cleaning the schema.
     */
    protected abstract fun cleanupTestSchema(shutdownDatabase: Boolean)

    /**
     * Retrieves the classpath-relative path to the persistence.xml file used in this AbstractJpaTest.
     *
     * @return classpath-relative path to the persistence.xml file used in this AbstractJpaTest.
     */
    protected abstract fun getPersistenceXmlFile(): String

    /**
     * Retrieves the name of the PersistenceUnit, as defined within the Persistence.xml file
     * and also within @Persistence annotations in user classes.
     *
     * @return the name of the active PersistenceUnit.
     */
    protected abstract fun getPersistenceUnitName(): String

    /**
     * Commits the currently active EntityTransaction, and starts a new EntityTransaction.
     *
     * @see .commit
     */
    protected fun commitAndStartNewTransaction() {
        commit(true)
    }

    /**
     * Performs rollback on the currently active EntityTransaction, and starts a new EntityTransaction.
     *
     * @see .commit
     */
    protected fun rollbackAndStartNewTransaction() {
        rollback(true)
    }

    /**
     *
     * Performs rollback on the currently active EntityTransaction, and starts a new EntityTransaction
     * if told to do so.
     *
     * if `true`, starts a new EntityTransaction following the rollback of the currently active one.
     *
     * @throws java.lang.IllegalStateException if the Transaction could not be rolled back or begun anew.
     */
    @Throws(IllegalStateException::class)
    protected fun rollback(startNewTransaction: Boolean) {
        endTransactionAndStartAnother(false, startNewTransaction)
    }

    /**
     * Commits the currently active EntityTransaction, and starts a new EntityTransaction if so ordered.
     *
     * @param startNewTransaction if `true`, starts a new EntityTransaction following the commit of the
     * currently active one.
     * @throws java.lang.IllegalStateException if the Transaction could not be committed or begun anew.
     */
    @Throws(IllegalStateException::class)
    protected fun commit(startNewTransaction: Boolean) {
        endTransactionAndStartAnother(true, startNewTransaction)
    }

    //
    // Private helpers
    //

    @Throws(IllegalStateException::class)
    private fun endTransactionAndStartAnother(commitActiveTransaction: Boolean,
                                              startNewTransaction: Boolean) {

        try {

            // Either commit or rollback the transaction
            if (commitActiveTransaction) {
                transaction!!.commit()
            } else {
                transaction!!.rollback()
            }

            // Create a new EntityTransaction
            transaction = entityManager!!.transaction

        } catch (e: Exception) {
            throw IllegalStateException("Could not create a new EntityTransaction", e)
        }

        if (startNewTransaction) {
            try {
                transaction!!.begin()
            } catch (e: Exception) {
                throw IllegalStateException("Could not begin() the newly created EntityTransaction.", e)
            }

        }
    }
}