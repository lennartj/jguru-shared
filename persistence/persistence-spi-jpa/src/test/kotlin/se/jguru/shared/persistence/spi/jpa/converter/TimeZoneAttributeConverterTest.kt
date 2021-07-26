package se.jguru.shared.persistence.spi.jpa.converter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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

    @BeforeEach
    fun setupSharedState() {
        Arrays.stream(TimeZone.getAvailableIDs()).forEach { allTimeZones.put(it, TimeZone.getTimeZone(it)) }
    }

    @Test
    fun validateConvertingToTransportForm() {

        for(current in allTimeZones) {

            // Act
            val result = unitUnderTest.convertToDatabaseColumn(current.value)

            // Assert
            assertThat(unitUnderTest.convertToDatabaseColumn(null)).isNull()
            assertThat(result).isEqualTo(current.key)
        }
    }

    @Test
    fun validateConvertingFromTransportForm() {

        for(current in allTimeZones) {

            // Act
            val result = unitUnderTest.convertToEntityAttribute(current.key)

            // Assert
            assertThat(unitUnderTest.convertToEntityAttribute(null)).isNull()
            assertThat(result).isEqualTo(current.value)
        }
    }
}