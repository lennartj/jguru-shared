package se.jguru.shared.jaxb.spi.shared.adapters

import org.junit.Assert
import org.junit.Test
import java.time.DayOfWeek

/**
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class DayOfWeekAdapterTest {

    private val transportForm = DayOfWeek.FRIDAY.value
    private val objectForm = DayOfWeek.FRIDAY
    private val unitUnderTest = DayOfWeekAdapter()

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