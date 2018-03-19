package se.jguru.shared.spi.jaxb.reference

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.xmlunit.builder.DiffBuilder
import org.xmlunit.diff.DefaultNodeMatcher
import org.xmlunit.diff.ElementSelectors
import se.jguru.shared.algorithms.api.resources.PropertyResources
import se.jguru.shared.spi.jaxb.people.Beverage
import se.jguru.shared.spi.jaxb.people.DrinkingPreferences
import javax.xml.bind.JAXBContext

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class ReferenceMarshallerAndUnmarshallerTest {

    // Shared state
    var originalContextFactory: String? = null
    lateinit var unitUnderTest: ReferenceImplementationMarshallerAndUnmarshaller

    @Before
    fun setupSharedState() {

        originalContextFactory = System.getProperty(JAXBContext.JAXB_CONTEXT_FACTORY)
        System.setProperty(JAXBContext.JAXB_CONTEXT_FACTORY, "com.sun.xml.internal.bind.v2.ContextFactory")

        unitUnderTest = ReferenceImplementationMarshallerAndUnmarshaller()
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
        val result = unitUnderTest.marshal(toMarshal = prefs)

        // Assert
        Assert.assertNotNull(result)

        val myDiffIdentical = DiffBuilder.compare(expected)
            .withTest(result)
            .normalizeWhitespace()
            .withNodeMatcher(DefaultNodeMatcher(ElementSelectors.byName))
            .checkForIdentical()
            .build()
        Assert.assertFalse(myDiffIdentical.hasDifferences());
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
}