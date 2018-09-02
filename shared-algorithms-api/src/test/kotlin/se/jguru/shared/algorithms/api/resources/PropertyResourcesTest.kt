package se.jguru.shared.algorithms.api.resources

import org.junit.Assert
import org.junit.Test
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
        Assert.assertEquals(theKey, pair1.first)
        Assert.assertEquals(theKey, pair2.first)

        Assert.assertEquals(theValue, pair1.second)
        Assert.assertEquals(theValue, pair2.second)
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
        Assert.assertNull(pair1)
        Assert.assertNull(pair2)
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
        Assert.assertEquals(3, parsed.size)
        expectedKeys.forEach { Assert.assertTrue(keys.contains(it)) }

        Assert.assertEquals(groupIdValue, parsed["groupId"])
        Assert.assertEquals(artifactIdValue, parsed["artifactId"])
        Assert.assertEquals(version, parsed["version"])
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
        Assert.assertEquals(expected, result)
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

        unfiltered.forEachIndexed { index, url -> println("Unfiltered [$index]: $url") }

        filtered.forEachIndexed { index, url -> println("Filtered [$index]: $url") }

        // Assert
        Assert.assertTrue(filtered.size <= unfiltered.size)
        Assert.assertEquals(1, filtered.size)
        Assert.assertTrue(filtered.first().toString().contains("shared-algorithms-api", true))
    }
}