package se.jguru.shared.persistence.spi.jpa.converter

import org.junit.Assert
import org.junit.Test
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
        Assert.assertNull(unitUnderTest.convertToDatabaseColumn(null))
        Assert.assertEquals(objectForm, result)
    }

    @Test
    fun validateConvertingFromTransportForm() {

        // Assemble

        // Act
        val result = unitUnderTest.convertToEntityAttribute(objectForm)

        // Assert
        Assert.assertNull(unitUnderTest.convertToEntityAttribute(null))
        Assert.assertEquals(theDate, result)
    }
}