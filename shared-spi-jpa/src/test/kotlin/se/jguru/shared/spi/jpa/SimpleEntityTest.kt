package se.jguru.shared.spi.jpa

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import se.jguru.shared.spi.jpa.people.DrinkingPreferences
import se.jguru.shared.test.entity.MoxyMarshallerUnmarshallerRule

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class SimpleEntityTest {

    @Rule fun jaxbRule() = MoxyMarshallerUnmarshallerRule()

    // Shared state
    lateinit var drinkingPrefs : DrinkingPreferences

    @Before
    fun setupSharedState() {

        
    }

    @Test
    fun validatePersistingEntity() {

        // Assemble

        // Act

        // Assert
    }
}