package se.jguru.shared.algorithms.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class ZonedDateTimeServiceTest {

    @Test
    fun printKnownZoneIDs() {

        // Act
        ZoneId.getAvailableZoneIds()
            .map { it.toString() }
            .sorted()
            .forEach { println(it) }
    }

    @Test
    fun validateDefaultValues() {

        // Assemble
        val now = ZonedDateTime.now();
        val unitUnderTest = object : ZonedDateTimeService {}

        // Act & Assert
        validateDates(unitUnderTest, now)
    }

    @Test
    fun validateOverridingNowYieldsDifferentDate() {

        // Assemble
        val stockholmZoneId = ZoneId.of("Europe/Stockholm")
        val then = ZonedDateTime.of(
            LocalDateTime.of(2018, Month.FEBRUARY, 2, 15, 30),
            stockholmZoneId)

        val unitUnderTest = object : ZonedDateTimeService {
            override fun getZoned(): ZonedDateTime = then
        }

        // Act
        validateDates(unitUnderTest, then)
    }

    //
    // Private helpers
    //

    private fun validateDates(unitUnderTest: ZonedDateTimeService, now: ZonedDateTime) {

        // Act
        val defaultZonedNow = unitUnderTest.getZoned()
        val defaultNow = unitUnderTest.getNow()
        val defaultToday = unitUnderTest.getToday()

        // Assert
        assertThat(now.offset).isEqualTo(defaultZonedNow.offset)

        val diffInSeconds = defaultZonedNow.toEpochSecond() - now.toEpochSecond()
        assertThat(diffInSeconds).isLessThan(1)
        assertThat(defaultToday).isEqualTo(now.toLocalDate())

        val localDiffInSeconds = defaultNow.toEpochSecond(defaultZonedNow.offset) - now
            .toLocalDateTime()
            .toEpochSecond(now.offset)

        assertThat(localDiffInSeconds).isLessThan(1)
        assertThat(defaultToday).isEqualTo(now.toLocalDate())
    }
}