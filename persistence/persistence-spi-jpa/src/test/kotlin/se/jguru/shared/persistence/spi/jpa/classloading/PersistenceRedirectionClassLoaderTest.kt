package se.jguru.shared.persistence.spi.jpa.classloading

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.slf4j.LoggerFactory
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
        val context = LoggerFactory.getILoggerFactory() as LoggerContext
        val configurator = JoranConfigurator()
        configurator.context = context

        // Call context.reset() to clear any previous configuration, e.g. default configuration.
        // For multi-step configuration, omit calling context.reset().
        context.reset()
        val quietLogbackConfig = javaClass.classLoader.getResource("logback-test-quiet.xml")
        configurator.doConfigure(quietLogbackConfig!!)

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