package se.jguru.shared.jaxb.spi.shared.adapters

import org.junit.Assert
import org.junit.jupiter.api.Test
import java.time.Period


/**
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class PeriodAdapterTest {

    private val transportForms = arrayOf(null, "P1D", "P7D", "P1M7D", "P1Y", "P1Y1M1D")
    private val objectForms = arrayOf(null,
        Period.ofDays(1),
        Period.ofWeeks(1),
        Period.ofWeeks(1).plusMonths(1),
        Period.ofYears(1),
        Period.ofDays(1).plusMonths(1).plusYears(1))

    private val unitUnderTest = PeriodAdapter()

    @Test
    fun validateConvertingToTransportForm() {

        // Assemble
        val results = arrayOfNulls<String>(transportForms.size)

        // Act
        for (i in objectForms.indices) {
            results[i] = unitUnderTest.marshal(objectForms[i])
        }

        // Assert
        for (i in results.indices) {
            Assert.assertEquals(transportForms[i], results[i])
        }
    }

    @Test
    fun validateConvertingFromTransportForm() {

        // Assemble
        val results = arrayOfNulls<Period>(transportForms.size)

        // Act
        for (i in transportForms.indices) {
            results[i] = unitUnderTest.unmarshal(transportForms[i])
        }

        // Assert
        for (i in results.indices) {
            Assert.assertEquals(objectForms[i], results[i])
        }
    }
}