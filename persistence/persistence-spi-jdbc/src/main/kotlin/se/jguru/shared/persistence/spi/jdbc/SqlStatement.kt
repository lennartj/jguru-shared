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

import com.fasterxml.jackson.annotation.JsonIgnore

/**
 * Simple classification of an SQL statements.
 *
 * @param identifier The identifier of this SqlStatement
 * @param sqlType The type of SQL statement provided in the template
 * @param template The SQL template text, possibly containing [SqlTemplateSubstitution] tokens.
 * @param argumentToken if a non-standard parameter identifier is used in the SQL, provide it here.
 * Otherwise defaults to '?'.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
open class SqlStatement @JvmOverloads constructor(
    val identifier: String,
    val sqlType: SqlStatementType,
    open val template: String,
    open val params: String? = null,
    private val argumentToken: Char? = null) : Comparable<SqlStatement> {

    /**
     * Replaces any SqlTemplateSubstitution tokens found within the template text template with the values
     * supplied within the substitutionMap, or an empty string if any present SqlTemplateSubstitutions
     * held no value within the supplied substitutionMap.
     *
     * @param substitutionMap A Map relating SqlTemplateSubstitutions to replacement values.
     * @return an SQL statement ready for use.
     */
    @JsonIgnore
    @JvmOverloads
    fun tokenize(substitutionMap: Map<SqlTemplateSubstitution, String> = mutableMapOf()): String {

        // Handle synthetic substitutions
        val allSubstitutions = mutableMapOf<SqlTemplateSubstitution, String>()
        allSubstitutions[SqlTemplateSubstitution.PARAMS] =
            substitutionMap.getOrElse(SqlTemplateSubstitution.PARAMS) { params ?: "" }

        allSubstitutions[SqlTemplateSubstitution.ARGUMENTS] =
            substitutionMap.getOrElse(SqlTemplateSubstitution.ARGUMENTS) {
                when (numParameters) {
                    0 -> ""
                    else -> (0 until numParameters).map { "${getArgumentToken()}" }.reduce { acc, c -> "$acc, $c" }
                }
            }


        // Join in manual substitutions
        SqlTemplateSubstitution.values()
            .filter { !it.isSynthetic }
            .forEach { allSubstitutions[it] = substitutionMap[it] ?: "" }

        var toReturn = template

        for ((currentSubstitution, replacementValue) in allSubstitutions) {
            toReturn = toReturn.replace(currentSubstitution.token(), replacementValue)
        }

        // All Done.
        return toReturn
    }

    override fun compareTo(other: SqlStatement): Int {

        var toReturn = this.sqlType.compareTo(other.sqlType)

        if (toReturn == 0) {
            toReturn = this.identifier.compareTo(other.identifier, true)
        }

        // All Done
        return toReturn
    }

    /**
     * The number of parameters found within the SQL.
     */
    val numParameters: Int
        @JsonIgnore
        get() = when (val commaCount = params?.count { it == ',' } ?: 0) {
            0 -> when (params?.trim()?.isNotEmpty() ?: false) {
                true -> 1
                else -> 0
            }
            else -> commaCount + 1
        }

    /**
     * @return The argument token char. Defaults to '?' (JDBC standard) unless explicitly provided.
     */
    @JsonIgnore
    fun getArgumentToken(): Char = when (argumentToken == null) {
        true -> '?'
        else -> argumentToken
    }

    override fun toString(): String {
        return "SqlStatement [$numParameters parameters]: $template"
    }
}
