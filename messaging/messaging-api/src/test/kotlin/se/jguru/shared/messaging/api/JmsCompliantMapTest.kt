package se.jguru.shared.messaging.api

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class JmsCompliantMapTest {

    private val log : Logger = LoggerFactory.getLogger(JmsCompliantMapTest::class.java)

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
        log.info("Got $unitUnderTest")

        // Assert
        assertThat(unitUnderTest.size).isEqualTo(1)
        assertThat(unitUnderTest["foo"]).isEqualTo("bar")
    }
}