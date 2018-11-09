package se.jguru.shared.jaxb.spi.adapters

import org.junit.Assert
import org.junit.Test
import java.util.Currency

/**
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class CurrencyAdapterTest {

    private val transportForm = "SEK"
    private val objectForm = Currency.getInstance("SEK")
    private val unitUnderTest = CurrencyAdapter()

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