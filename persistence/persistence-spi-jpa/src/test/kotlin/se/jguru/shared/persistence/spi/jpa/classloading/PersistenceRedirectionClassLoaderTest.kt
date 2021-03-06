package se.jguru.shared.persistence.spi.jpa.classloading

import org.apache.logging.log4j.LogManager
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
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

    @Before
    fun setupSharedState() {
        originalClassLoader = Thread.currentThread().contextClassLoader
    }

    @After
    fun teardownSharedState() {
        Thread.currentThread().contextClassLoader = originalClassLoader
    }

    @Test(expected = IllegalArgumentException::class)
    fun validateExceptionOnEmptyRedirectionTarget() {

        // Act & Assert
        PersistenceRedirectionClassLoader(originalClassLoader, "")
    }

    @Test
    fun validateStandardThreadContextClassloaderTypeIsNotPersistenceRedirectionClassLoader() {

        // Assemble
        val activeClassloader = Thread.currentThread().contextClassLoader

        // Assert
        Assert.assertFalse(activeClassloader is PersistenceRedirectionClassLoader)
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
        Assert.assertEquals(1, redirectedURLs.size.toLong())
        Assert.assertEquals(1, nonRedirectedURLs.size.toLong())

        Assert.assertTrue(redirectedURLs[0].path.endsWith(REDIRECTED))
        Assert.assertTrue(nonRedirectedURLs[0].path.endsWith(NON_REDIRECTED))

        Assert.assertTrue(unitUnderTest.caseInsensitive)
        Assert.assertEquals(REDIRECTED, unitUnderTest.redirectTo)
        Assert.assertTrue(unitUnderTest.toString().isNotBlank())
    }

    @Test
    @Throws(Exception::class)
    fun validateRedirectionForSingleResource() {

        // Assemble
        val unitUnderTest = PersistenceRedirectionClassLoader(originalClassLoader, REDIRECTED)
        Thread.currentThread().contextClassLoader = unitUnderTest

        // Act
        val redirectedResource = unitUnderTest.getResource(PersistenceRedirectionClassLoader.PERSISTENCE_XML)
        val nonRedirectedResource = unitUnderTest.getResource(NON_REDIRECTED)

        // Assert
        Assert.assertNotNull(redirectedResource)
        Assert.assertNotNull(nonRedirectedResource)

        Assert.assertTrue(redirectedResource.path.endsWith(REDIRECTED))
        Assert.assertTrue(nonRedirectedResource.path.endsWith(NON_REDIRECTED))
    }

    @Test
    @Throws(Exception::class)
    fun validateRedirectionOnlyHappensForPersistenceXmlWithQuietLogging() {

        // Assemble
        val loggerContext = LogManager.getContext(false) as org.apache.logging.log4j.core.LoggerContext
        val quietLogConfig = javaClass.classLoader.getResource("log4j2-test-quiet.xml")

        // this will force a reconfiguration
        loggerContext.configLocation = quietLogConfig.toURI()

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
        Assert.assertEquals(1, redirectedURLs.size.toLong())
        Assert.assertEquals(1, nonRedirectedURLs.size.toLong())

        Assert.assertTrue(redirectedURLs[0].path.endsWith(REDIRECTED))
        Assert.assertTrue(nonRedirectedURLs[0].path.endsWith(NON_REDIRECTED))
    }
}