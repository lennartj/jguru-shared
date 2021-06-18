package se.jguru.shared.json.spi.jackson.simplified

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.MonthDay
import java.time.Period
import java.time.ZonedDateTime

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
data class TimeFormats(
    var someZonedDateTime: ZonedDateTime,
    var someDateTime: LocalDateTime,
    var someDate: LocalDate,
    var someTime: LocalTime,
    var someDuration: Duration,
    var someMonthDay: MonthDay,
    var somePeriod: Period
)