package se.jguru.shared.algorithms.api.resources

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.SortedMap

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class MavenDependencyInformationTest {

    // Shared state
    lateinit var propertyMap : SortedMap<String, String>

    @BeforeEach
    fun setupSharedState() {

        propertyMap = PropertyResources.parseResource(resourcePath = "testdata/introspection/dependencies.properties")

        assertThat(propertyMap).isNotNull
        assertThat(propertyMap.size).isEqualTo(40)

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
        assertThat(result).isNotNull
        assertThat(result.mavenVersion).isEqualTo("1.7.25")
        assertThat(result.groupID).isEqualTo("org.slf4j")
        assertThat(result.artifactID).isEqualTo("slf4j-api")
        assertThat(result.scope).isEqualTo(DependencyScope.COMPILE)
    }

    @Test
    fun validateParsingDependencyInformation() {

        // Assemble
        // se.jguru.shared.algorithms.api/jguru-shared-algorithms-api/version = 1.0.0-SNAPSHOT
        val currentArtifact = "jguru-shared-algorithms-api"

        // Act
        val result = MavenDependencyInformation.parse(propertyMap)

        // Assert
        assertThat(result).isNotNull
        assertThat(result.size).isEqualTo(13)

        val ownArtifact = result.first { it.artifactID == currentArtifact }
        assertThat(ownArtifact.mavenVersion).isEqualTo("1.0.0-SNAPSHOT")
    }
}