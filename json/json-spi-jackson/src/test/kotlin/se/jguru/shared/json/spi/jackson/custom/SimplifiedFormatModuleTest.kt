package se.jguru.shared.json.spi.jackson.custom

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

open class SimplifiedFormatModuleTest {

    private val log : Logger = LoggerFactory.getLogger(SimplifiedFormatModuleTest::class.java)

    @Test
    fun validateGettingVersionWhenNoManifestCouldBeRead() {

        // Assemble

        // Act
        val result = SimplifiedFormatModule.findLocalJarVersion()
        // log.debug("Got:\n${result.toFullString()}")

        // Assert
        assertThat(result).isNotNull
    }
}