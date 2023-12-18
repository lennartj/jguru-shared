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

import java.sql.PreparedStatement

/**
 * Specification for how to interact with a [PreparedStatement] in terms of
 * assigning its parameters.
 */
interface PreparedStatementArgumentSetter<T, P> {

    /**
     * Sets the arguments within the supplied [PreparedStatement] originating from any supplied arguments
     */
    fun setArguments(ps: PreparedStatement, arguments: List<T> = mutableListOf(), converter: (List<T>) -> P)
}

/**
 * Abstract specification for creating an in/where clause.
 *
 * @param arguments The list of arguments for the in clause.
 * @param converter The converter between the arguments (i.e. List) and the primitive
 * @param whereFactory A factory method invoked to create a WHERE clause if [hasArguments] is `true`
 * @param prepSetter A PreparedStatementSetter used to create
 */
abstract class InClauseStruct<T, P> @JvmOverloads constructor(
    private val arguments: List<T>,
    private val converter: (List<T>) -> P,
    whereFactory: () -> String,
    open val prepSetter: PreparedStatementArgumentSetter<T, P> = getStandardArgumentSetter()) {

    /**
     * Has the value `true` if the arguments List of this [InClauseStruct] was not empty.
     */
    val hasArguments = arguments.isNotEmpty()

    /**
     * Synthesizes a SQL `where` clause tailored to t
     */
    val whereClause: String = when (hasArguments) {
        true -> whereFactory()
        else -> ""
    }

    fun toPair(): Pair<String, PreparedStatementArgumentSetter<T, P>> = Pair(whereClause, prepSetter)

    companion object {

        @JvmStatic
        fun getStandardWhereFactory(colName: String): () -> String = { " where $colName = any ( ? ) " }

        @JvmStatic
        fun <T, P> getStandardArgumentSetter() = object : PreparedStatementArgumentSetter<T, P> {
            override fun setArguments(ps: PreparedStatement, arguments: List<T>, converter: (List<T>) -> P) {
                if (arguments.isNotEmpty()) {
                    ps.setObject(1, converter.invoke(arguments))
                }
            }
        }
    }
}

/**
 * An in-clause which uses Strings for arguments.
 *
 * @param colName The name of the where/in column should be created.
 * @param arguments The list of arguments for the in clause.
 */
class StringInClauseStruct(colName: String, arguments: List<String>)
    : InClauseStruct<String, Array<String>>(arguments, { it.toTypedArray() }, getStandardWhereFactory(colName))

/**
 * An in-clause which uses Longs for arguments.
 *
 * @param colName The name of the where/in column should be created.
 * @param arguments The list of arguments for the in clause.
 */
class LongInClauseStruct(colName: String, arguments: List<Long>)
    : InClauseStruct<Long, LongArray>(arguments, { it.toLongArray() }, getStandardWhereFactory(colName))
