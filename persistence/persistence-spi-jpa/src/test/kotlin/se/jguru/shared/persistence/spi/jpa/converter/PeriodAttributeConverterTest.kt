package se.jguru.shared.persistence.spi.jpa.converter

import org.junit.Assert
import org.junit.Before
import org.junit.jupiter.api.Test
import java.time.Period
import java.util.SortedMap
import java.util.TreeMap

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class PeriodAttributeConverterTest {

    private val allPeriods: SortedMap<String, Period> = TreeMap()
    private val unitUnderTest = PeriodAttributeConverter()

    @Before
    fun setupSharedState() {
        listOf(
            Period.ofDays(1),
            Period.ofMonths(1),
            Period.ofWeeks(1),
            Period.ofYears(1),
            Period.ofYears(1).plusMonths(1).plusDays(1)
        ).forEach { allPeriods[it.toString()] = it }
    }

    @Test
    fun validateConvertingToTransportForm() {

        for (current in allPeriods) {

            // Act
            val result = unitUnderTest.convertToDatabaseColumn(current.value)

            // Assert
            Assert.assertNull(unitUnderTest.convertToDatabaseColumn(null))
            Assert.assertEquals(current.key, result)
        }
    }

    @Test
    fun validateConvertingFromTransportForm() {

        for (current in allPeriods) {

            // Act
            val result = unitUnderTest.convertToEntityAttribute(current.key)

            // Assert
            Assert.assertNull(unitUnderTest.convertToEntityAttribute(null))
            Assert.assertEquals(current.value, result)
        }
    }
}