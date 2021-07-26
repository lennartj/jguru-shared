package se.jguru.shared.service.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test

class SemanticVersionTest {

    @Test
    fun validateComparisonWithoutQualifier() {

        // Assemble
        val v120 = SemanticVersion(1, 2, 0)
        val v12 = SemanticVersion(1, 2)
        val v1 = SemanticVersion(1)

        // Act & Assert
        assertThat(v120).isEqualTo(v12)
        assertThat(v120.compareTo(v12)).isEqualTo(0)

        assertThat(v1.compareTo(v120)).isEqualTo(-2)
        assertThat(v12.compareTo(v1)).isEqualTo(2)

        assertThat(v120.isUndefined).isFalse
    }

    @Test
    fun validateComparisonWithQualifier() {

        // Assemble
        val v120 = SemanticVersion(1, 2, 0, "FINAL")
        val v12 = SemanticVersion(1, 2, qualifier = "FINAL")
        val v1Bah = SemanticVersion(1, qualifier = "BAH")
        val v1 = SemanticVersion(1)

        // Act & Assert
        assertThat(v12).isEqualTo(v120)
        assertThat(v120.compareTo(v12)).isEqualTo(0)

        assertThat(v1.compareTo(v120)).isEqualTo(-2)
        assertThat(v12.compareTo(v1)).isEqualTo(2)
        assertThat(v1Bah.compareTo(v1)).isEqualTo(3)
        assertThat(v1.compareTo(v1Bah)).isEqualTo(-3)
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
        assertThat(semVer120).isEqualTo(semVer12)
        assertThat(semVer123.qualifier).isNull()

        assertThat(semVer123Final.qualifier).isEqualTo("Final")

        assertThat(semVer1.major).isEqualTo(1)
        assertThat(semVer1.minor).isEqualTo(0)
        assertThat(semVer1.micro).isEqualTo(0)
        assertThat(semVer1.qualifier).isNull()
    }

    @Test
    fun validateExceptionOnIncorrectParsing() {

        // Act & Assert
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
            SemanticVersion.parseFrom("not.a.version")
        }
    }

    @Test
    fun validateExceptionOnNegativeMajorVersion() {

        // Act & Assert
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
            SemanticVersion(-1)
        }
    }

    @Test
    fun validateExceptionOnNegativeMinorVersion() {

        // Act & Assert
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
            SemanticVersion(23, -1)
        }
    }

    @Test
    fun validateExceptionOnNegativeMicroVersion() {

        // Act & Assert
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
            SemanticVersion(23, 24, -25)
        }
    }
}