package se.jguru.shared.algorithms.api

import org.junit.Assert
import org.junit.Test
import java.util.ArrayList
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
            Assert.assertEquals(expectedMsg, expected.message)
        } catch (e: Exception) {
            Assert.fail("Expected IllegalArgumentException, but got " + e)
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
            Assert.assertEquals(expectedMsg, expected.message)
        } catch (e: Exception) {
            Assert.fail("Expected IllegalArgumentException, but got " + e)
        }

    }

    @Test(expected = IllegalArgumentException::class)
    fun validateCollectionEmptyness() {

        // Assemble
        val aCollection = ArrayList<String>()

        // Act & Assert
        Validate.notEmpty<String, List<String>>(aCollection, "aCollection")
    }

    @Test(expected = IllegalArgumentException::class)
    fun validateMapEmptyness() {

        // Assemble
        val anEmptySortedMap = TreeMap<String, Double>()

        // Act & Assert
        Validate.notEmpty<String, Double, SortedMap<String, Double>>(anEmptySortedMap, "anEmptySortedMap")
    }

    @Test
    fun validateErrorMessageOnNullArgumentWithNullName() {

        // Act & Assert
        try {
            Validate.notNull<Any>(null, "")
        } catch (expected: NullPointerException) {
            Assert.assertEquals("Cannot handle null argument.", expected.message)
        } catch (e: Exception) {
            Assert.fail("Expected IllegalArgumentException, but got " + e)
        }

    }

    @Test
    fun validateReturningSameInstance() {

        // Assemble
        val toValidate = StringBuilder()

        // Act
        val result = Validate.notNull(toValidate, "toValidate")

        // Assert
        Assert.assertSame(toValidate, result)
    }
}