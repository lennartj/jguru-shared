package se.jguru.shared.persistence.spi.jpa

import org.junit.Assert
import org.junit.Test
import se.jguru.shared.persistence.spi.jpa.classloading.CommonPersistenceProvidersInfo
import java.util.Arrays
import javax.persistence.EntityManagerFactory
import javax.persistence.Persistence
import javax.persistence.spi.PersistenceProvider

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class JpaHelperTest {

    @Test
    fun validateFetchingPersistenceProviders() {

        // Assemble
        val commonProviders = CommonPersistenceProvidersInfo.values()

        // Act
        val persistenceProviders = JpaHelper.persistenceProviders

        // Assert
        Assert.assertNotNull(persistenceProviders)
        Assert.assertEquals(commonProviders.size, persistenceProviders.size)

        Arrays.stream(commonProviders)
            .map { it.getProviderClassName() }
            .forEach { current -> Assert.assertNotNull(persistenceProviders.firstOrNull { it::class.java.name == current }) }
    }

    @Test
    fun validateCreatingSchema() {

        // Assemble

        // Act

        // Assert
    }
}