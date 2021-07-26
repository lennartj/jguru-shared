package se.jguru.shared.persistence.spi.jpa.converter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
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
        assertThat(unitUnderTest.convertToDatabaseColumn(null)).isNull()
        assertThat(result).isEqualTo(1984)
    }

    @Test
    fun validateConvertingFromTransportForm() {

        // Assemble
        val theYear = nineteenEightyFour

        // Act
        val result = unitUnderTest.convertToEntityAttribute(theYear.value)

        // Assert
        assertThat(unitUnderTest.convertToEntityAttribute(null)).isNull()
        assertThat(result).isEqualTo(theYear)
    }
}