package se.jguru.shared.persistence.spi.jpa.classloading

import org.apache.logging.log4j.LogManager
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Collections


private const val thePackage = "testdata/classloading/"
private const val REDIRECTED = "${thePackage}redirectedPersistence.xml"
private const val NON_REDIRECTED = "${thePackage}standard.properties"

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class PersistenceRedirectionClassLoaderTest {

    // Shared state
    private lateinit var originalClassLoader: ClassLoader

    @BeforeEach
    fun setupSharedState() {
        originalClassLoader = Thread.currentThread().contextClassLoader
    }

    @AfterEach
    fun teardownSharedState() {
        Thread.currentThread().contextClassLoader = originalClassLoader
    }

    @Test
    fun validateExceptionOnEmptyRedirectionTarget() {

        // Act & Assert
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
            PersistenceRedirectionClassLoader(originalClassLoader, "")
        }
    }

    @Test
    fun validateStandardThreadContextClassloaderTypeIsNotPersistenceRedirectionClassLoader() {

        // Assemble
        val activeClassloader = Thread.currentThread().contextClassLoader

        // Assert
        assertThat(activeClassloader).isNotInstanceOf(PersistenceRedirectionClassLoader::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun validateRedirectionOnlyHappensForPersistenceXml() {

        // Assemble
        val unitUnderTest = PersistenceRedirectionClassLoader(
            originalClassLoader, REDIRECTED)

        Thread.currentThread().contextClassLoader = unitUnderTest

        // Act
        val redirectedResources = unitUnderTest.getResources(
            PersistenceRedirectionClassLoader.PERSISTENCE_XML)
        val nonRedirectedResources = unitUnderTest.getResources(NON_REDIRECTED)

        val redirectedURLs = Collections.list(redirectedResources)
        val nonRedirectedURLs = Collections.list(nonRedirectedResources)

        // Assert
        assertThat(redirectedURLs.size).isEqualTo(1)
        assertThat(nonRedirectedURLs.size).isEqualTo(1)

        assertThat(redirectedURLs[0].path).endsWith(REDIRECTED)
        assertThat(nonRedirectedURLs[0].path).endsWith(NON_REDIRECTED)

        assertThat(unitUnderTest.caseInsensitive).isTrue
        assertThat(unitUnderTest.toString()).isNotBlank
        assertThat(unitUnderTest.redirectTo).isEqualTo(REDIRECTED)
    }

    @Test
    fun validateRedirectionForSingleResource() {

        // Assemble
        val unitUnderTest = PersistenceRedirectionClassLoader(originalClassLoader, REDIRECTED)
        Thread.currentThread().contextClassLoader = unitUnderTest

        // Act
        val redirectedResource = unitUnderTest.getResource(PersistenceRedirectionClassLoader.PERSISTENCE_XML)
        val nonRedirectedResource = unitUnderTest.getResource(NON_REDIRECTED)

        // Assert
        assertThat(redirectedResource).isNotNull
        assertThat(nonRedirectedResource).isNotNull

        assertThat(redirectedResource?.path).endsWith(REDIRECTED)
        assertThat(nonRedirectedResource?.path).endsWith(NON_REDIRECTED)
    }

    @Test
    fun validateRedirectionOnlyHappensForPersistenceXmlWithQuietLogging() {

        // Assemble
        val loggerContext = LogManager.getContext(false) as org.apache.logging.log4j.core.LoggerContext
        val quietLogConfig = javaClass.classLoader.getResource("log4j2-test-quiet.xml")

        // this will force a reconfiguration
        loggerContext.configLocation = quietLogConfig?.toURI()

        val unitUnderTest = PersistenceRedirectionClassLoader(
            originalClassLoader, REDIRECTED)

        Thread.currentThread().contextClassLoader = unitUnderTest

        // Act
        val redirectedResources = unitUnderTest.getResources(
            PersistenceRedirectionClassLoader.PERSISTENCE_XML)
        val nonRedirectedResources = unitUnderTest.getResources(NON_REDIRECTED)

        val redirectedURLs = Collections.list(redirectedResources)
        val nonRedirectedURLs = Collections.list(nonRedirectedResources)

        // Assert
        assertThat(redirectedURLs.size).isEqualTo(1)
        assertThat(nonRedirectedURLs.size).isEqualTo(1)

        assertThat(redirectedURLs[0].path).endsWith(REDIRECTED)
        assertThat(nonRedirectedURLs[0].path).endsWith(NON_REDIRECTED)
    }
}