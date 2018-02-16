package se.jguru.shared.spi.jaxb.adapter

import org.junit.Assert
import org.junit.Test
import java.time.LocalDate
import java.time.Month

/**
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class LocalDateAdapterTest {

    private val transportForm = "2015-04-25"
    private val objectForm = LocalDate.of(2015, Month.APRIL, 25)
    private val unitUnderTest = LocalDateAdapter()

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