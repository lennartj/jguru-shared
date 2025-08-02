package se.jguru.shared.jaxb.spi.shared.adapters

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.Currency

/**
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class CurrencyAdapterTest {

  private val log : Logger = LoggerFactory.getLogger(CurrencyAdapterTest::class.java)

  private val transportForm = "SEK"
  private val objectForm = Currency.getInstance("SEK")
  private val unitUnderTest = CurrencyAdapter()

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