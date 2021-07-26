package se.jguru.shared.persistence.spi.jpa.converter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
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

    @BeforeEach
    fun setupSharedState() {
        ZoneId.getAvailableZoneIds().forEach { allZoneIDs[it] = ZoneId.of(it) }

        allZoneIDs.forEach { (anID, aZoneID) -> println("[$anID]: $aZoneID") }
    }

    @Test
    fun validateConvertingToTransportForm() {

        for (current in allZoneIDs) {

            // Act
            val result = unitUnderTest.convertToDatabaseColumn(current.value)

            // Assert
            assertThat(unitUnderTest.convertToDatabaseColumn(null)).isNull()
            assertThat(result).isEqualTo(current.key)
        }
    }

    @Test
    fun validateConvertingFromTransportForm() {

        for (current in allZoneIDs) {

            // Act
            val result = unitUnderTest.convertToEntityAttribute(current.key)

            // Assert
            assertThat(unitUnderTest.convertToEntityAttribute(null)).isNull()
            assertThat(result).isEqualTo(current.value)
        }
    }
}