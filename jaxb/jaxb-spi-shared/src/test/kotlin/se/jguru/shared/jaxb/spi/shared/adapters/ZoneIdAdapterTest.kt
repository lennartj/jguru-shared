package se.jguru.shared.jaxb.spi.shared.adapters

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZoneId

/**
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class ZoneIdAdapterTest {

    private val log : Logger = LoggerFactory.getLogger(ZoneIdAdapterTest::class.java)

    private val transportForm = "Europe/Stockholm"
    private val objectForm = ZoneId.of("Europe/Stockholm")
    private val unitUnderTest = ZoneIdAdapter()

    @Test
    fun validateConvertingToTransportForm() {

        // Assemble

        // Act
        val result = unitUnderTest.marshal(objectForm)
        // log.info("Got: $result")

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