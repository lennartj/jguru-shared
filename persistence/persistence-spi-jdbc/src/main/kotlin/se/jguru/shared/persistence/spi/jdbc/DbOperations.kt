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
            prep.executeQuery().use { rs ->

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
                        toReturn.add(converted)
                    }
                }
            }
        }

        // All Done.
        return toReturn
    }

    /**
     * Inserts all data from the supplied DataImportResult into the database.
     *
     * @param dataSource The DataSource where data should be inserted.
     * @param preparedStatementSQL The SQL for the prepared statement.
     * @param toInsert The list of objects to persist/insert.
     * @param parameterFactory a factory method which should supply the
     */
    @JvmStatic
    fun <T> insert(dataSource: DataSource,
                   preparedStatementSQL: String,
                   toInsert: List<T>,
                   parameterFactory: (anElement: T) -> Array<Any?>): Int {
        return dataSource.connection.use {

            val ps = it.prepareStatement(preparedStatementSQL)
            val numRowsInserted = AtomicInteger()

            toInsert.forEachIndexed { _, anElement ->

                // Be defensive; this should not really be required.
                ps.clearParameters()

                // Create and assign the parameters to the PS
                val parameters = parameterFactory(anElement)
                parameters.forEachIndexed { paramIndex, paramValue ->
                    ps.setObject(paramIndex + 1, paramValue)
                }

                try {

                    // Execute the insert and update the counter correspondingly
                    val numRowsAffected = ps.executeUpdate()
                    numRowsInserted.addAndGet(numRowsAffected)

                } catch (e: Exception) {
                    log.error("Could not insert $anElement", e)
                }
            }

            // All Done.
            numRowsInserted.get()
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
