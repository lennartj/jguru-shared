package se.jguru.shared.persistence.spi.jpa.converter

import org.junit.Assert
import org.junit.Before
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

    @Before
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
            Assert.assertNull(unitUnderTest.convertToDatabaseColumn(null))
            Assert.assertEquals(current.key, result)
        }
    }

    @Test
    fun validateConvertingFromTransportForm() {

        for(current in allMonthDays) {

            // Act
            val result = unitUnderTest.convertToEntityAttribute(current.key)

            // Assert
            Assert.assertNull(unitUnderTest.convertToEntityAttribute(null))
            Assert.assertEquals(current.value, result)
        }
    }
}