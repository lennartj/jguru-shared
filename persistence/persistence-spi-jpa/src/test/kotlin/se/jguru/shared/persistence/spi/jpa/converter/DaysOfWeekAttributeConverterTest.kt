package se.jguru.shared.persistence.spi.jpa.converter

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.util.SortedSet
import java.util.TreeSet

/**
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
class DaysOfWeekAttributeConverterTest {

    // Shared state
    private lateinit var weekDays: SortedSet<DayOfWeek>
    private lateinit var weekEnds: SortedSet<DayOfWeek>
    private val unitUnderTest = DaysOfWeekAttributeConverter()

    @BeforeEach
    fun setupSharedState() {

        weekDays = TreeSet<DayOfWeek>(listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.THURSDAY
        ))

        weekEnds = TreeSet<DayOfWeek>(listOf(
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY))
    }

    @Test
    fun validateConvertingToTransportForm() {

        // Assemble

        // Act
        val weekdayResults = unitUnderTest.convertToDatabaseColumn(weekDays)
        val weekendResults = unitUnderTest.convertToDatabaseColumn(weekEnds)

        // Assert
        assertThat(unitUnderTest.convertToDatabaseColumn(null)).isNull()
        assertThat(weekdayResults).isEqualTo("1,2,3,4,5")
        assertThat(weekendResults).isEqualTo("6,7")
    }

    @Test
    fun validateConvertingFromTransportForm() {

        // Assemble

        // Act
        val weekdays = unitUnderTest.convertToEntityAttribute("1,3,4,2,5")
        val nothing = unitUnderTest.convertToEntityAttribute("")
        val reallyNothing = unitUnderTest.convertToEntityAttribute(null)

        // Assert
        assertThat(reallyNothing).isNotNull
        assertThat(nothing).isNotNull
        assertThat(nothing).isEmpty()
        assertThat(reallyNothing).isEmpty()

        assertThat(weekdays).isEqualTo(weekDays)
    }

    @Test
    fun validateExceptionOnIncorrectWeekOfDayValue() {

        // Act & Assert
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
            unitUnderTest.convertToEntityAttribute("1,2,42")
        }
    }
}