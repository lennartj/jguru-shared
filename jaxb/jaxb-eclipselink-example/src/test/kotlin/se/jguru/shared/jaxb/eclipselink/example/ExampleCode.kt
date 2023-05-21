package se.jguru.shared.jaxb.eclipselink.example

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.xmlunit.builder.DiffBuilder
import org.xmlunit.diff.DefaultNodeMatcher
import org.xmlunit.diff.Diff
import org.xmlunit.diff.ElementSelectors
import se.jguru.shared.algorithms.api.resources.PropertyResources
import se.jguru.shared.jaxb.spi.eclipselink.MoxyMarshallerAndUnmarshaller
import jakarta.xml.bind.JAXBContext

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class ExampleCode {

    // Shared state
    lateinit var ale: Beverage
    lateinit var stout: Beverage
    lateinit var porter: Beverage

    lateinit var lennart: Person
    lateinit var malin: Person
    lateinit var lasse: Person
    lateinit var anders: Person

    lateinit var prefs: DrinkingPreferences
    lateinit var moxyMarsh: MoxyMarshallerAndUnmarshaller

    @BeforeEach
    fun setupSharedState() {

        ale = Beverage("Avenyn Ale")
        stout = Beverage("Idjit")
        porter = Beverage("Ostronporter")

        lennart = Person("Lennart", 254, ale)
        malin = Person("Malin", 32, stout)
        lasse = Person("Lasse", 52, porter)
        anders = Person("Anders", 42, ale)

        prefs = DrinkingPreferences(listOf(lennart, malin, lasse, anders))

        // Create the MOXy MAUM and configure it slightly
        moxyMarsh = MoxyMarshallerAndUnmarshaller()
        moxyMarsh.namespacePrefixResolver.put(Beverage.NAMESPACE, "bev")
        moxyMarsh.namespacePrefixResolver.put("http://typical/people", "tp")
    }

    @AfterEach
    fun teardownSharedState() {
        System.clearProperty(JAXBContext.JAXB_CONTEXT_FACTORY)
    }

    @Test
    fun showMarshallingToXML() {

        // Assemble
        val expected = PropertyResources.readFully("testdata/drinkingPrefs.xml")

        // Act
        val result = moxyMarsh.marshal(arrayOf(prefs))
        // println("Got: $result")

        // Assert
        val normalizedDiff: Diff = DiffBuilder.compare(expected)
            .withTest(result)
            .normalizeWhitespace()
            .withNodeMatcher(DefaultNodeMatcher(ElementSelectors.byName))
            .checkForIdentical()
            .build()
        assertThat(normalizedDiff.hasDifferences()).isFalse
    }

    @Test
    fun showUnmarshallingFromXML() {

        // Assemble
        val data = PropertyResources.readFully("testdata/drinkingPrefs.xml")

        // Act
        val resurrected = moxyMarsh.unmarshal(DrinkingPreferences::class.java, data)

        // Assert
        assertThat(resurrected).isNotNull
        assertThat(prefs.compareTo(resurrected)).isEqualTo(0)
    }
}