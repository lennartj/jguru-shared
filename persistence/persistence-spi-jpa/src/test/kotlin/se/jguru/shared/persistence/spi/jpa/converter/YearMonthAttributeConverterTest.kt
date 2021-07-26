package se.jguru.shared.persistence.spi.jpa.converter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Month
import java.time.YearMonth
import java.util.SortedMap
import java.util.TreeMap

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class YearMonthAttributeConverterTest {

    private val allYearMonths: SortedMap<String, YearMonth> = TreeMap()
    private val unitUnderTest = YearMonthAttributeConverter()

    @BeforeEach
    fun setupSharedState() {
        for (currentYear in 2019..2025) {
            for (currentMonth in Month.values()) {

                val currentYearMonth = YearMonth.of(currentYear, currentMonth)
                allYearMonths[currentYearMonth.toString()] = currentYearMonth
            }
        }
    }

    @Test
    fun validateConvertingToTransportForm() {

        for (current in allYearMonths) {

            // Act
            val result = unitUnderTest.convertToDatabaseColumn(current.value)

            // Assert
            assertThat(unitUnderTest.convertToDatabaseColumn(null)).isNull()
            assertThat(result).isEqualTo(current.key)
        }
    }

    @Test
    fun validateConvertingFromTransportForm() {

        for (current in allYearMonths) {

            // Act
            val result = unitUnderTest.convertToEntityAttribute(current.key)

            // Assert
            assertThat(unitUnderTest.convertToEntityAttribute(null)).isNull()
            assertThat(result).isEqualTo(current.value)
        }
    }
}