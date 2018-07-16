package se.jguru.shared.persistence.spi.jpa.converter

import org.junit.Assert
import org.junit.Test
import java.time.DayOfWeek

/**
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
class DayOfWeekAttributeConverterTest {

    private val unitUnderTest = DayOfWeekAttributeConverter()

    @Test
    fun validateConvertingToTransportForm() {

        // Assemble

        // Act
        val result = unitUnderTest.convertToDatabaseColumn(DayOfWeek.THURSDAY)

        // Assert
        Assert.assertNull(unitUnderTest.convertToDatabaseColumn(null))
        Assert.assertEquals(DayOfWeek.THURSDAY.value, result)
    }

    @Test
    fun validateConvertingFromTransportForm() {

        // Assemble
        val theDay = DayOfWeek.WEDNESDAY

        // Act
        val result = unitUnderTest.convertToEntityAttribute(theDay.value)

        // Assert
        Assert.assertNull(unitUnderTest.convertToEntityAttribute(null))
        Assert.assertEquals(theDay, result)
    }
}