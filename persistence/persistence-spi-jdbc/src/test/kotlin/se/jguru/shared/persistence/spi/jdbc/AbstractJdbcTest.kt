package se.jguru.shared.persistence.spi.jdbc

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.Statement
import javax.sql.DataSource

enum class TransactionIsolation(val connectionValue: Int) {

    /**
     * A constant indicating that transactions are not supported.
     */
    TRANSACTION_NONE(Connection.TRANSACTION_NONE),


    /**
     * A constant indicating that
     * dirty reads, non-repeatable reads and phantom reads can occur.
     * This level allows a row changed by one transaction to be read
     * by another transaction before any changes in that row have been
     * committed (a "dirty read").  If any of the changes are rolled back,
     * the second transaction will have retrieved an invalid row.
     */
    TRANSACTION_READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),

    /**
     * A constant indicating that
     * dirty reads are prevented; non-repeatable reads and phantom
     * reads can occur.  This level only prohibits a transaction
     * from reading a row with uncommitted changes in it.
     */
    TRANSACTION_READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),

    /**
     * A constant indicating that
     * dirty reads and non-repeatable reads are prevented; phantom
     * reads can occur.  This level prohibits a transaction from
     * reading a row with uncommitted changes in it, and it also
     * prohibits the situation where one transaction reads a row,
     * a second transaction alters the row, and the first transaction
     * rereads the row, getting different values the second time
     * (a "non-repeatable read").
     */
    TRANSACTION_REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),

    /**
     * A constant indicating that
     * dirty reads, non-repeatable reads and phantom reads are prevented.
     * This level includes the prohibitions in
     * `TRANSACTION_REPEATABLE_READ` and further prohibits the
     * situation where one transaction reads all rows that satisfy
     * a `WHERE` condition, a second transaction inserts a row that
     * satisfies that `WHERE` condition, and the first transaction
     * rereads for the same condition, retrieving the additional
     * "phantom" row in the second read.
     */
    TRANSACTION_SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE)
}

abstract class AbstractJdbcTest {

    protected lateinit var dataSource: DataSource

    @BeforeEach
    fun setupSharedState() {

        dataSource = createDataSource()

        // Setup initial DB state
        executeStatements(getInitializeDbStateSqlStatements())
    }

    @AfterEach
    fun teardownSharedState() {
        executeStatements(getCleanupDbStateSqlStatements())
    }

    protected abstract fun createDataSource(): DataSource

    protected abstract fun getInitializeDbStateSqlStatements() : List<String>

    protected abstract fun getCleanupDbStateSqlStatements() : List<String>

    //
    // Helpers
    //

    protected fun <R> doInConnection(executor: (conn: Connection) -> R): R =
        dataSource.connection.use { executor.invoke(it) }

    protected fun executeStatements(sqlStatements: List<String>): List<Int> {

        val toReturn = mutableListOf<Int>()

        doInConnection { conn ->
            sqlStatements
                .map {

                    if(log.isDebugEnabled) {
                        log.debug("Firing: $it")
                    }

                    conn.createStatement().executeUpdate(it, Statement.RETURN_GENERATED_KEYS)
                }
                .forEach { toReturn.add(it) }
        }

        return toReturn
    }

    /**
     * DB ConnectionPool accessor method.
     */
    @JvmOverloads
    protected fun createDatabasePool(
        jdbcURL: String,
        jdbcUserName: String,
        jdbcPassword: String,
        poolName: String = "unitTestDbPool",
        isolation: TransactionIsolation = TransactionIsolation.TRANSACTION_REPEATABLE_READ,
        minimumIdle: Int = 0,
        maximumPoolSize: Int = 10): DataSource {

        // Create the DB Pool
        val config = HikariConfig()

        // Configure the DB pool
        config.poolName = poolName
        config.jdbcUrl = jdbcURL
        config.username = jdbcUserName
        config.password = jdbcPassword

        config.transactionIsolation = isolation.name
        config.minimumIdle = minimumIdle
        config.maximumPoolSize = maximumPoolSize

        // All Done.
        return HikariDataSource(config)
    }

    companion object {

        @JvmStatic
        private val log = LoggerFactory.getLogger(AbstractJdbcTest::class.java)
    }
}