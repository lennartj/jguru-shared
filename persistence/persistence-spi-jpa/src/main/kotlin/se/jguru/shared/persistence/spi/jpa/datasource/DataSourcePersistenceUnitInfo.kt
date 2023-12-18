/*-
 * #%L
 * Nazgul Project: jguru-shared-persistence-spi-jpa
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
package se.jguru.shared.persistence.spi.jpa.datasource

import se.jguru.codestyle.annotations.UseOpenMembers
import se.jguru.shared.persistence.spi.jpa.classloading.CommonPersistenceProvidersInfo
import se.jguru.shared.persistence.spi.jpa.classloading.PersistenceProviderInfo
import java.net.URL
import java.util.Properties
import jakarta.persistence.SharedCacheMode
import jakarta.persistence.ValidationMode
import jakarta.persistence.spi.ClassTransformer
import jakarta.persistence.spi.PersistenceUnitInfo
import jakarta.persistence.spi.PersistenceUnitTransactionType
import javax.sql.DataSource

/**
 * ## DataSource PersistenceUnitInfo wrapper
 *
 * Simplifies performing JPA tests, by providing a simple, purely programmatic and non-resource-hungry
 * JPA PersistenceUnitInfo implementation.
 *
 * @param datasource The [DataSource] to wrap
 * @param persistenceProviderInfo The [PersistenceProviderInfo] used to create the JPA-enabled objects.
 * @param persistenceUnitName An arbitrary persistence unit name.
 * @param classLoader The [ClassLoader] used to load JPA domain classes. This should include the classpath of the
 * JARs containing your Entity domain model classes.
 */
@Suppress("RedundantModalityModifier")
@UseOpenMembers
open class DataSourcePersistenceUnitInfo @JvmOverloads constructor(
    private val datasource: DataSource,
    private val persistenceProviderInfo : PersistenceProviderInfo = CommonPersistenceProvidersInfo.HIBERNATE,
    private val persistenceUnitName : String = "DatasourceBasedPersistenceUnit",
    private val classLoader: ClassLoader = Thread.currentThread().contextClassLoader
) : PersistenceUnitInfo {

    var tempClassLoader: ClassLoader? = null

    override fun getPersistenceUnitRootUrl(): URL = DataSourcePersistenceUnitInfo::class.java.getResource("")

    override fun getJtaDataSource(): DataSource = datasource

    override fun getNonJtaDataSource(): DataSource = datasource

    override fun getMappingFileNames(): MutableList<String> = mutableListOf()

    override fun getNewTempClassLoader(): ClassLoader = when (tempClassLoader) {
        null -> classLoader
        else -> tempClassLoader!!
    }

    override fun getPersistenceUnitName(): String = persistenceUnitName

    override fun getSharedCacheMode(): SharedCacheMode = SharedCacheMode.UNSPECIFIED

    override fun getClassLoader(): ClassLoader = classLoader

    override fun getTransactionType(): PersistenceUnitTransactionType = PersistenceUnitTransactionType.RESOURCE_LOCAL

    override fun getProperties(): Properties = Properties()

    override fun getPersistenceXMLSchemaVersion(): String = "2.1"

    override fun addTransformer(transformer: ClassTransformer?) {
        // Do Nothing.
    }

    override fun getManagedClassNames(): MutableList<String> = mutableListOf()

    override fun getJarFileUrls(): MutableList<URL> = mutableListOf()

    override fun getPersistenceProviderClassName(): String = persistenceProviderInfo.getProviderClassName()

    override fun excludeUnlistedClasses(): Boolean = false

    override fun getValidationMode(): ValidationMode = ValidationMode.NONE
}
