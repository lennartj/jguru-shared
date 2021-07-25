package se.jguru.shared.messaging.api

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class JmsCompliantMapTest {

    @Test
    fun validateExceptionOnAttemptingToInsertIncorrectType() {

        // Assemble
        val unitUnderTest = JmsCompliantMap()

        // Act & Assert
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
            unitUnderTest["blah"] = StringBuffer()
        }
    }

    @Test
    fun validateNormalInsertion() {

        // Assemble
        val unitUnderTest = JmsCompliantMap()

        // Act
        unitUnderTest["foo"] = "bar"

        // Assert
        assertThat(unitUnderTest.size).isEqualTo(1)
        assertThat(unitUnderTest["foo"]).isEqualTo("bar")
    }
}