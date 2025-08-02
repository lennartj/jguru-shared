package se.jguru.shared.jaxb.spi.shared.adapters

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.Arrays
import java.util.Locale
import java.util.TreeMap


/**
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class LocaleAdapterTest {

    private val log : Logger = LoggerFactory.getLogger(LocaleAdapterTest::class.java)

    private val transportForms = listOf(null, "se", "se", "se-SE", "se-SE", "se-SE-x-lvariant-FI")
    private val expectedTransportForms = listOf(null, "se", "se", "se-SE", "se-SE", "se-SE-x-lvariant-FI")
    private val objectForms = listOf(null,
            Locale.of("se"),
            Locale.of("se"),
            Locale.of("se", "SE"),
            Locale.of("se", "SE"),
            Locale.of("se", "SE", "FI"))

    private val unitUnderTest = LocaleAdapter()

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
            assertThat(results[i]).isEqualTo(expectedTransportForms[i])
        }
    }

    @Test
    fun validateConvertingFromTransportForm() {

        // Assemble
        val results = arrayOfNulls<Locale>(transportForms.size)

        // Act
        for (i in transportForms.indices) {
            results[i] = unitUnderTest.unmarshal(transportForms[i])
        }

        // Assert
        for (i in results.indices) {
            assertThat(results[i]).isEqualTo(objectForms[i])
        }
    }

    @Test
    fun validateConvertingUsingLanguageTag() {

        // Assemble
        val theOddNonMatchingLanguageTag = "nn-NO"
        val languageTag2Locale = TreeMap<String, Locale>()
        Arrays.stream(Locale.getAvailableLocales())
                .filter { c ->

                    val languageTag = c.toLanguageTag()

                    // Perform the filtering
                    !languageTag2Locale.keys.contains(languageTag) &&
                            !theOddNonMatchingLanguageTag.equals(languageTag, ignoreCase = true)
                }
                .forEach { c -> languageTag2Locale.put(c.toLanguageTag(), c) }

        // Act
        // languageTag2Locale.forEach((key1, value1) -> System.out.println("[" + key1 + "]: " + value1));
        val parsed = TreeMap<String, Locale>()
        languageTag2Locale.keys.forEach { k -> parsed.put(k, Locale.forLanguageTag(k)) }

        // Assert
        assertThat(parsed.size.toLong()).isEqualTo(languageTag2Locale.size.toLong())
        languageTag2Locale.forEach { (key, value) ->

            val reParsedValue = parsed[key]

            if (value != reParsedValue) {
                log.info("[$key]: $value ($reParsedValue)")
            }

            // Check sanity
            assertThat(reParsedValue).isEqualTo(value)
        }
    }
}