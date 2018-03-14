package se.jguru.shared.algorithms.api

import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Arrays
import java.util.Locale
import java.util.TimeZone
import java.util.TreeMap

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class WellKnownTimeZonesTest {

    // Shared state
    lateinit var anInstant: Instant
    lateinit var timestamp : ZonedDateTime

    @Before
    fun setupSharedState() {

        this.anInstant = Instant.ofEpochMilli(1000L)
        timestamp = ZonedDateTime.ofInstant(this.anInstant, ZoneId.of("GMT"))

        Arrays.stream(Locale.getAvailableLocales())
            .map { "[${it.toLanguageTag()}]: ${it.getDisplayLanguage(Locale.ENGLISH)}" }
            .forEach { println(it) }
    }

    @Test
    fun validateConversions() {

        // Assemble

        // Act
        Arrays.stream(WellKnownTimeZones.values())
            .forEach { println("[${it.name}]: ${it.id}, ${it.getTimeZone().displayName}") }

        // Assert
    }

    @Test
    fun validateTimeZones() {

        // Assemble
        val id2Zone = TreeMap<String, ZoneId>()
        val id2TimeZone = TreeMap<String, TimeZone>()
        ZoneId.getAvailableZoneIds()
            .sorted()
            .forEach { id2Zone[it] = ZoneId.of(it) }

        TimeZone.getAvailableIDs()
            .sorted()
            .forEach { id2TimeZone[it] = TimeZone.getTimeZone(it) }

        id2TimeZone.entries
            .sortedBy { it.key }
            .forEach { println("[${it.key}]:  ZoneID (${id2Zone[it.key]}) and TimeZoneID (${it.value.id})") }

        // Act
        /*
        fun toStyles(theId: ZoneId, locale: Locale) : String = TextStyle
                .values()
                .map { "[${it.name}: ${theId.getDisplayName(it, locale)}]" }
                .reduce { l, r -> l + ", " + r}

        id2Zone.entries
            .sortedBy { it.key }
            .forEach { println("[${it.key}] -> " + toStyles(it.value, Locale.ENGLISH)) }

        id2Zone.entries
            .sortedBy { it.key }
            .forEach { println("[${it.key}] -> " + it.value.rules.) }
            */

        /*
        id2Zone.entries
            .stream()
            .filter { it.key.contains("sv", true) }
            .forEach { println("[${it.key}]: ${it.value.normalized()}") }
            */

        // Assert

    }

}