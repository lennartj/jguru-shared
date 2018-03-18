package se.jguru.shared.spi.jaxb.eclipselink

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.xmlunit.builder.Input
import org.xmlunit.matchers.CompareMatcher.isSimilarTo
import se.jguru.shared.algorithms.api.resources.PropertyResources
import se.jguru.shared.spi.jaxb.people.Beverage
import se.jguru.shared.spi.jaxb.people.DrinkingPreferences
import javax.xml.bind.JAXBContext
import org.xmlunit.diff.ElementSelectors
import org.xmlunit.diff.DefaultNodeMatcher
import org.xmlunit.builder.DiffBuilder
import org.xmlunit.diff.Diff



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
}