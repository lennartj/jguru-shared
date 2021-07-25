package se.jguru.shared.jaxb.spi.eclipselink

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.xmlunit.builder.DiffBuilder
import org.xmlunit.diff.DefaultNodeMatcher
import org.xmlunit.diff.Diff
import org.xmlunit.diff.ElementSelectors
import se.jguru.shared.algorithms.api.resources.PropertyResources
import se.jguru.shared.algorithms.api.xml.MarshallingFormat
import se.jguru.shared.jaxb.spi.eclipselink.people.Beverage
import se.jguru.shared.jaxb.spi.eclipselink.people.DrinkingPreferences
import javax.xml.bind.JAXBContext

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class EclipseLinkMarshallerAndUnmarshallerTest {

    // Shared state
    var originalContextFactory: String? = null
    lateinit var unitUnderTest: MoxyMarshallerAndUnmarshaller

    @Before
    fun setupSharedState() {

        originalContextFactory = System.getProperty(JAXBContext.JAXB_CONTEXT_FACTORY)
        System.setProperty(JAXBContext.JAXB_CONTEXT_FACTORY, "org.eclipse.persistence.jaxb.JAXBContextFactory")

        unitUnderTest = MoxyMarshallerAndUnmarshaller()
        unitUnderTest.namespacePrefixResolver.put(Beverage.NAMESPACE, "bev")
    }

    @After
    fun teardownSharedState() {
        if (originalContextFactory == null) {
            System.clearProperty(JAXBContext.JAXB_CONTEXT_FACTORY)
        } else {
            System.setProperty(JAXBContext.JAXB_CONTEXT_FACTORY, originalContextFactory)
        }
    }

    @Test
    fun validateMarshallingToXML() {

        // Assemble
        val resourcePath = "testdata/people/drinkingPreferences.xml"
        val prefs = DrinkingPreferences.createPrefs()

        val expected = PropertyResources.readFully(resourcePath = resourcePath)

        // Act
        val result = unitUnderTest.marshal(arrayOf(prefs))

        // Assert
        Assert.assertNotNull(result)

        val normalizedDiff: Diff = DiffBuilder.compare(expected)
            .withTest(result)
            .normalizeWhitespace()
            .withNodeMatcher(DefaultNodeMatcher(ElementSelectors.byName))
            .checkForIdentical()
            .build()
        Assert.assertFalse(normalizedDiff.hasDifferences());
    }

    @Test
    fun validateUnmarshallingFromXML() {

        // Assemble
        val resourcePath = "testdata/people/drinkingPreferences.xml"
        val expected = DrinkingPreferences.createPrefs()
        val data = PropertyResources.readFully(resourcePath = resourcePath)

        // Act
        val result = unitUnderTest.unmarshal(resultType = DrinkingPreferences::class.java, toUnmarshal = data)

        // Assert
        Assert.assertNotNull(result)
        Assert.assertEquals(0, expected.compareTo(result))
    }

    @Test
    fun validateMarshallingToJSON() {

        // Assemble
        val resourcePath = "testdata/people/drinkingPreferences.json"
        val prefs = DrinkingPreferences.createPrefs()

        val expected = PropertyResources.readFully(resourcePath = resourcePath)

        // Act
        val result = unitUnderTest.marshal(toMarshal = arrayOf(prefs), format = MarshallingFormat.JSON)
        println("got: $result")

        // Assert
        Assert.assertNotNull(result)
        JSONAssert.assertEquals(expected, result, true)
    }

    @Test
    fun validateUnmarshallingFromJSON() {

        // Assemble
        val resourcePath = "testdata/people/drinkingPreferences.json"
        val expected = DrinkingPreferences.createPrefs()
        val data = PropertyResources.readFully(resourcePath = resourcePath)

        // Act
        val result = unitUnderTest.unmarshal(
            resultType = DrinkingPreferences::class.java,
            toUnmarshal = data,
            format = MarshallingFormat.JSON
        )

        // Assert
        Assert.assertNotNull(result)
        Assert.assertEquals(0, expected.compareTo(result))
    }
}