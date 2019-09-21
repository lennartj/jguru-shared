package se.jguru.shared.persistence.spi.jpa.converter

import org.junit.Assert
import org.junit.Before
import org.junit.Test
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

    @Before
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
            Assert.assertNull(unitUnderTest.convertToDatabaseColumn(null))
            Assert.assertEquals(current.key, result)
        }
    }

    @Test
    fun validateConvertingFromTransportForm() {

        for (current in allYearMonths) {

            // Act
            val result = unitUnderTest.convertToEntityAttribute(current.key)

            // Assert
            Assert.assertNull(unitUnderTest.convertToEntityAttribute(null))
            Assert.assertEquals(current.value, result)
        }
    }
}