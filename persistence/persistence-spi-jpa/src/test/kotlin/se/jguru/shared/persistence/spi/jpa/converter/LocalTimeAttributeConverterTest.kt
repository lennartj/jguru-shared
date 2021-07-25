package se.jguru.shared.persistence.spi.jpa.converter

import org.junit.Assert
import org.junit.jupiter.api.Test
import java.sql.Time
import java.time.LocalTime

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class LocalTimeAttributeConverterTest {

    private val theTime = LocalTime.parse("15:40:00")
    private val objectForm = Time.valueOf(theTime)

    private val unitUnderTest = LocalTimeAttributeConverter()

    @Test
    fun validateConvertingToTransportForm() {

        // Assemble

        // Act
        val result = unitUnderTest.convertToDatabaseColumn(theTime)

        // Assert
        Assert.assertNull(unitUnderTest.convertToDatabaseColumn(null))
        Assert.assertEquals(objectForm, result)
    }

    @Test
    fun validateConvertingFromTransportForm() {

        // Assemble

        // Act
        val result = unitUnderTest.convertToEntityAttribute(objectForm)

        // Assert
        Assert.assertNull(unitUnderTest.convertToEntityAttribute(null))
        Assert.assertEquals(theTime, result)
    }
}