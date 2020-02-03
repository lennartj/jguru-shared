/*-
 * #%L
 * Nazgul Project: jguru-shared-persistence-spi-jpa
 * %%
 * Copyright (C) 2018 jGuru Europe AB
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
package se.jguru.shared.persistence.spi.jpa

import java.io.Serializable
import java.util.TreeMap

/**
 * Most of the standardized `javax.persistence.*` properties are enumerated within this class.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
enum class PersistenceProperty(val property: String,
                               val description: String) : Serializable {

    PROVIDER("javax.persistence.provider", "Class name of the provider to use."),

    TRANSACTION_TYPE("javax.persistence.transactionType",
        "Accepts values: { RESOURCE_LOCAL | JTA }. Type of transactions to use. " +
            "In Java SE the default is RESOURCE_LOCAL. In Java EE the default is JTA."),

    JTA_DATASOURCE("javax.persistence.jtaDataSource",
        "JNDI name of a (transactional) JTA data source."),

    NON_JTA_DATASOURCE("javax.persistence.nonJtaDataSource",
        "JNDI name of a (non-transactional) data source."),

    JDBC_URL("javax.persistence.jdbc.url", "The JDBC URL used to connect to the database."),

    JDBC_DRIVER("javax.persistence.jdbc.driver", "The JDBC Driver fully qualified class name."),

    JDBC_USER("javax.persistence.jdbc.user", "The database username."),

    JDBC_PASSWORD("javax.persistence.jdbc.password", "The database password."),

    QUERY_TIMEOUT("javax.persistence.query.timeout", "The JPQL Query timeout."),

    SHARED_CACHE_MODE("javax.persistence.sharedCache.mode", "The mode of using shared cache."),

    VALIDATION_MODE("javax.persistence.validation.mode", "Defines how to validate entities."),

    VALIDATION_GROUP_PRE_PERSIST("javax.persistence.validation.group.pre-persist	",
        "Defines which entity groups to validate before persisting them."),

    VALIDATION_GROUP_PRE_UPDATE("javax.persistence.validation.group.pre-update",
        "Defines which entity groups to validate before updating them."),

    VALIDATION_GROUP_PRE_REMOVE("javax.persistence.validation.group.pre-remove",
        "Defines which entity groups to validate before removing them."),

    VALIDATION_FACTORY("javax.persistence.validation.factory",
        "The classname of the validation factory used."),

    SCHEMA_GENERATION_DB_ACTION("javax.persistence.schema-generation.database.action	",
        "Accepts values: { create | drop | drop-and-create | none }."),

    SCHEMA_GENERATION_SCRIPTS_ACTION("javax.persistence.schema-generation.scripts.action	",
        "Accepts values: { create | drop | drop-and-create | none	 }."),

    SCHEMA_GENERATION_SCRIPTS_CREATE_FILENAME("javax.persistence.schema-generation.scripts.create-target	",
        "The name/path of a file to which create-Schema SQL is written."),

    SCHEMA_GENERATION_SCRIPTS_DROP_FILENAME("javax.persistence.schema-generation.scripts.drop-target	",
        "The name/path of a file to which drop-Schema SQL is written."),

    SCHEMA_GENERATION_CREATE_SCRIPT_SOURCE("javax.persistence.schema-generation.create-script-source	",
        "Defines the location of the create script. This parameter has to " +
            "be defined, if the javax.persistence.schema-generation.create-source " +
            "property was set to script, metadata-then-script or " +
            "script-then-metadata."),

    SCHEMA_GENERATION_DROP_SCRIPT_SOURCE("javax.persistence.schema-generation.drop-script-source	",
        "Defines the location of the drop script. This parameter has to be " +
            "defined if the javax.persistence.schema-generation.drop-source " +
            "property was set to script, metadata-then-script or " +
            "script-then-metadata."),

    SQL_LOAD_SCRIPT_SOURCE("javax.persistence.sql-load-script-source",
        "Defines the location of the SQL script that shall " +
            "be used load data into the database.")
}

/**
 * ## PersistenceProperty builder
 *
 * Permits chaining properties on the form:
 * `PersistencePropertyBuilder().with(JDBC_DRIVER.property, "some.driver").build()`
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class PersistencePropertyBuilder @JvmOverloads constructor(

    private val props: MutableMap<String, String> = TreeMap()) {

    /**
     * Simple
     *
     * @param name The persistence property name
     * @param value The persistence property corresponding value.
     */
    fun withProperty(name: String, value: String): PersistencePropertyBuilder {

        props[name] = value

        return this
    }

    /**
     * Convenience builder method which the supplied value for the give [PersistenceProperty].
     *
     * @param prop The [PersistenceProperty] wrapping the standard JPA property name.
     * @param value The value of the supplied PersistenceProperty.
     */
    fun with(prop: PersistenceProperty, value: String): PersistencePropertyBuilder =
        this.withProperty(prop.property, value)

    /**
     * @return The completed Map with JPA properties.
     */
    fun build() = props
}
