package se.jguru.shared.persistence.spi.jpa.converter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
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
        assertThat(unitUnderTest.convertToDatabaseColumn(null)).isNull()
        assertThat(result).isEqualTo(DayOfWeek.THURSDAY.value)
    }

    @Test
    fun validateConvertingFromTransportForm() {

        // Assemble
        val theDay = DayOfWeek.WEDNESDAY

        // Act
        val result = unitUnderTest.convertToEntityAttribute(theDay.value)

        // Assert
        assertThat(unitUnderTest.convertToEntityAttribute(null)).isNull()
        assertThat(result).isEqualTo(theDay)
    }
}