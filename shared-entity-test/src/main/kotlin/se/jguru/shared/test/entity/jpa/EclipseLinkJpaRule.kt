package se.jguru.shared.test.entity.jpa

import org.eclipse.persistence.config.PersistenceUnitProperties
import org.eclipse.persistence.config.PersistenceUnitProperties.TRANSACTION_TYPE
import org.eclipse.persistence.config.TargetServer
import org.eclipse.persistence.internal.jpa.EntityManagerFactoryImpl
import org.eclipse.persistence.internal.jpa.EntityManagerSetupImpl
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.util.UUID
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import javax.persistence.spi.PersistenceUnitTransactionType


/**
 * Creates a JUnit rule wrapping an EclipseLink PersistenceUnit, to enable
 * JPA operations in unit tests.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class EclipseLinkJpaRule @JvmOverloads constructor(
    persistenceUnitName: String = "unitTestPU",
    private val database: CommonEclipseLinkConfigurations = CommonEclipseLinkConfigurations.H2_IN_MEMORY) : TestRule {

    // var emf: EntityManagerFactory =
    private val em = ThreadLocal<EntityManager>()

    override fun apply(base: Statement, description: Description): Statement {
        TODO("not implemented")
    }

    @JvmOverloads
    fun createEntityManagerFactory(persistenceUnitName: String = "unitTestPU",
                                   userName: String = "sa",
                                   password: String = "",
                                   dbName: String = "testDB"): EntityManagerFactory {

        val setupImpl = EntityManagerSetupImpl(persistenceUnitName,
            "Session_" + UUID.randomUUID().toString())

        // All Done.
        return EntityManagerFactoryImpl(setupImpl, database.createJpaConfiguration(userName, password, dbName))
    }
}

enum class CommonEclipseLinkConfigurations(
    private val jdbcDriverClass: String,
    private val jdbcUrlProducer: (String) -> String) {

    HSQLDB_IN_MEMORY(
        "org.hsqldb.jdbc.JDBCDriver",
        { "jdbc:hsqldb:mem:$it;hsqldb.tx_level=serializable" }),

    H2_IN_MEMORY(
        "org.h2.Driver",
        { "jdbc:h2:mem:$it;DB_CLOSE_DELAY=-1" });

    fun createJpaConfiguration(userName: String,
                               password: String,
                               dbName: String): MutableMap<String, Any> {

        val toReturn = mutableMapOf<String, Any>()

        // Ensure RESOURCE_LOCAL transactions is used.
        toReturn[TRANSACTION_TYPE] = PersistenceUnitTransactionType.RESOURCE_LOCAL.name

        // Configure the internal EclipseLink connection pool
        toReturn[PersistenceUnitProperties.JDBC_DRIVER] = jdbcDriverClass
        toReturn[PersistenceUnitProperties.JDBC_URL] = jdbcUrlProducer.invoke(dbName)
        toReturn[PersistenceUnitProperties.JDBC_USER] = userName
        toReturn[PersistenceUnitProperties.JDBC_PASSWORD] = password
        toReturn[PersistenceUnitProperties.CONNECTION_POOL_MIN] = "1"

        // Configure logging. FINE ensures all SQL is shown
        toReturn[PersistenceUnitProperties.LOGGING_LEVEL] = "FINE"

        // Ensure that no server-platform is configured
        toReturn[PersistenceUnitProperties.TARGET_SERVER] = TargetServer.None

        // All Done.
        return toReturn
    }
}