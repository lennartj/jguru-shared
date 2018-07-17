package se.jguru.shared.persistence.test.jpa.dbunit

import org.dbunit.database.IDatabaseConnection
import org.dbunit.dataset.IDataSet
import se.jguru.shared.persistence.test.jpa.db.DatabaseInfo
import java.io.Serializable

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
interface DbUnitAccess : Serializable {

    /**
     * The dbUnit IDatabaseConnection, hooked up to the same
     * database as the JPA EntityManager, either using a connection of its own
     * or piggybacking on
     */
    fun getDbUnitConnection(): IDatabaseConnection

    /**
     * Retrieves the name of the database used for this [DbUnitAccess].
     *
     * @return the name of the database used for this [DbUnitAccess].
     */
    fun getDatabaseName(): String

    /**
     * @return The [DatabaseInfo] of the underlying Database.
     */
    fun getDatabaseType(): DatabaseInfo

    fun extractData() : IDataSet
}

open class StandardDbUnitAccess : DbUnitAccess {

    override fun getDbUnitConnection(): IDatabaseConnection {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getDatabaseName(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getDatabaseType(): DatabaseInfo {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun extractData(): IDataSet {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}