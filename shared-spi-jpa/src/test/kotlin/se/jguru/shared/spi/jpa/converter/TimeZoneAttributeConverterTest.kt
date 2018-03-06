package se.jguru.shared.spi.jpa.converter

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.Arrays
import java.util.SortedMap
import java.util.TimeZone
import java.util.TreeMap

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class TimeZoneAttributeConverterTest {

    private val allTimeZones : SortedMap<String, TimeZone> = TreeMap()
    private val unitUnderTest = TimeZoneAttributeConverter()

    @Before
    fun setupSharedState() {
        Arrays.stream(TimeZone.getAvailableIDs()).forEach { allTimeZones.put(it, TimeZone.getTimeZone(it)) }
    }

    @Test
    fun validateConvertingToTransportForm() {

        for(current in allTimeZones) {

            // Act
            val result = unitUnderTest.convertToDatabaseColumn(current.value)

            // Assert
            Assert.assertNull(unitUnderTest.convertToDatabaseColumn(null))
            Assert.assertEquals(current.key, result)
        }
    }

    @Test
    fun validateConvertingFromTransportForm() {

        for(current in allTimeZones) {

            // Act
            val result = unitUnderTest.convertToEntityAttribute(current.key)

            // Assert
            Assert.assertNull(unitUnderTest.convertToEntityAttribute(null))
            Assert.assertEquals(current.value, result)
        }
    }
}