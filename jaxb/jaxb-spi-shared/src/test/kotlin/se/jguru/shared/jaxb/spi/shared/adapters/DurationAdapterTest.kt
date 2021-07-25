package se.jguru.shared.jaxb.spi.shared.adapters

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.SortedMap
import java.util.TreeMap

class DurationAdapterTest {

    private val allDurations: SortedMap<String, Duration> = TreeMap()
    private val unitUnderTest = DurationAdapter()

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
            val result = unitUnderTest.marshal(current.value)

            // Assert
            assertThat(unitUnderTest.marshal(null)).isNull()
            assertThat(result).isEqualTo(current.key)
        }
    }

    @Test
    fun validateConvertingFromTransportForm() {

        for (current in allDurations) {

            // Act
            val result = unitUnderTest.unmarshal(current.key)

            // Assert
            assertThat(unitUnderTest.unmarshal(null)).isNull()
            assertThat(result).isEqualTo(current.value)
        }
    }
}