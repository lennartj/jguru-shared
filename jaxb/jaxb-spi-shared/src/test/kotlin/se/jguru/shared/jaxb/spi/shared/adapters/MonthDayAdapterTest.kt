package se.jguru.shared.jaxb.spi.shared.adapters

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Month
import java.time.MonthDay

/**
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class MonthDayAdapterTest {

    private val transportForms = arrayOf(null, "--01-09", "--01-10", "--01-11", "--09-09", "--09-10", "--09-11")
    private val objectForms = arrayOf(null,
        MonthDay.of(Month.JANUARY, 9),
        MonthDay.of(Month.JANUARY, 10),
        MonthDay.of(Month.JANUARY, 11),
        MonthDay.of(Month.SEPTEMBER, 9),
        MonthDay.of(Month.SEPTEMBER, 10),
        MonthDay.of(Month.SEPTEMBER, 11))

    private val unitUnderTest = MonthDayAdapter()

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
            assertThat(results[i]).isEqualTo(transportForms[i])
        }
    }

    @Test
    fun validateConvertingFromTransportForm() {

        // Assemble
        val results = arrayOfNulls<MonthDay>(transportForms.size)

        // Act
        for (i in transportForms.indices) {
            results[i] = unitUnderTest.unmarshal(transportForms[i])
        }

        // Assert
        for (i in results.indices) {
            assertThat(results[i]).isEqualTo(objectForms[i])
        }
    }
}