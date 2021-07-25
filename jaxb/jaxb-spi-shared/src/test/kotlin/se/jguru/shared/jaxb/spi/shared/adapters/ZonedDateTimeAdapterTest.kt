package se.jguru.shared.jaxb.spi.shared.adapters

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import se.jguru.shared.algorithms.api.WellKnownTimeZones
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.time.ZonedDateTime

/**
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class ZoneDateTimeAdapterTest {

    private val transportForm = "2015-04-25T15:30:00+02:00[Europe/Stockholm]"
    private val objectForm = ZonedDateTime.of(
        LocalDate.of(2015, Month.APRIL, 25),
        LocalTime.of(15, 30, 0),
        WellKnownTimeZones.SWEDISH.getZoneId())
    private val unitUnderTest = ZonedDateTimeAdapter()

    @Test
    fun validateConvertingToTransportForm() {

        // Assemble

        // Act
        val result = unitUnderTest.marshal(objectForm)
        // System.out.println("Got: " + result);

        // Assert
        assertThat(unitUnderTest.marshal(null)).isNull()
        assertThat(result).isEqualTo(transportForm)
    }

    @Test
    fun validateConvertingFromTransportForm() {

        // Assemble

        // Act
        val result = unitUnderTest.unmarshal(transportForm)

        // Assert
        assertThat(unitUnderTest.unmarshal(null)).isNull()
        assertThat(result).isEqualTo(objectForm)
    }
}
