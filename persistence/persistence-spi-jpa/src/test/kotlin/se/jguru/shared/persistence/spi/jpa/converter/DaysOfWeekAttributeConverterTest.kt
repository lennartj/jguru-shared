package se.jguru.shared.persistence.spi.jpa.converter

import org.junit.Assert
import org.junit.Before
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

    @Before
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
        Assert.assertNull(unitUnderTest.convertToDatabaseColumn(null))
        Assert.assertEquals("1,2,3,4,5", weekdayResults)
        Assert.assertEquals("6,7", weekendResults)
    }

    @Test
    fun validateConvertingFromTransportForm() {

        // Assemble

        // Act
        val weekdays = unitUnderTest.convertToEntityAttribute("1,3,4,2,5")
        val nothing = unitUnderTest.convertToEntityAttribute("")
        val reallyNothing = unitUnderTest.convertToEntityAttribute(null)

        // Assert
        Assert.assertNotNull(reallyNothing)
        Assert.assertNotNull(nothing)
        Assert.assertTrue(nothing.isEmpty())
        Assert.assertTrue(reallyNothing.isEmpty())

        Assert.assertEquals(weekDays, weekdays)
    }

    @Test(expected = IllegalArgumentException::class)
    fun validateExceptionOnIncorrectWeekOfDayValue() {

        // Act & Assert
        val result = unitUnderTest.convertToEntityAttribute("1,2,42")

        println("Got: $result")
    }
}