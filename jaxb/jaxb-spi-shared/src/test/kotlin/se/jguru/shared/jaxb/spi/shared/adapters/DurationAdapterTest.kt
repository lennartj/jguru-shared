package se.jguru.shared.jaxb.spi.shared.adapters

import org.junit.Assert
import org.junit.Before
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.SortedMap
import java.util.TreeMap

class DurationAdapterTest {

    private val allDurations: SortedMap<String, Duration> = TreeMap()
    private val unitUnderTest = DurationAdapter()

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
            val result = unitUnderTest.marshal(current.value)

            // Assert
            Assert.assertNull(unitUnderTest.marshal(null))
            Assert.assertEquals(current.key, result)
        }
    }

    @Test
    fun validateConvertingFromTransportForm() {

        for (current in allDurations) {

            // Act
            val result = unitUnderTest.unmarshal(current.key)

            // Assert
            Assert.assertNull(unitUnderTest.unmarshal(null))
            Assert.assertEquals(current.value, result)
        }
    }
}