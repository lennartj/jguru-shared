package se.jguru.shared.jaxb.spi.shared.adapters

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.Month

/**
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class LocalDateAdapterTest {

    private val log : Logger = LoggerFactory.getLogger(LocalDateAdapterTest::class.java)

    private val transportForm = "2015-04-25"
    private val objectForm = LocalDate.of(2015, Month.APRIL, 25)
    private val unitUnderTest = LocalDateAdapter()

    @Test
    fun validateConvertingToTransportForm() {

        // Assemble

        // Act
        val result = unitUnderTest.marshal(objectForm)
        // log.debug("Got: $result");

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