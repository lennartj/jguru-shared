package se.jguru.shared.entity.test

import org.eclipse.persistence.jaxb.JAXBContextFactory
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import se.jguru.shared.entity.test.people.DrinkingPreferences
import javax.xml.bind.JAXBContext

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class EclipseLinkMarshallerAndUnmarshallerTest {

    // Shared state
    var originalContextFactory : String? = null
    lateinit var unitUnderTest : EclipseLinkMarshallerAndUnmarshaller

    @Before
    fun setupSharedState() {

        originalContextFactory = System.getProperty(JAXBContext.JAXB_CONTEXT_FACTORY)
        System.setProperty(JAXBContext.JAXB_CONTEXT_FACTORY, "org.eclipse.persistence.jaxb.JAXBContextFactory")

        unitUnderTest = EclipseLinkMarshallerAndUnmarshaller()
    }

    @After
    fun teardownSharedState() {
        if(originalContextFactory == null) {
            System.clearProperty(JAXBContext.JAXB_CONTEXT_FACTORY)
        } else {
            System.setProperty(JAXBContext.JAXB_CONTEXT_FACTORY, originalContextFactory)
        }
    }

    @Test
    fun validateMarshallingToXML() {

        // Assemble
        val prefs = DrinkingPreferences.createPrefs()

        // Act
        val result = unitUnderTest.marshal(toMarshal = prefs)

        // Assert
        Assert.assertNotNull(result)
    }
}