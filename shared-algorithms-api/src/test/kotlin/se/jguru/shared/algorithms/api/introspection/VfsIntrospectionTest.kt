package se.jguru.shared.algorithms.api.introspection

import org.apache.logging.log4j.core.Core
import org.jboss.vfs.VFS
import org.jboss.vfs.VFSUtils
import org.jboss.vfs.VirtualFile
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.net.URL
import java.net.URLClassLoader

private const val JAR_RESOURCE_PATH = "testdata/introspection/jarWithPropertyFiles.jar"

open class VfsIntrospectionTest {

    // Shared state
    lateinit var originalThreadContextClassLoader: ClassLoader
    lateinit var jarWithPropertyFilesURL : URL

    @Before
    fun setupSharedState() {

        // Stash the original TC classloader
        this.originalThreadContextClassLoader = Thread.currentThread().contextClassLoader

        // Sneak in the jarWithPropertyFiles.jar into the test classpath
        val url = originalThreadContextClassLoader.getResource(JAR_RESOURCE_PATH).openConnection().url
            ?: throw IllegalStateException("Required resource [$JAR_RESOURCE_PATH] not found.")

        val testContextClassLoader = URLClassLoader(arrayOf(url), originalThreadContextClassLoader)
        Thread.currentThread().contextClassLoader = testContextClassLoader

        val resourcePaths = testContextClassLoader.getResources("META-INF/MANIFEST.MF")
            .toList()
            .mapIndexed { index, currentURL -> " [$index]: $currentURL" }
            .reduce { acc, s -> "$acc\n$s" }
        Assert.assertTrue(resourcePaths.isNotEmpty())

        // Ensure that we have a non-null URL to the added resource when
        // searched within the test contextClassLoader.
        //
        jarWithPropertyFilesURL = testContextClassLoader.getResource(JAR_RESOURCE_PATH)
        Assert.assertNotNull(jarWithPropertyFilesURL)
    }

    @After
    fun teardownSharedState() {

        // Restore the original TC classloader
        Thread.currentThread().contextClassLoader = originalThreadContextClassLoader
    }

    @Test
    fun validateCodeSourceForResourceInFileSystem() {

        // Assemble
        val vFileToJarFile = VFS.getChild(jarWithPropertyFilesURL.toURI())

        // Act
        val vFileLocation = Introspection.getCodeSourceFor(vFileToJarFile::class.java)?.location
            ?: throw IllegalStateException("Could not find location URL for class ${vFileToJarFile::class.java.name}")

        // println("Got location: ${vFileLocation.toURI()}")

        // Assert
        Assert.assertNotNull(vFileLocation)
        Assert.assertEquals(VFSUtils.VFS_PROTOCOL, vFileToJarFile.asFileURL().protocol)
    }

    @Test
    fun validateReadingManifestFileFromMavenDependencyJar() {

        // Assemble

        // Act
        val jarBasedManifest = Introspection.getManifestFrom(Core::class.java)
        val manifestMap = Introspection.extractMapOf(jarBasedManifest)

        // Assert
        Assert.assertNotNull(jarBasedManifest)
        Assert.assertNotNull(manifestMap)

        Assert.assertNotNull(manifestMap["Bundle-Version"])
        Assert.assertEquals(jarBasedManifest.mainAttributes.size, manifestMap.size)

        jarBasedManifest.mainAttributes
            .forEach { key, value -> Assert.assertEquals("" + value, manifestMap["" + key]) }

        /*
        println("Got ${jarBasedManifest.mainAttributes.size} main " +
                "attributes: ${jarBasedManifest.mainAttributes.entries}")
        println("Map [${manifestMap.size} elements]:\n" +
                manifestMap.entries
                        .sortedBy { it.key }
                        .map { (k, v) -> "[$k]: $v" }
                        .reduce { total, current -> total + "\n" + current })
                        */
    }

    @Test
    fun validateReadingManifestFile() {

        // Assemble

        // Act
        val fileBasedManifest = Introspection.getManifestFrom(Introspection::class.java)
        val manifestMap = Introspection.extractMapOf(fileBasedManifest)

        // Assert
        Assert.assertNotNull(fileBasedManifest)
        Assert.assertNotNull(manifestMap)

        Assert.assertNotNull(manifestMap["Bundle-Version"])
        Assert.assertEquals(fileBasedManifest.mainAttributes.size, manifestMap.size)

        fileBasedManifest.mainAttributes
            .forEach { key, value -> Assert.assertEquals("" + value, manifestMap["" + key]) }

        /*
        println("Got ${fileBasedManifest.mainAttributes.size} main " +
                "attributes: ${fileBasedManifest.mainAttributes.entries}")
        println("Map [${manifestMap.size} elements]:\n" +
                manifestMap.entries
                        .sortedBy { it.key }
                        .map { (k, v) -> "[$k]: $v" }
                        .reduce { total, current -> total + "\n" + current })
                        */
    }

    //
    // Private helpers
    //

    companion object {

        @JvmStatic
        @JvmOverloads
        fun getVfsFileFor(resourcePath: String,
                          projectDirectoryName: String = "shared-algorithms-api",
                          onlyWithinJAR : Boolean = true) : VirtualFile {

            val classLoader = Thread.currentThread().contextClassLoader

            val foundResource = classLoader.getResources(resourcePath)
                .asSequence()
                .filter {
                    val thePath = it.path
                    val correctLocation = thePath.contains(projectDirectoryName) && thePath.contains(resourcePath)

                    when {
                        onlyWithinJAR -> correctLocation && thePath.contains(".jar!/")
                        else -> correctLocation
                    }
                }
                .firstOrNull() ?: throw IllegalStateException("Required resource [$resourcePath] not found.")

            return VFS.getChild(foundResource.toURI())
        }
    }
}