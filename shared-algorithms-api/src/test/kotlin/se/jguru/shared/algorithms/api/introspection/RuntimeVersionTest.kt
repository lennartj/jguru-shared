package se.jguru.shared.algorithms.api.introspection

import org.junit.Assert
import org.junit.Test

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class RuntimeVersionTest {


    @Test
    fun validateParsingJava18VersionStrings() {

        // Assemble
        val versionString = "1.8.0_181"

        // Act
        val result = RuntimeVersion.parseJavaVersion(versionString)

        // Assert
        Assert.assertNotNull(result)
        Assert.assertEquals(8, result.major)
        Assert.assertEquals(0, result.minor)
        Assert.assertEquals(181, result.micro)
    }

    @Test
    fun validateParsingJava9VersionStrings() {

        // Assemble
        val versionString = "9.0.4"

        // Act
        val result = RuntimeVersion.parseJavaVersion(versionString)

        // Assert
        Assert.assertNotNull(result)
        Assert.assertEquals(9, result.major)
        Assert.assertEquals(0, result.minor)
        Assert.assertEquals(4, result.micro)
    }

    @Test
    fun validateParsingJava10VersionStrings() {

        // Assemble
        val versionString = "10.0.2"

        // Act
        val result = RuntimeVersion.parseJavaVersion(versionString)

        // Assert
        Assert.assertNotNull(result)
        Assert.assertEquals(10, result.major)
        Assert.assertEquals(0, result.minor)
        Assert.assertEquals(2, result.micro)
    }

    @Test
    fun validateParsingShortJava10VersionStrings() {

        // Assemble
        val versionString = "10.0"

        // Act
        val result = RuntimeVersion.parseJavaVersion(versionString)

        // Assert
        Assert.assertNotNull(result)
        Assert.assertEquals(10, result.major)
        Assert.assertEquals(0, result.minor)
        Assert.assertNull(result.micro)
    }

    @Test
    fun validateParsingJava11VersionString() {

        // Assemble
        val versionString = "11"

        // Act
        val result = RuntimeVersion.parseJavaVersion(versionString)

        // Assert
        Assert.assertNotNull(result)
        Assert.assertEquals(11, result.major)
        Assert.assertNull(result.minor)
        Assert.assertNull(result.micro)
    }
}