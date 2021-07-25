package se.jguru.shared.persistence.spi.jpa.converter

import org.junit.Assert
import org.junit.Before
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.util.SortedMap
import java.util.TreeMap

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class ZoneIdAttributeConverterTest {

    private val allZoneIDs: SortedMap<String, ZoneId> = TreeMap()
    private val unitUnderTest = ZoneIdAttributeConverter()

    @Before
    fun setupSharedState() {
        ZoneId.getAvailableZoneIds().forEach { allZoneIDs[it] = ZoneId.of(it) }

        allZoneIDs.forEach { anID, aZoneID -> println("[$anID]: $aZoneID") }
    }

    @Test
    fun validateConvertingToTransportForm() {

        for (current in allZoneIDs) {

            // Act
            val result = unitUnderTest.convertToDatabaseColumn(current.value)

            // Assert
            Assert.assertNull(unitUnderTest.convertToDatabaseColumn(null))
            Assert.assertEquals(current.key, result)
        }
    }

    @Test
    fun validateConvertingFromTransportForm() {

        for (current in allZoneIDs) {

            // Act
            val result = unitUnderTest.convertToEntityAttribute(current.key)

            // Assert
            Assert.assertNull(unitUnderTest.convertToEntityAttribute(null))
            Assert.assertEquals(current.value, result)
        }
    }
}