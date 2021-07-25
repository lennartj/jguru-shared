package se.jguru.shared.persistence.spi.jpa.converter

import org.junit.Assert
import org.junit.jupiter.api.Test
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId

/**
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
class LocalDateTimeAttributeConverterTest {

    private var zoneId = ZoneId.systemDefault()

    private val transportForm = "2015-04-25T15:40:00"
    private val theDateTime = LocalDateTime.parse(transportForm)
    private val zonedDateTime = theDateTime.atOffset(zoneId.rules.getOffset(theDateTime)).toZonedDateTime()

    private val objectForm = Timestamp(zonedDateTime.toInstant().toEpochMilli())
    private val unitUnderTest = LocalDateTimeAttributeConverter()

    @Test
    fun validateConvertingToTransportForm() {

        // Assemble

        // Act
        val result = unitUnderTest.convertToDatabaseColumn(theDateTime)

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
        Assert.assertEquals(theDateTime, result)
    }
}