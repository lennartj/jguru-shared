package se.jguru.shared.jaxb.spi.shared.adapters

import org.junit.Assert
import org.junit.Test
import java.util.Arrays
import java.util.Locale
import java.util.TreeMap


/**
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class LocaleAdapterTest {

    private val transportForms = arrayOf(null, "se", "se", "se-SE", "se-SE", "se-SE-x-lvariant-FI")
    private val expectedTransportForms = arrayOf(null, "se", "se", "se-SE", "se-SE", "se-SE-x-lvariant-FI")
    private val objectForms = arrayOf(null,
            Locale("se"),
            Locale("se"),
            Locale("se", "SE"),
            Locale("se", "SE"),
            Locale("se", "SE", "FI"))

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
            Assert.assertEquals(expectedTransportForms[i], results[i])
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
            Assert.assertEquals(objectForms[i], results[i])
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
        Assert.assertEquals(languageTag2Locale.size.toLong(), parsed.size.toLong())
        languageTag2Locale.forEach { key, value ->

            val reparsedValue = parsed[key]

            if (value != reparsedValue) {
                println("[$key]: $value ($reparsedValue)")
            }

            // Check sanity
            Assert.assertEquals(value, reparsedValue)
        }
    }
}