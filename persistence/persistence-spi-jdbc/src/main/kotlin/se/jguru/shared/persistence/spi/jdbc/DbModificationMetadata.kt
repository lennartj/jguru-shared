/*-
 * #%L
 * Nazgul Project: jguru-shared-persistence-spi-jdbc
 * %%
 * Copyright (C) 2018 - 2020 jGuru Europe AB
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

import java.io.Serializable

/**
 * ## Database operation metadata
 *
 * Contains retrieved metadata operations that update the database state.
 * (I.e. INSERT, UPDATE or DELETE operations).
 *
 * @param numRowsAffected The number of DB rows affected by the UPDATE, INSERT or DELETE statement executed.
 * @param generatedPrimaryKeys A list containing any generated (and requested) primary key
 * values from the corresponding DB operation. Will (normally) only be populated if a SQL INSERT operation
 * has been executed.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
data class DbModificationMetadata(
    var numRowsAffected: Int,
    var generatedPrimaryKeys: List<Any?> = listOf()) : Serializable
