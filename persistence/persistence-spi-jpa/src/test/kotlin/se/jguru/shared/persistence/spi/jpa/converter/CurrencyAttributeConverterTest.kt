package se.jguru.shared.persistence.spi.jpa.converter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.Currency
import java.util.SortedMap
import java.util.TreeMap

private val log : Logger = LoggerFactory.getLogger(CurrencyAttributeConverterTest::class.java)

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class CurrencyAttributeConverterTest {

    private val code2ObjectForm : SortedMap<String, Currency> = TreeMap()
    private val unitUnderTest = CurrencyAttributeConverter()

    @BeforeEach
    fun setupSharedState() {

        Currency.getAvailableCurrencies()
            .filter {

                val currentCode = it.currencyCode
                val alreadyPresent = code2ObjectForm.containsKey(currentCode)

                if(alreadyPresent) {
                    log.error("Currency Code [$currentCode] occurs more than 1 time!")
                } 

                !alreadyPresent }
            .forEach { code2ObjectForm[it.currencyCode] = it }

        if(log.isDebugEnabled) {
            code2ObjectForm.entries.forEach {
                log.debug("[${it.key}]: ${it.value.displayName} (${it.value.symbol})")
            }
        }
    }

    @Test
    fun validateConvertingToTransportForm() {

        code2ObjectForm.forEach { (code, currency) ->
            assertThat(unitUnderTest.convertToEntityAttribute(code)).isSameAs(currency)
        }
    }

    @Test
    fun validateConvertingFromTransportForm() {

        code2ObjectForm.forEach { (code, currency) ->
            assertThat(unitUnderTest.convertToDatabaseColumn(currency)).isEqualTo(code)
        }
    }
}