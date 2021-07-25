package se.jguru.shared.json.spi.jackson.custom

import org.junit.Assert
import org.junit.jupiter.api.Test

open class SimplifiedFormatModuleTest {

    @Test
    fun validateGettingVersionWhenNoManifestCouldBeRead() {

        // Assemble

        // Act
        val result = SimplifiedFormatModule.findLocalJarVersion()
        // println("Got: ${result.toFullString()}")

        // Assert
        Assert.assertNotNull(result)
    }
}