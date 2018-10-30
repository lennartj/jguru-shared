package se.jguru.shared.persistence.spi.jpa.converter

import org.junit.Assert
import org.junit.Test
import java.time.Year

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class YearAttributeConverterTest {

    private val nineteenEightyFour = Year.of(1984)
    private val unitUnderTest = YearAttributeConverter()

    @Test
    fun validateConvertingToTransportForm() {

        // Assemble

        // Act
        val result = unitUnderTest.convertToDatabaseColumn(nineteenEightyFour)

        // Assert
        Assert.assertNull(unitUnderTest.convertToDatabaseColumn(null))
        Assert.assertEquals(1984, result)
    }

    @Test
    fun validateConvertingFromTransportForm() {

        // Assemble
        val theYear = nineteenEightyFour

        // Act
        val result = unitUnderTest.convertToEntityAttribute(theYear.value)

        // Assert
        Assert.assertNull(unitUnderTest.convertToEntityAttribute(null))
        Assert.assertEquals(theYear, result)
    }
}