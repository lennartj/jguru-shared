package se.jguru.shared.spi.jpa.converter

import org.junit.Assert
import org.junit.Test
import java.util.Locale

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class LocaleAttributeConverterTest {

    private val transportForms = arrayOf(null, "se", "se", "se-SE", "se-SE", "se-SE-x-lvariant-FI")
    private val expectedTransportForms = arrayOf(null, "se", "se", "se-SE", "se-SE", "se-SE-x-lvariant-FI")
    private val objectForms = arrayOf(null,
        Locale("se"),
        Locale("se"),
        Locale("se", "SE"),
        Locale("se", "SE"),
        Locale("se", "SE", "FI"))

    private val unitUnderTest = LocaleAttributeConverter()

    @Test
    fun validateConvertingToTransportForm() {

        // Assemble
        val results = arrayOfNulls<String>(transportForms.size)

        // Act
        for (i in objectForms.indices) {
            results[i] = unitUnderTest.convertToDatabaseColumn(objectForms[i])
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
            results[i] = unitUnderTest.convertToEntityAttribute(transportForms[i])
        }

        // Assert
        for (i in results.indices) {
            Assert.assertEquals(objectForms[i], results[i])
        }
    }
}