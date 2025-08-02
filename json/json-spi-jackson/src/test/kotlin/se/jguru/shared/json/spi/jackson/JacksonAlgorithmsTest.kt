package se.jguru.shared.json.spi.jackson

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import se.jguru.shared.algorithms.api.resources.PropertyResources
import se.jguru.shared.json.spi.jackson.people.DrinkingPreferences
import se.jguru.shared.json.spi.jackson.people.Person
import se.jguru.shared.json.spi.jackson.simplified.TimeFormats
import se.jguru.shared.json.spi.jackson.validation.Animal
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.MonthDay
import java.time.Period
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.SortedMap
import java.util.TreeMap

val stockholmTimezone : ZoneId = ZoneId.of("Europe/Stockholm")

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class JacksonAlgorithmsTest {

    private val log : Logger = LoggerFactory.getLogger(JacksonAlgorithmsTest::class.java)

    // Shared state
    lateinit var prefs: DrinkingPreferences
    lateinit var lennart: Person
    lateinit var timeFormats: TimeFormats
    lateinit var collectionMap: SortedMap<Long, List<String>>

    @BeforeEach
    fun setupSharedState() {

        prefs = DrinkingPreferences.createPrefs()
        lennart = prefs.people.first()

        val localDate = LocalDate.of(2019, Month.FEBRUARY, 1)
        val localTime = LocalTime.of(16, 45)

        timeFormats = TimeFormats(
            ZonedDateTime.of(localDate, localTime, stockholmTimezone),
            LocalDateTime.of(localDate, localTime),
            localDate,
            localTime,
            Duration.of(3L, ChronoUnit.DAYS).plusHours(2).plusMinutes(5),
            MonthDay.of(Month.MARCH, 21),
            Period.ofYears(2).plusMonths(1).plusDays(6)
        )

        collectionMap = TreeMap()
        collectionMap[1L] = listOf("a", "b", "c")
        collectionMap[2L] = listOf("d", "e", "f")
    }

    @Test
    fun validateJsonSerialization() {

        // Assemble
        val expectedCompactFormat = PropertyResources.readFully("testdata/people/person_compact.json")
        val expectedPrettyFormat = PropertyResources.readFully("testdata/people/person_humanReadable.json")

        // Act
        val compactFormat = JacksonAlgorithms.serialize(lennart, compactOutput = true)
        val humanReadableFormat = JacksonAlgorithms.serialize(lennart)

        // Assert
        JSONAssert.assertEquals(compactFormat, humanReadableFormat, true)
        assertThat(compactFormat).isNotEqualTo(humanReadableFormat)
        assertThat(compactFormat.compareTo(humanReadableFormat)).isNotEqualTo(0)

        JSONAssert.assertEquals(expectedCompactFormat, compactFormat, true)
        JSONAssert.assertEquals(expectedPrettyFormat, humanReadableFormat, true)
        assertThat(expectedCompactFormat).isEqualTo(compactFormat)
    }

    @Test
    fun validateJsonDeserialization() {

        // Assemble
        val data = PropertyResources.readFully("testdata/people/person_compact.json")

        // Act
        val result = JacksonAlgorithms.deserialize(data, Person::class.java)

        // Assert
        assertThat(result).isNotNull
        assertThat(result).isEqualTo(lennart)
    }

    @Test
    fun validateComplexObjectSerialization() {

        // Assemble
        val expected = PropertyResources.readFully("testdata/people/default_prefs.json")

        // Act
        val result = JacksonAlgorithms.serialize(prefs)
        // log.debug("Got: $result")

        // Assert
        JSONAssert.assertEquals(expected, result, true)
    }

    @Test
    fun validateComplexObjectDeserialization() {

        // Assemble
        val data = PropertyResources.readFully("testdata/people/default_prefs.json")

        // Act
        val result = JacksonAlgorithms.deserialize(data, DrinkingPreferences::class.java)

        // Assert
        assertThat(result).isNotNull
        assertThat(result).isEqualTo(prefs)

        val anders = result.people.find { it.name == "Anders" }
        val lennart = result.people.find { it.name == "Lennart" }

        assertThat(lennart?.beverage).isEqualTo(anders?.beverage)
        assertThat(lennart?.beverage).isSameAs(anders?.beverage)
    }

    @Test
    fun validateChangingJsonPropertyName() {

        // Assemble
        val data = PropertyResources.readFully("testdata/validation/fluffy.json")
        val dog = Animal("Fluffy", 3)

        // Act
        val result = JacksonAlgorithms.serialize(dog)
        // log.debug("Got: $result")

        // Assert
        JSONAssert.assertEquals(data, result, true)
    }

    @Test
    fun validateDeserializingIncorrectRepresentation() {

        // Assemble
        val data = PropertyResources.readFully("testdata/validation/incorrect_animal.json")

        // Act & Assert
        assertThatExceptionOfType(UnrecognizedPropertyException::class.java).isThrownBy {
            JacksonAlgorithms.deserialize(data, Animal::class.java)
        }
    }

    @Test
    fun validateCreatingJsonSchema() {

        // Assemble
        val expected = PropertyResources.readFully("testdata/validation/animal_schema.json")

        // Act
        val result = JacksonAlgorithms.getSchemaAsString(Animal::class.java)

        // Assert
        JSONAssert.assertEquals(expected, result, true)
    }

    @Test
    fun validateCreatingJsonSchemaWithTitleAndDescription() {

        // Assemble
        val expected = PropertyResources.readFully("testdata/validation/animal_schema_with_title.json")

        // Act
        val result = JacksonAlgorithms.getSchemaAsString(
            Animal::class.java,
            title = "The schema Title",
            description = "Some longwinded description"
        )
        // log.debug("Got: $result")
        
        // Assert
        JSONAssert.assertEquals(expected, result, true)
    }

    @Test
    fun validateTimeFormatSerialization() {

        // Assemble
        val expected = PropertyResources.readFully("testdata/simplified/timeformats.json")

        // Act
        val result = JacksonAlgorithms.serialize(timeFormats)
        // log.debug("Got: $result")

        // Assert
        JSONAssert.assertEquals(expected, result, true)
    }

    @Test
    fun validateTimeFormatDeserialization() {

        // Assemble
        val data = PropertyResources.readFully("testdata/simplified/timeformats.json")

        // Act
        val resurrected = JacksonAlgorithms.deserialize(data, TimeFormats::class.java)
        // log.debug("Got: $resurrected")

        // Assert
        assertThat(resurrected).isNotNull
        assertThat(resurrected.someDateTime).isEqualTo(timeFormats.someDateTime)
        assertThat(resurrected.someDate).isEqualTo(timeFormats.someDate)
        assertThat(resurrected.someTime).isEqualTo(timeFormats.someTime)
        assertThat(resurrected.someDuration).isEqualTo(timeFormats.someDuration)
        assertThat(resurrected.someMonthDay).isEqualTo(timeFormats.someMonthDay)
        assertThat(resurrected.somePeriod).isEqualTo(timeFormats.somePeriod)
    }

    @Test
    fun validateMapSerialization() {

        // Assemble
        val expected = PropertyResources.readFully("testdata/simplified/sortedMap.json")

        // Act
        val result = JacksonAlgorithms.serialize(collectionMap)
        // log.debug("Got: $result")

        // Assert
        JSONAssert.assertEquals(expected, result, true)
    }

    @Test
    fun validateMapDeserialization() {

        // Assemble
        val data = PropertyResources.readFully("testdata/simplified/sortedMap.json")
        // val mapper = ObjectMapperBuilder.getDefault()

        // Act
        val resurrected = JacksonAlgorithms.deserializeMap(data, Long::class.java, ArrayList::class.java)
        log.debug("Got: {}, of type {}", resurrected, resurrected::class.java.simpleName)

        // Assert
        assertThat(resurrected).isNotNull
        assertThat(resurrected).isInstanceOf(HashMap::class.java)
        assertThat(resurrected.size).isEqualTo(collectionMap.size)

        collectionMap.forEach { (key, value) ->
            val actual = resurrected[key]

            assertThat(actual).isNotNull
            assertThat(actual!!.size).isEqualTo(value.size)

            for (i in value.indices) {
                assertThat(actual[i]).isEqualTo(value[i])
            }
        }
    }
}