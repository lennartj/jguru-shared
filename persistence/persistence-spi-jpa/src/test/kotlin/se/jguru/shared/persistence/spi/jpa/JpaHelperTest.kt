package se.jguru.shared.persistence.spi.jpa

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import se.jguru.shared.persistence.spi.jpa.classloading.CommonPersistenceProvidersInfo
import java.util.Arrays

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
        assertThat(persistenceProviders).isNotNull
        assertThat(persistenceProviders.size).isEqualTo(commonProviders.size)

        Arrays.stream(commonProviders)
            .map { it.getProviderClassName() }
            .forEach { current ->
                assertThat(persistenceProviders.firstOrNull { it::class.java.name == current }).isNotNull
            }
    }
}