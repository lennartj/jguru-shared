/*-
 * #%L
 * Nazgul Project: jguru-shared-persistence-spi-hibernate
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
package se.jguru.shared.persistence.spi.hibernate

import org.hibernate.boot.model.naming.Identifier
import org.hibernate.boot.model.naming.PhysicalNamingStrategy
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment
import org.slf4j.LoggerFactory
import java.io.Serializable
import java.util.Locale

/**
 * An enumeration identifying different database structures.
 */
enum class DbStructure {

    CATALOG,

    SCHEMA,

    SEQUENCE,

    TABLE,

    COLUMN
}

/**
 * Simple [PhysicalNamingStrategy] implementation which can inject a separate algorithm for each
 * type of database object in its constructor.
 */
open class SimplePhysicalNamingStrategy @JvmOverloads constructor(
    private val catalogConverter: (Identifier?, DbStructure) -> Identifier? = { id, struct -> toLowerCase(id, struct) },
    private val schemaConverter: (Identifier?, DbStructure) -> Identifier? = { id, struct -> toLowerCase(id, struct) },
    private val tableConverter: (Identifier?, DbStructure) -> Identifier? = { id, struct -> toLowerCase(id, struct) },
    private val sequenceConverter: (Identifier?, DbStructure) -> Identifier? = { id, struct -> toLowerCase(id, struct) },
    private val columnConverter: (Identifier?, DbStructure) -> Identifier? = { id, struct -> toLowerCase(id, struct) }
) : PhysicalNamingStrategy, Serializable {

    override fun toPhysicalCatalogName(name: Identifier?, context: JdbcEnvironment?): Identifier? = catalogConverter.invoke(name, DbStructure.CATALOG)

    override fun toPhysicalSchemaName(name: Identifier?, context: JdbcEnvironment?): Identifier? = schemaConverter.invoke(name, DbStructure.SCHEMA)

    override fun toPhysicalTableName(name: Identifier?, context: JdbcEnvironment?): Identifier? = tableConverter.invoke(name, DbStructure.TABLE)

    override fun toPhysicalSequenceName(name: Identifier?, context: JdbcEnvironment?): Identifier? = sequenceConverter.invoke(name, DbStructure.SEQUENCE)

    override fun toPhysicalColumnName(name: Identifier?, context: JdbcEnvironment?): Identifier? = columnConverter.invoke(name, DbStructure.COLUMN)

    companion object {

        /**
         * Singleton instance, if required.
         */
        @JvmStatic
        val INSTANCE = SimplePhysicalNamingStrategy()

        /**
         * Locale for lowerCase conversion.
         */
        @JvmStatic
        var CONVERT_LOCALE: Locale = Locale("sv", "SE")

        /**
         * Performs lowerCase conversion in [CONVERT_LOCALE] Locale.
         *
         * @param identifier The [Identifier] received by Hibernate.
         * @param dbStructure An identifier defining where the ca
         */
        @JvmStatic
        fun toLowerCase(identifier: Identifier?, dbStructure: DbStructure): Identifier? {

            val struct = dbStructure.name.toLowerCase(CONVERT_LOCALE)

            if (identifier == null) {

                if (log.isDebugEnabled) {
                    log.debug("toLowerCase mapped $struct: [null] --> [null]")
                }

                return null
            }

            val givenText = identifier.text
            val resultingText = identifier.text.trim().toLowerCase(CONVERT_LOCALE)

            if (log.isDebugEnabled) {
                log.debug("toLowerCase mapped $struct: [$givenText] --> [$resultingText]")
            }

            // All Done.
            return Identifier.toIdentifier(resultingText)
        }

        @JvmStatic
        private val SNAKE_CASE_REGEX = "([a-z])([A-Z])".toRegex()

        @JvmStatic
        private val SNAKE_CASE_REPLACEMENT = "$1_$2"

        /**
         * Performs snakeCase conversion in swedish Locale.
         */
        @JvmStatic
        fun toSnakeCase(identifier: Identifier?, dbStructure: DbStructure): Identifier? {

            val struct = dbStructure.name.toLowerCase(CONVERT_LOCALE)

            if (identifier == null) {

                if (log.isDebugEnabled) {
                    log.debug("toSnakeCase mapped $struct: [null] --> [null]")
                }

                return null
            }

            val givenText = identifier.text
            val resultingText = identifier.text.replace(SNAKE_CASE_REGEX, SNAKE_CASE_REPLACEMENT).toLowerCase(CONVERT_LOCALE)

            if (log.isDebugEnabled) {
                log.debug("toSnakeCase mapped $struct: [$givenText] --> [$resultingText]")
            }

            return Identifier.toIdentifier(resultingText)
        }

        @JvmStatic
        private val log = LoggerFactory.getLogger(SimplePhysicalNamingStrategy::class.java)
    }
}
