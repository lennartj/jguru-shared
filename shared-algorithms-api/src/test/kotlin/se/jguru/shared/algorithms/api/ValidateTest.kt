package se.jguru.shared.algorithms.api

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Test
import java.util.SortedMap
import java.util.TreeMap

/**
 *
 * @author [Lennart Jörelid](mailto:lj@jguru.se), jGuru Europe AB
 */
class ValidateTest {

    @Test
    fun validateErrorMessageOnSuppliedArgumentName() {

        // Assemble
        val argumentName = "fooBar"
        val expectedMsg = "Cannot handle empty 'fooBar' argument."

        // Act & Assert
        try {
            Validate.notEmpty("", argumentName)
        } catch (expected: IllegalArgumentException) {
            assertThat(expected.message).isEqualTo(expectedMsg)
        } catch (e: Exception) {
            fail("Expected IllegalArgumentException, but got $e")
        }
    }

    @Test
    fun validateErrorMessageOnNullArgument() {

        // Assemble
        val argumentName = "fooBar"
        val expectedMsg = "Cannot handle null 'fooBar' argument."

        // Act & Assert
        try {
            Validate.notNull<Any>(null, argumentName)
        } catch (expected: NullPointerException) {
            assertThat(expected.message).isEqualTo(expectedMsg)
        } catch (e: Exception) {
            fail("Expected IllegalArgumentException, but got $e")
        }
    }

    @Test
    fun validateCollectionEmptyness() {

        // Assemble
        val aCollection = ArrayList<String>()

        // Act & Assert
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
            Validate.notEmpty<String, List<String>>(aCollection, "aCollection")
        }
    }

    @Test
    fun validateMapEmptyness() {

        // Assemble
        val anEmptySortedMap = TreeMap<String, Double>()

        // Act & Assert
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
            Validate.notEmpty<String, Double, SortedMap<String, Double>>(anEmptySortedMap, "anEmptySortedMap")
        }
    }

    @Test
    fun validateErrorMessageOnNullArgumentWithNullName() {

        // Act & Assert
        try {
            Validate.notNull<Any>(null, "")
        } catch (expected: NullPointerException) {
            assertThat(expected.message).isEqualTo("Cannot handle null argument.")
        } catch (e: Exception) {
            fail("Expected IllegalArgumentException, but got $e")
        }

    }

    @Test
    fun validateReturningSameInstance() {

        // Assemble
        val toValidate = StringBuilder()

        // Act
        val result = Validate.notNull(toValidate, "toValidate")

        // Assert
        assertThat(result).isSameAs(toValidate)
    }
}