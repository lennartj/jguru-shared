package se.jguru.shared.algorithms.api.introspection

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

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
        assertThat(result).isNotNull
        assertThat(result.major).isEqualTo(8)
        assertThat(result.minor).isEqualTo(0)
        assertThat(result.micro).isEqualTo(181)
    }

    @Test
    fun validateParsingJava9VersionStrings() {

        // Assemble
        val versionString = "9.0.4"

        // Act
        val result = RuntimeVersion.parseJavaVersion(versionString)

        // Assert
        assertThat(result).isNotNull
        assertThat(result.major).isEqualTo(9)
        assertThat(result.minor).isEqualTo(0)
        assertThat(result.micro).isEqualTo(4)
    }

    @Test
    fun validateParsingJava10VersionStrings() {

        // Assemble
        val versionString = "10.0.2"

        // Act
        val result = RuntimeVersion.parseJavaVersion(versionString)

        // Assert
        assertThat(result).isNotNull
        assertThat(result.major).isEqualTo(10)
        assertThat(result.minor).isEqualTo(0)
        assertThat(result.micro).isEqualTo(2)
    }

    @Test
    fun validateParsingShortJava10VersionStrings() {

        // Assemble
        val versionString = "10.0"

        // Act
        val result = RuntimeVersion.parseJavaVersion(versionString)

        // Assert
        assertThat(result).isNotNull
        assertThat(result.major).isEqualTo(10)
        assertThat(result.minor).isEqualTo(0)
        assertThat(result.micro).isNull()
    }

    @Test
    fun validateParsingJava11VersionString() {

        // Assemble
        val versionString = "11"

        // Act
        val result = RuntimeVersion.parseJavaVersion(versionString)

        // Assert
        assertThat(result).isNotNull
        assertThat(result.major).isEqualTo(11)
        assertThat(result.minor).isNull()
        assertThat(result.micro).isNull()
    }
}