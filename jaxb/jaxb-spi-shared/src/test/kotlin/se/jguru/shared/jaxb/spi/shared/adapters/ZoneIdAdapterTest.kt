package se.jguru.shared.jaxb.spi.shared.adapters

import org.junit.Assert
import org.junit.jupiter.api.Test
import java.time.ZoneId

/**
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class ZoneIdAdapterTest {

    private val transportForm = "Europe/Stockholm"
    private val objectForm = ZoneId.of("Europe/Stockholm")
    private val unitUnderTest = ZoneIdAdapter()

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