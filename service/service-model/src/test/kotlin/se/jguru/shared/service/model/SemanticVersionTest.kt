package se.jguru.shared.service.model

import org.junit.Assert
import org.junit.Test

class SemanticVersionTest {

    @Test
    fun validateComparisonWithoutQualifier() {

        // Assemble
        val v120 = SemanticVersion(1, 2, 0)
        val v12 = SemanticVersion(1, 2)
        val v1 = SemanticVersion(1)

        // Act & Assert
        Assert.assertEquals(v120, v12)
        Assert.assertEquals(0, v120.compareTo(v12))

        Assert.assertEquals(-2, v1.compareTo(v120))
        Assert.assertEquals(2, v12.compareTo(v1))

        Assert.assertFalse(v120.isUndefined)
    }

    @Test
    fun validateComparisonWithQualifier() {

        // Assemble
        val v120 = SemanticVersion(1, 2, 0, "FINAL")
        val v12 = SemanticVersion(1, 2, qualifier = "FINAL")
        val v1Bah = SemanticVersion(1, qualifier = "BAH")
        val v1 = SemanticVersion(1)

        // Act & Assert
        Assert.assertEquals(v120, v12)
        Assert.assertEquals(0, v120.compareTo(v12))

        Assert.assertEquals(-2, v1.compareTo(v120))
        Assert.assertEquals(2, v12.compareTo(v1))
        Assert.assertEquals(3, v1Bah.compareTo(v1))
        Assert.assertEquals(-3, v1.compareTo(v1Bah))
    }

    @Test
    fun validateParsingSemanticVersion() {

        // Assemble
        val v123 = "1.2.3"
        val v120 = "1.2.0"
        val v12 = "1.2"
        val v1 = "1"
        val v123Final = "1.2.3.Final"

        // Act
        val semVer123 = SemanticVersion.parseFrom(v123)
        val semVer120 = SemanticVersion.parseFrom(v120)
        val semVer12 = SemanticVersion.parseFrom(v12)
        val semVer1 = SemanticVersion.parseFrom(v1)
        val semVer123Final = SemanticVersion.parseFrom(v123Final)

        // Assert
        Assert.assertEquals(semVer120, semVer12)
        Assert.assertNull(semVer123.qualifier)

        Assert.assertEquals("Final", semVer123Final.qualifier)

        Assert.assertEquals(1, semVer1.major)
        Assert.assertEquals(0, semVer1.minor)
        Assert.assertEquals(0, semVer1.micro)
        Assert.assertNull(semVer1.qualifier)
    }

    @Test(expected = IllegalArgumentException::class)
    fun validateExceptionOnIncorrectParsing() {

        // Act & Assert
        SemanticVersion.parseFrom("not.a.version")
    }

    @Test(expected = IllegalArgumentException::class)
    fun validateExceptionOnNegativeMajorVersion() {

        // Act & Assert
        SemanticVersion(-1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun validateExceptionOnNegativeMinorVersion() {

        // Act & Assert
        SemanticVersion(23, -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun validateExceptionOnNegativeMicroVersion() {

        // Act & Assert
        SemanticVersion(23, 24, -25)
    }
}