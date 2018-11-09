package se.jguru.shared.jaxb.spi.adapters

import org.junit.Assert
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month

/**
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
class LocalDateTimeAdapterTest {

    private val transportForm = "2015-04-25T15:40:00"
    private val objectForm = LocalDateTime.of(
            LocalDate.of(2015, Month.APRIL, 25),
            LocalTime.of(15, 40, 0))
    private val unitUnderTest = LocalDateTimeAdapter()

    @Test
    fun validateConvertingToTransportForm() {

        // Assemble

        // Act
        val result = unitUnderTest.marshal(objectForm)
        // System.out.println("Got: " + result);

        // Assert
        Assert.assertNull(unitUnderTest.marshal(null))
        Assert.assertEquals(transportForm, result)
    }

    @Test
    fun validateConvertingFromTransportForm() {

        // Assemble

        // Act
        val result = unitUnderTest.unmarshal(transportForm)

        // Assert
        Assert.assertNull(unitUnderTest.unmarshal(null))
        Assert.assertEquals(objectForm, result)
    }
}
