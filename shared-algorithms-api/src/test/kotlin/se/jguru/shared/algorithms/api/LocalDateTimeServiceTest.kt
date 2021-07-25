package se.jguru.shared.algorithms.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneOffset

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class LocalDateTimeServiceTest {

    @Test
    fun validateDefaultValues() {

        // Assemble
        val now = LocalDateTime.now();
        val unitUnderTest = object : LocalDateTimeService {}

        // Act & Assert
        validateDates(unitUnderTest, now)
    }

    @Test
    fun validateOverridingNowYieldsDifferentDate() {

        // Assemble
        val then = LocalDateTime.of(2018, Month.FEBRUARY, 2, 15, 30)

        val unitUnderTest = object : LocalDateTimeService {
            override fun getNow(): LocalDateTime = then
        }

        // Act
        validateDates(unitUnderTest, then)
    }

    //
    // Private helpers
    //

    private fun validateDates(unitUnderTest: LocalDateTimeService, now: LocalDateTime) {

        // Act
        val defaultNow = unitUnderTest.getNow()
        val defaultToday = unitUnderTest.getToday()

        // Assert
        val diffInSeconds = defaultNow.toEpochSecond(ZoneOffset.UTC) - now.toEpochSecond(ZoneOffset.UTC)
        assertThat(diffInSeconds).isLessThan(1)
        assertThat(defaultToday).isEqualTo(now.toLocalDate())
    }
}