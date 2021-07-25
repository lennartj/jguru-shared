package se.jguru.shared.algorithms.api.resources

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URL

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class PropertyResourcesTest {

    @Test
    fun validateSplittingPropertyLine() {

        // Assemble
        val theKey = "some_key"
        val theValue = "some_value"
        val line1 = " $theKey = $theValue "
        val line2 = "$theKey=$theValue"

        // Act
        val pair1 = PropertyResources.splitPropertyLine(line1) as Pair
        val pair2 = PropertyResources.splitPropertyLine(line2) as Pair

        // Assert
        assertThat(pair1.first).isEqualTo(theKey)
        assertThat(pair2.first).isEqualTo(theKey)

        assertThat(pair1.second).isEqualTo(theValue)
        assertThat(pair2.second).isEqualTo(theValue)
    }

    @Test
    fun validateNullPairReturnedOnNoOrEmptyKey() {

        // Assemble
        val line1 = "=some_value"
        val line2 = "no_separator_here"

        // Act
        val pair1 = PropertyResources.splitPropertyLine(line1)
        val pair2 = PropertyResources.splitPropertyLine(line2)

        // Assert
        assertThat(pair1).isNull()
        assertThat(pair2).isNull()
    }

    @Test
    fun validateSplittingProperties() {

        // Assemble
        val expectedKeys = arrayOf("groupId", "artifactId", "version")
        val groupIdValue = "se.jguru.shared.algorithms.api"
        val artifactIdValue = "jguru-shared-algorithms-api"
        val version = "1.0.0-SNAPSHOT"

        // Act
        val parsed = PropertyResources.parseResource(resourcePath = "testdata/introspection/unittest.properties")
        val keys = parsed.keys

        // Assert
        assertThat(parsed.size).isEqualTo(3)
        expectedKeys.forEach { assertThat(keys).contains(it) }

        assertThat(parsed["groupId"]).isEqualTo(groupIdValue)
        assertThat(parsed["artifactId"]).isEqualTo(artifactIdValue)
        assertThat(parsed["version"]).isEqualTo(version)
    }

    @Test
    fun validateReadingResourceFilesFully() {

        // Assemble
        val expected = "This is a resource with non-ascii characters.\n" +
            "åäöÅÄÖ."

        // Act
        val result = PropertyResources.readFully(resourcePath = "testdata/resources/simpleResource.txt")
        // println("Got:\n" + result)

        // Assert
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun validateUrlFiltering() {

        // Assemble
        val sharedAlgorithmsUrlFilter: (URL) -> Boolean = {
            it.toString().contains("shared-algorithms-api", true)
        }

        // Act
        val unfiltered = PropertyResources.getResourceURLs(MavenDependencyInformation.DEPENDENCY_RESOURCE)
        val filtered = PropertyResources.getResourceURLs(
            MavenDependencyInformation.DEPENDENCY_RESOURCE,
            sharedAlgorithmsUrlFilter)

        //
        // unfiltered.forEachIndexed { index, url -> println("Unfiltered [$index]: $url") }
        //
        // filtered.forEachIndexed { index, url -> println("Filtered [$index]: $url") }
        //

        // Assert
        assertThat(unfiltered.size).isGreaterThanOrEqualTo(filtered.size)
        assertThat(filtered.size).isEqualTo(1)
        assertThat(filtered.first().toString()).contains("shared-algorithms-api")
    }
}