package se.jguru.shared.json.spi.jackson

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.skyscreamer.jsonassert.JSONAssert
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
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class JacksonAlgorithmsTest {

    // Shared state
    lateinit var prefs: DrinkingPreferences
    lateinit var lennart: Person
    lateinit var timeFormats: TimeFormats

    private fun normalizeSpace(s: String) = s.replace("\\s+".toRegex(), " ")

    @Before
    fun setupSharedState() {

        prefs = DrinkingPreferences.createPrefs()
        lennart = prefs.people.first()

        timeFormats = TimeFormats(
            LocalDateTime.of(2019, Month.FEBRUARY, 1, 16, 45),
            LocalDate.of(2019, Month.FEBRUARY, 1),
            LocalTime.of(16, 45),
            Duration.of(3L, ChronoUnit.DAYS).plusHours(2).plusMinutes(5)
        )
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
        Assert.assertFalse(compactFormat == humanReadableFormat)
        Assert.assertFalse(compactFormat.compareTo(humanReadableFormat) == 0)

        JSONAssert.assertEquals(expectedCompactFormat, compactFormat, true)
        JSONAssert.assertEquals(expectedPrettyFormat, humanReadableFormat, true)
        Assert.assertEquals(expectedCompactFormat, compactFormat)
    }

    @Test
    fun validateJsonDeserialization() {

        // Assemble
        val data = PropertyResources.readFully("testdata/people/person_compact.json")

        // Act
        val result = JacksonAlgorithms.deserialize(data, Person::class.java)

        // Assert
        Assert.assertNotNull(result)
        Assert.assertEquals(lennart, result)
    }

    @Test
    fun validateComplexObjectSerialization() {

        // Assemble
        val expected = PropertyResources.readFully("testdata/people/default_prefs.json")

        // Act
        val result = JacksonAlgorithms.serialize(prefs)
        println("Got: $result")

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
        Assert.assertNotNull(result)
        Assert.assertEquals(prefs, result)

        val anders = result.people.find { it.name == "Anders" }
        val lennart = result.people.find { it.name == "Lennart" }

        Assert.assertEquals(anders?.beverage, lennart?.beverage)
        Assert.assertSame(anders?.beverage, lennart?.beverage)
    }

    @Test
    fun validateChangingJsonPropertyName() {

        // Assemble
        val data = PropertyResources.readFully("testdata/validation/fluffy.json")
        val dog = Animal("Fluffy", 3)

        // Act
        val result = JacksonAlgorithms.serialize(dog)

        // Assert
        JSONAssert.assertEquals(data, result, true)
    }

    @Test(expected = UnrecognizedPropertyException::class)
    fun validateDeserializingIncorrectRepresentation() {

        // Assemble
        val data = PropertyResources.readFully("testdata/validation/incorrect_animal.json")

        // Act & Assert
        JacksonAlgorithms.deserialize(data, Animal::class.java)
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
            description = "Some longwinded description")

        // Assert
        JSONAssert.assertEquals(expected, result, true)
    }

    @Test
    fun validateTimeFormatSerialization() {

        // Assemble
        val expected = PropertyResources.readFully("testdata/simplified/timeformats.json")

        // Act
        val result = JacksonAlgorithms.serialize(timeFormats)
        println("Got: $result")

        // Assert
        JSONAssert.assertEquals(expected, result, true)
    }

    @Test
    fun validateTimeFormatDeserialization() {

        // Assemble
        val data = PropertyResources.readFully("testdata/simplified/timeformats.json")

        // Act
        val resurrected = JacksonAlgorithms.deserialize(data, TimeFormats::class.java)

        // Assert
        Assert.assertNotNull(resurrected)
        Assert.assertEquals(timeFormats, resurrected)
    }
}