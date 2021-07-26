package se.jguru.shared.persistence.spi.jpa.converter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.sql.Date
import java.time.LocalDate
import java.time.ZoneId

/**
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
class LocalDateAttributeConverterTest {

    private var zoneId = ZoneId.systemDefault()

    private val transportForm = "2015-04-25"
    private val theDate = LocalDate.parse(transportForm)
    private val theInstant = theDate.atStartOfDay()

    private val zonedDateTime = theInstant.atOffset(zoneId.rules.getOffset(theInstant)).toZonedDateTime()

    private val objectForm = Date(zonedDateTime.toInstant().toEpochMilli())
    private val unitUnderTest = LocalDateAttributeConverter()

    @Test
    fun validateConvertingToTransportForm() {

        // Assemble

        // Act
        val result = unitUnderTest.convertToDatabaseColumn(theDate)

        // Assert
        assertThat(unitUnderTest.convertToDatabaseColumn(null)).isNull()
        assertThat(result).isEqualTo(objectForm)
    }

    @Test
    fun validateConvertingFromTransportForm() {

        // Assemble

        // Act
        val result = unitUnderTest.convertToEntityAttribute(objectForm)

        // Assert
        assertThat(unitUnderTest.convertToEntityAttribute(null)).isNull()
        assertThat(result).isEqualTo(theDate)
    }
}