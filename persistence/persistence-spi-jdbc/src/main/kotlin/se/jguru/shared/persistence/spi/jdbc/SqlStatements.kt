/*-
 * #%L
 * Nazgul Project: jguru-shared-persistence-spi-jdbc
 * %%
 * Copyright (C) 2018 - 2023 jGuru Europe AB
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

import java.util.SortedMap
import java.util.TreeMap

/**
 * Simple structure in which to store SQL statements.
 *
 * @param statements The list of SqlStatement objects wrapped
 * @param systemName The name of the system for which the wrapped SqlStatements are valid.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
open class SqlStatements @JvmOverloads constructor(
    val systemName: String,
    val statements: SortedMap<SqlStatementType, MutableList<SqlStatement>> = TreeMap()) {

    /**
     * Adds the supplied SqlStatement to this holder.
     *
     * @param statement The SqlStatement to add.
     */
    fun addStatement(statement: SqlStatement) {
        val statementList = statements.getOrPut(statement.sqlType) { ArrayList() }
        statementList.add(statement)
    }

    /**
     * Finds the SqlStatement for the supplied identifier and type, or throws an
     * IllegalArgumentException if none was found.
     */
    @JvmOverloads
    @Throws(IllegalArgumentException::class)
    fun getStatement(identifier: String, type: SqlStatementType = SqlStatementType.READ): SqlStatement {

        return statements[type]?.find { identifier.equals(it.identifier, true) }
            ?: throw IllegalArgumentException("No SQL statement found for identifier [$identifier] " +
                "and type [${type.name}]")
    }
}
