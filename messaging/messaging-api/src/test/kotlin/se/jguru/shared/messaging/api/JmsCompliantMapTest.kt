package se.jguru.shared.messaging.api

import org.junit.Assert
import org.junit.Test

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class JmsCompliantMapTest {

    @Test(expected = IllegalArgumentException::class)
    fun validateExceptionOnAttemptingToInsertIncorrectType() {

        // Assemble
        val unitUnderTest = JmsCompliantMap()

        // Act & Assert
        unitUnderTest["blah"] = StringBuffer()
    }

    @Test
    fun validateNormalInsertion() {

        // Assemble
        val unitUnderTest = JmsCompliantMap()

        // Act
        unitUnderTest["foo"] = "bar"

        // Assert
        Assert.assertEquals(1, unitUnderTest.size)
        Assert.assertEquals("bar", unitUnderTest["foo"])
    }
}