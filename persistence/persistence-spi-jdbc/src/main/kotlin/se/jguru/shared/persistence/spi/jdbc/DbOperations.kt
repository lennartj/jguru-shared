/*-
 * #%L
 * Nazgul Project: jguru-shared-persistence-spi-jdbc
 * %%
 * Copyright (C) 2018 - 2019 jGuru Europe AB
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
package se.jguru.shared.persistence.spi.jdbc

import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.sql.Statement
import java.util.concurrent.atomic.AtomicInteger
import javax.sql.DataSource

/**
 * Utility methods for working with JDBC statements and DataSources.
 * It is recommended to create local [ResultSet] extension functions delegating
 * their invocation to these methods, in order to simplify usage.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
object DbOperations {

    @JvmStatic
    private val log = LoggerFactory.getLogger(DbOperations::class.java)

    /**
     * Retrieves a nullable value from the supplied ResultSet.
     *
     * @param columnIndex The JDBC column index (starting with 1).
     * @return The value, or null if the underlying SQL number was null.
     */
    @JvmStatic
    fun getLongOrNull(columnIndex: Int, resultSet: ResultSet): Long? =
        getOrNull(columnIndex, resultSet) { idx, rs -> rs.getLong(idx) }

    /**
     * Retrieves a nullable value from the supplied ResultSet.
     *
     * @param columnIndex The JDBC column index (starting with 1).
     * @return The value, or null if the underlying SQL number was null.
     */
    @JvmStatic
    fun getIntOrNull(columnIndex: Int, resultSet: ResultSet): Int? =
        getOrNull(columnIndex, resultSet) { idx, rs -> rs.getInt(idx) }

    /**
     * Retrieves a nullable value from the supplied ResultSet.
     *
     * @param columnIndex The JDBC column index (starting with 1).
     * @return The value, or null if the underlying SQL number was null.
     */
    @JvmStatic
    fun getShortOrNull(columnIndex: Int, resultSet: ResultSet): Short? =
        getOrNull(columnIndex, resultSet) { idx, rs -> rs.getShort(idx) }

    /**
     * Retrieves a nullable value from the supplied ResultSet.
     *
     * @param columnIndex The JDBC column index (starting with 1).
     * @return The value, or null if the underlying SQL number was null.
     */
    @JvmStatic
    fun getByteOrNull(columnIndex: Int, resultSet: ResultSet): Byte? =
        getOrNull(columnIndex, resultSet) { idx, rs -> rs.getByte(idx) }

    /**
     * Retrieves a nullable value from the supplied ResultSet.
     *
     * @param columnIndex The JDBC column index (starting with 1).
     * @return The value, or null if the underlying SQL number was null.
     */
    @JvmStatic
    fun getBooleanOrNull(columnIndex: Int, resultSet: ResultSet): Boolean? =
        getOrNull(columnIndex, resultSet) { idx, rs -> rs.getBoolean(idx) }

    /**
     * Fires the SQL statement into a connection obtained from the supplied DataSource,
     * then applies the RowDataConverter to convert each row into a T product.
     *
     * @param dataSource The [DataSource] used to fire the SQL
     * @param sql The SQL to fire
     * @param rowDataConverter The [RowDataConverter] used to convert each row within
     * the [ResultSet] into a T domain object.
     * @param parameters The parameters for the SQL query, or empty if no parameters are needed.
     */
    @JvmStatic
    @JvmOverloads
    fun <T> readAndConvert(dataSource: DataSource,
                           sql: String,
                           rowDataConverter: (rs: ResultSet, rowNum: Int) -> T?,
                           parameters: List<Any?> = mutableListOf()): List<T> {

        val toReturn = mutableListOf<T>()

        dataSource.connection.use {

            // Prepare the statement
            val prep = it.prepareStatement(sql)

            if (log.isDebugEnabled) {

                val dbMetadata = prep.connection.metaData
                log.debug("Connected to Database [${dbMetadata.databaseProductName}, " +
                    "v. ${dbMetadata.databaseProductVersion}] using Driver [${dbMetadata.driverName}, " +
                    "v. ${dbMetadata.driverVersion}]")
            }

            // Set any given (positional) parameters
            if (parameters.isNotEmpty()) {
                parameters.forEachIndexed { index, current ->

                    val jdbcIndex = index + 1

                    if (log.isDebugEnabled) {
                        val argumentType = when (current == null) {
                            true -> "<null>"
                            else -> current::class.java.name
                        }

                        log.debug("Setting parameter [$index (jdbcIndex: $jdbcIndex)] to type [$argumentType]")
                    }

                    prep.setObject(index + 1, current)
                }
            }

            // Execute and extract fully
            prep.use { ps ->
                ps.executeQuery().use { rs ->

                    // Get the metadata
                    val rsMetadata = rs.metaData
                    val colCount = rsMetadata.columnCount

                    if (log.isDebugEnabled) {

                        val buffer = StringBuilder("Retrieved result with [$colCount] columns:\n")

                        for (index in 1..colCount) {
                            buffer.append("Column [$index/$colCount]: \"${rsMetadata.getColumnName(index)}\" " +
                                "- ${rsMetadata.getColumnTypeName(index)}\n")
                        }

                        log.debug(buffer.toString())
                    }

                    val index = AtomicInteger()
                    while (rs.next()) {

                        val converted = rowDataConverter.invoke(rs, index.incrementAndGet())

                        if (converted != null) {
                            toReturn.add(converted as T)
                        }
                    }
                }
            }
        }

        // All Done.
        return toReturn
    }

    /**
     * Convenience method to update all data from the supplied DataImportResult into the database.
     * Does not retrieve auto-generated Primary Keys, since no new records are created in a SQL UPDATE statement.
     *
     * @param dataSource The DataSource where data should be inserted.
     * @param preparedStatementSQL The SQL for the prepared statement.
     * @param valueHoldersToUpdate The objects containing values to update (i.e. value holder objects).
     * @param parameterFactory a factory method which should provide an array containing the arguments
     * produced by an element to be updated within the database. The arguments should match the supplied
     * preparedStatementSQL.
     */
    @JvmStatic
    fun <T> update(dataSource: DataSource,
                   preparedStatementSQL: String,
                   valueHoldersToUpdate: List<T>,
                   closeOnCompletion : Boolean = false,
                   parameterFactory: (anElement: T) -> Array<Any?>): DbModificationMetadata =

        insertOrUpdate(dataSource,
            preparedStatementSQL,
            valueHoldersToUpdate,
            null,
            closeOnCompletion,
            parameterFactory)

    /**
     * Inserts or Updates database records in batched mode (i.e. using JDBC preparedStatement.executeBatch()).
     *
     * @param dataSource The [DataSource] to which the statements should be fired
     * @param preparedStatementSQL The SQL of the prepared statement.
     * @param toInsertOrUpdate The objects containing values to insert or update (i.e. value holder objects).
     * @param idColumnNames If supplied, contains the names of ID columns whose (generated) values
     * are returned within the [DbModificationMetadata] response. This provides a means to retrieve
     * generated primary key values from the database after creating new records. The order of keys
     * is given by the `ResultSet.generatedPrimaryKeys` method.
     * @param closeOnCompletion if `true`, attempt to close the ResultSet on completion.
     * @param parameterFactory A lambda extracting parameters in the order defined within the [preparedStatementSQL])
     * from a single object within the [toInsertOrUpdate] list.
     *
     * @return An [DbModificationMetadata] object wrapping the
     */
    @JvmStatic
    @JvmOverloads
    fun <T> insertOrUpdate(dataSource: DataSource,
                           preparedStatementSQL: String,
                           toInsertOrUpdate: List<T>,
                           idColumnNames: Array<String>? = null,
                           closeOnCompletion : Boolean = false,
                           parameterFactory: (anElement: T) -> Array<Any?>): DbModificationMetadata {

        return dataSource.connection.use {

            val generatedIdColumnsSupplied = idColumnNames != null && idColumnNames.isNotEmpty()
            val ps = when (generatedIdColumnsSupplied) {
                true -> it.prepareStatement(preparedStatementSQL, idColumnNames)
                else -> it.prepareStatement(preparedStatementSQL)
            }

            val toReturn = ps.use {

                toInsertOrUpdate.forEachIndexed { _, anElement ->

                    // Be defensive; this should not really be required.
                    ps.clearParameters()

                    // Create and assign the parameters to the PS
                    val parameters = parameterFactory(anElement)
                    parameters.forEachIndexed { paramIndex, paramValue ->
                        ps.setObject(paramIndex + 1, paramValue)
                    }

                    // Add to the batch
                    ps.addBatch()
                }

                val numRowsAffected = try {
                    ps.executeBatch()
                        .filter { result -> result != Statement.SUCCESS_NO_INFO }
                        .filter { result -> result != Statement.EXECUTE_FAILED }
                        .sum()
                } catch (e: Exception) {

                    log.error("Could not execute batch [$preparedStatementSQL]", e)
                    throw IllegalStateException("Could not execute batch [$preparedStatementSQL]", e)
                }

                // Handle returning of primary keys
                val generatedPrimaryKeys = mutableListOf<Any>()

                if (generatedIdColumnsSupplied) {

                    // Attempt to fetch generated primary keys
                    ps.generatedKeys.use { rs ->
                        while (rs.next()) {

                            val rsmd = rs.metaData
                            val generatedPKsInThisRow = mutableListOf<Any?>()

                            for (index in 1..rsmd.columnCount) {

                                // Handle a null value.
                                val readValue = rs.getObject(index)
                                when (rs.wasNull()) {
                                    true -> {

                                        log.warn("Received a null generated PK for column [${rsmd.getColumnName(index)}" +
                                            " :: ${rsmd.getColumnTypeName(index)}] in " +
                                            "table [${rsmd.getTableName(index)}]")

                                        generatedPKsInThisRow.add(null)
                                    }
                                    else -> generatedPKsInThisRow.add(readValue)
                                }
                            }

                            when (generatedPKsInThisRow.size) {
                                0 -> {
                                    log.warn("Expected: at least 1 generated primary key, but got 0. [${idColumnNames?.size}]"
                                    +
                                    " Columns: " + idColumnNames?.reduce { acc, s -> "$acc, $s" } ?: "<nothing>")
                                }
                                1 -> generatedPrimaryKeys.add(generatedPKsInThisRow[0]!!)
                                else -> generatedPrimaryKeys.add(generatedPKsInThisRow)
                            }
                        }
                    }
                }

                // All Done.
                DbModificationMetadata(numRowsAffected, generatedPrimaryKeys)
            }

            if (closeOnCompletion && !ps.isClosed && !ps.isCloseOnCompletion) {
                ps.closeOnCompletion()
            }

            toReturn
        }
    }

    /**
     * Retrieves the value from given column of the ResultSet supplied, taking into consideration that
     * the retrieved value may be null.
     *
     * @param columnIndex The index of the column for which the value should be retrieved.
     * @param resultSet The [ResultSet] from which the value should be retrieved.
     * @param accessor The accessor method used to actually read the value.
     *
     * @return the value or `null` if the given column held a null value.
     */
    @JvmStatic
    fun <T> getOrNull(columnIndex: Int,
                      resultSet: ResultSet,
                      accessor: (columnIndex: Int, rs: ResultSet) -> T): T? {

        val toReturn = accessor.invoke(columnIndex, resultSet)

        return when (resultSet.wasNull()) {
            true -> null
            else -> toReturn
        }
    }

    /**
     * Retrieves the value from given column label of the ResultSet supplied, taking into
     * consideration that the retrieved value may be null.
     *
     * @param columnLabel The label of the column for which the value should be retrieved.
     * @param resultSet The [ResultSet] from which the value should be retrieved.
     * @param accessor The accessor method used to actually read the value.
     *
     * @return the value or `null` if the given column held a null value.
     */
    @JvmStatic
    fun <T> getOrNull(columnLabel: String,
                      resultSet: ResultSet,
                      accessor: (columnLabel: String, rs: ResultSet) -> T): T? {

        val toReturn = accessor.invoke(columnLabel, resultSet)

        return when (resultSet.wasNull()) {
            true -> null
            else -> toReturn
        }
    }
}
