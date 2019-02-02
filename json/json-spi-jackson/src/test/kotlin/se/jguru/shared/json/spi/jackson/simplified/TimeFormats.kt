package se.jguru.shared.json.spi.jackson.simplified

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
data class TimeFormats(
    var someDateTime : LocalDateTime,
    var someDate : LocalDate,
    var someTime : LocalTime,
    var someDuration : Duration)