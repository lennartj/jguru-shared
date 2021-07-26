package se.jguru.shared.persistence.spi.jpa.converter

import org.assertj.core.api.Assertions.assertThat
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
        assertThat(unitUnderTest.convertToDatabaseColumn(null)).isNull()
        assertThat(result).isEqualTo(objectForm)
    }

    @Test
    fun validateConvertingFromTransportForm() {

        // Assemble

        // Act
        val result = unitUnderTest.convertToEntityAttribute(objectForm)

        // Assert
        assertThat(unitUnderTest.convertToEntityAttribute(null)).isNull()
        assertThat(result).isEqualTo(theTime)
    }
}