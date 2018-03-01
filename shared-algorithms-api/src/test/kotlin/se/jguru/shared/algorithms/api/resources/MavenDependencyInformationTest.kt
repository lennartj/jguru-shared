package se.jguru.shared.algorithms.api.resources

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.SortedMap

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class MavenDependencyInformationTest {

    // Shared state
    lateinit var propertyMap : SortedMap<String, String>

    @Before
    fun setupSharedState() {

        propertyMap = PropertyResources.parseResource(resourcePath = "testdata/introspection/dependencies.properties")

        Assert.assertNotNull(propertyMap)
        Assert.assertEquals(40, propertyMap.size)

        propertyMap.entries.forEach { println("[${it.key}]: ${it.value}") }
    }

    @Test
    fun validateParsingSingleDependencyInfo() {

        // Assemble
        /*
        [org.slf4j/slf4j-api/scope]: compile
        [org.slf4j/slf4j-api/type]: jar
        [org.slf4j/slf4j-api/version]: 1.7.25
        */

        // Act
        val result = MavenDependencyInformation.parse("org.slf4j", "slf4j-api", propertyMap)

        // Assert
        Assert.assertNotNull(result)
        Assert.assertEquals("1.7.25", result.mavenVersion)
        Assert.assertEquals("org.slf4j", result.groupID)
        Assert.assertEquals("slf4j-api", result.artifactID)
        Assert.assertEquals(DependencyScope.COMPILE, result.scope)
    }

    @Test
    fun validateParsingDependencyInformation() {

        // Assemble

        // Act
        val result = MavenDependencyInformation.parse(propertyMap)

        // Assert
        Assert.assertNotNull(result)
        Assert.assertEquals(12, result.size)
    }
}