package se.jguru.shared.json.spi.jackson.custom

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

open class SimplifiedFormatModuleTest {

    @Test
    fun validateGettingVersionWhenNoManifestCouldBeRead() {

        // Assemble

        // Act
        val result = SimplifiedFormatModule.findLocalJarVersion()
        // println("Got: ${result.toFullString()}")

        // Assert
        assertThat(result).isNotNull
    }
}