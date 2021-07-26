package se.jguru.shared.persistence.spi.jpa.converter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Month
import java.time.MonthDay
import java.util.SortedMap
import java.util.TreeMap

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class MonthDayAttributeConverterTest {

    private val allMonthDays : SortedMap<String, MonthDay> = TreeMap()
    private val unitUnderTest = MonthDayAttributeConverter()

    @BeforeEach
    fun setupSharedState() {
        for(current in Month.values()) {
            for(i in 8 .. 25) {

                val currentMonthDay = MonthDay.of(current, i)
                allMonthDays[currentMonthDay.toString()] = currentMonthDay
            }
        }
    }

    @Test
    fun validateConvertingToTransportForm() {

        for(current in allMonthDays) {

            // Act
            val result = unitUnderTest.convertToDatabaseColumn(current.value)

            // Assert
            assertThat(unitUnderTest.convertToDatabaseColumn(null)).isNull()
            assertThat(result).isEqualTo(current.key)
        }
    }

    @Test
    fun validateConvertingFromTransportForm() {

        for(current in allMonthDays) {

            // Act
            val result = unitUnderTest.convertToEntityAttribute(current.key)

            // Assert
            assertThat(unitUnderTest.convertToEntityAttribute(null)).isNull()
            assertThat(result).isEqualTo(current.value)
        }
    }
}