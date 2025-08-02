package se.jguru.shared.jaxb.spi.metro

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.xmlunit.builder.DiffBuilder
import org.xmlunit.diff.DefaultNodeMatcher
import org.xmlunit.diff.ElementSelectors
import se.jguru.shared.algorithms.api.resources.PropertyResources
import se.jguru.shared.jaxb.spi.metro.people.Beverage
import se.jguru.shared.jaxb.spi.metro.people.DrinkingPreferences
import jakarta.xml.bind.JAXBContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class MetroMarshallerAndUnmarshallerTest {

    private val log : Logger = LoggerFactory.getLogger(MetroMarshallerAndUnmarshallerTest::class.java)

    // Shared state
    private var originalContextFactory: String? = null
    lateinit var unitUnderTest: ReferenceImplementationMarshallerAndUnmarshaller

    @BeforeEach
    fun setupSharedState() {

        originalContextFactory = System.getProperty(JAXBContext.JAXB_CONTEXT_FACTORY)
        System.setProperty(JAXBContext.JAXB_CONTEXT_FACTORY, "org.glassfish.jaxb.runtime.v2.ContextFactory")

        unitUnderTest = ReferenceImplementationMarshallerAndUnmarshaller()
        unitUnderTest.namespacePrefixResolver.put(Beverage.NAMESPACE, "bev")
    }

    @AfterEach
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
        val result = unitUnderTest.marshal(toMarshal = arrayOf(prefs))

        // Assert
        assertThat(result).isNotNull()

        val myDiffIdentical = DiffBuilder.compare(expected)
            .withTest(result)
            .normalizeWhitespace()
            .withNodeMatcher(DefaultNodeMatcher(ElementSelectors.byName))
            .checkForIdentical()
            .build()

        assertThat(myDiffIdentical.hasDifferences()).isFalse
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
        assertThat(result).isNotNull
        assertThat(expected.compareTo(result)).isEqualTo(0)
    }
}