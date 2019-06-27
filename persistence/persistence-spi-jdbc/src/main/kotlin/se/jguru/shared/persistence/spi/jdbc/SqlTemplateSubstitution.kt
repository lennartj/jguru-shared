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

/**
 * Enumeration defining the available SQL template substitutions.
 *
 * @param isSynthetic If true, the substitution cannot be set from the standard [MapBuilder].
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
enum class SqlTemplateSubstitution(val isSynthetic: Boolean) {
    DISTINCT(false),
    TABLE(false),
    WHERE(false),
    PARAMS(true),
    ARGUMENTS(true);

    /**
     * @return The SQL template Token which can be substituted for another value within an SQL Template.
     */
    fun token(): String = "##$name##"

    /**
     * Simple Builder for SqlTemplateSubstitution Maps.
     */
    class MapBuilder {
        private var distinct: Boolean = false
        private var table: String = ""
        private var where: String = ""

        /**
         * Call this to yield a "DISTINCT" clause.
         */
        fun distinct(): MapBuilder {
            distinct = true
            return this
        }

        /**
         * @param condition assigns the distinct substitution conditionally.
         */
        fun distinctIf(condition: Boolean): MapBuilder {
            distinct = condition
            return this
        }

        /**
         * @param name The schema and table of this SqlTemplateSubstitution. Example: "organisations.organisationuser"
         */
        fun schemaAndTable(name: String): MapBuilder {
            table = name
            return this
        }

        /**
         * @param whereClause The where clause of this SqlTemplateSubstitution without the "WHERE" keyword (which
         * will be appended automagically). Example: "id = any(?)"
         */
        fun where(whereClause: String): MapBuilder {
            where = whereClause
            return this
        }

        /**
         * Builds the full SqlTemplateSubstitution tokenization map.
         *
         * @return the full SqlTemplateSubstitution tokenization map.
         */
        fun build(): Map<SqlTemplateSubstitution, String> {
            val toReturn = mutableMapOf<SqlTemplateSubstitution, String>()

            toReturn[DISTINCT] = when (distinct) {
                true -> " distinct "
                else -> ""
            }

            toReturn[TABLE] = table
            toReturn[WHERE] = when (where.isEmpty()) {
                true -> ""
                else -> " where $where "
            }

            // All Done.
            return toReturn
        }
    }

    companion object {

        /**
         * Starting point to retrieve a MapBuilder.
         *
         * @return an empty MapBuilder.
         */
        @JvmStatic
        fun builder(): MapBuilder = MapBuilder()
    }
}
