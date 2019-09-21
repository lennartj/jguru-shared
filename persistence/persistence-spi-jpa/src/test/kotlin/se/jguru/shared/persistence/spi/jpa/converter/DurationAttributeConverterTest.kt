package se.jguru.shared.persistence.spi.jpa.converter

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.time.Duration
import java.util.SortedMap
import java.util.TreeMap

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class DurationAttributeConverterTest {

    private val allDurations: SortedMap<String, Duration> = TreeMap()
    private val unitUnderTest = DurationAttributeConverter()

    @Before
    fun setupSharedState() {
        listOf(
            Duration.ofDays(1),
            Duration.ofHours(1),
            Duration.ofMinutes(1),
            Duration.ofSeconds(1),
            Duration.ofDays(1)
                .plusHours(1)
                .plusMinutes(1)
                .plusSeconds(1)
                .plusMillis(1)
                .plusNanos(1))
            .forEach { allDurations[it.toString()] = it }
    }

    @Test
    fun validateConvertingToTransportForm() {

        for (current in allDurations) {

            // Act
            val result = unitUnderTest.convertToDatabaseColumn(current.value)

            // Assert
            Assert.assertNull(unitUnderTest.convertToDatabaseColumn(null))
            Assert.assertEquals(current.key, result)
        }
    }

    @Test
    fun validateConvertingFromTransportForm() {

        for (current in allDurations) {

            // Act
            val result = unitUnderTest.convertToEntityAttribute(current.key)

            // Assert
            Assert.assertNull(unitUnderTest.convertToEntityAttribute(null))
            Assert.assertEquals(current.value, result)
        }
    }
}