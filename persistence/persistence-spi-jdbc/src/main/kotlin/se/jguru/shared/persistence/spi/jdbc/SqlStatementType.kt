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

/**
 * Enumeration defining the type of an SQL statement.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
enum class SqlStatementType {

    /**
     * Statement which intends to persist data. Typically an SQL create statement.
     */
    CREATE,

    /**
     * Statement which intends to (only) read data. Typically an SQL select statement.
     */
    READ,

    /**
     * Statement which intends to update data. Typically an SQL update statement.
     */
    UPDATE,

    /**
     * Statement which intends to delete data. Typically an SQL delete statement.
     */
    DELETE
}
