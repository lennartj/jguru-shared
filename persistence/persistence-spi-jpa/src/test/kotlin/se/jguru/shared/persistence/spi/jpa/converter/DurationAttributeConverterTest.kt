package se.jguru.shared.persistence.spi.jpa.converter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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

    @BeforeEach
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
            assertThat(unitUnderTest.convertToDatabaseColumn(null)).isNull()
            assertThat(result).isEqualTo(current.key)
        }
    }

    @Test
    fun validateConvertingFromTransportForm() {

        for (current in allDurations) {

            // Act
            val result = unitUnderTest.convertToEntityAttribute(current.key)

            // Assert
            assertThat(unitUnderTest.convertToEntityAttribute(null)).isNull()
            assertThat(result).isEqualTo(current.value)
        }
    }
}