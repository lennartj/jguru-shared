package se.jguru.shared.algorithms.api.introspection

import ch.qos.logback.classic.joran.JoranConfigurator
import org.junit.Assert
import org.junit.Test
import se.jguru.shared.algorithms.api.Validate
import java.util.TreeMap

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class IntrospectionTest {

    @Test
    fun validateTypesCorrectlyExtracted() {

        // Assemble
        val expected = mutableListOf("[Ljava.lang.Object;",
            "java.lang.Integer",
            "java.lang.String",
            "java.lang.StringBuilder",
            "java.util.ArrayList")
        val objectList = arrayOf("FooBar!", 42, StringBuilder())

        // Act
        val typesFrom = Introspection.getTypesFrom(objectList)

        // Assert
        // Assert.assertEquals(expected.size, typesFrom.size)
        typesFrom
            .sortedWith(Introspection.CLASSNAME_COMPARATOR)
            .forEachIndexed { index, currentClass ->
                Assert.assertEquals(expected[index], currentClass.name)
            }
    }

    @Test
    fun validateTypeNamesCorrectlyExtracted() {

        // Assemble
        val expected = mutableListOf("[Ljava.lang.Object;",
            "java.lang.Integer",
            "java.lang.String",
            "java.lang.StringBuilder")

        // Act
        val typesFound = Introspection.getTypeNamesFrom("FooBar!", 42, StringBuilder())

        // Assert
        Assert.assertEquals(expected.size, typesFound.size)
        typesFound
            .sorted()
            .forEachIndexed { index, currentClassName ->
                Assert.assertEquals(expected[index], currentClassName)
            }
    }

    @Test
    fun validateNullCodeSourceForSystemClass() {

        // Act & Assert
        // println("String .protectionDomain: ${String::class.java.protectionDomain}")
        Assert.assertNull(Introspection.getCodeSourceFor(String::class.java))
    }

    @Test
    fun validateNonNullCodeSourceForNonSystemClass() {

        // Act & Assert
        Assert.assertNotNull(Introspection.getCodeSourceFor(Validate::class.java))
    }

    @Test
    fun validateCodeSourcePrintoutForNonSystemClass() {

        // Assemble
        val nonSystemClass: Class<*> = Validate::class.java
        val systemClass: Class<*> = String::class.java

        // Act
        val nonSystemResult = Introspection.getCodeSourcePrintoutFor(nonSystemClass)
        val systemResult = Introspection.getCodeSourcePrintoutFor(systemClass)
        // println("Got: $result")
        // println("Got: $systemResult")

        // Assert
        Assert.assertTrue(nonSystemResult.isNotEmpty())
        Assert.assertTrue(systemResult.isNotEmpty())
    }

    @Test
    fun validateSystemPropertiesAndFiltering() {

        // Assemble
        val versionFilter : (String) -> Boolean = { aKey -> aKey.toLowerCase().contains("version")}

        // Act
        val allSystemProperties = Introspection.getSystemProperties()
        val versionSystemProperties = Introspection.getSystemProperties(versionFilter)

        // Assert
        Assert.assertTrue(allSystemProperties.isNotEmpty()  )
        Assert.assertTrue(versionSystemProperties.isNotEmpty())
        Assert.assertTrue(versionSystemProperties.size < allSystemProperties.size)

        versionSystemProperties.forEach { key, value -> println("[$key]: $value") }
    }

    @Test
    fun validateRetrievingMutableProperties() {

        // Assemble
        val expectedNames = arrayOf("mutableString", "mutableBoolean")

        // Act
        val mutableProperties = Introspection.getMutablePropertiesFor(SemiMutableType::class)

        // Assert
        Assert.assertNotNull(mutableProperties)
        Assert.assertEquals(2, mutableProperties.size)

        mutableProperties.map { it.name }.forEach { Assert.assertTrue(expectedNames.contains(it)) }
    }

    @Test
    fun validateUpdatingMutableProperties() {

        // Assemble
        val incoming = SemiMutableType(
            "incomingImmutable",
            "incomingMutable",
            true,
            false)

        val localState = SemiMutableType(
            "localImmutable",
            "localMutable",
            true,
            true)

        val expected = SemiMutableType(
            "localImmutable",
            "incomingMutable",
            true,
            false)

        // Act
        val updated = Introspection.updateProperties(SemiMutableType::class, incoming, localState)

        // Assert
        Assert.assertTrue(updated)
        Assert.assertEquals(expected, localState)
    }

    @Test
    fun validateParsingSemanticVersionFromManifestFile() {

        // Assemble
        val manifest = Introspection.getManifestFrom(Introspection::class.java)

        // Act
        val semVer = Introspection.findVersionFromManifestProperty(manifest)

        // Assert
        Assert.assertNotNull(semVer)
        Assert.assertNotNull(semVer.major)
        Assert.assertNotNull(semVer.minor)
        Assert.assertNotNull(semVer.micro)
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

    @Test
    fun validateParsingVersionMap() {

        // Assemble
        val bundleStyleMap = TreeMap<String, String>()
        val bundleMajorMinorStyleMap = TreeMap<String, String>()
        val specificationStyleMap = TreeMap<String, String>()
        val specificationMajorMinorStyleMap = TreeMap<String, String>()

        bundleStyleMap[Introspection.BUNDLE_VERSION] = "5.4.3.SNAPSHOT"
        bundleMajorMinorStyleMap[Introspection.BUNDLE_VERSION] = "15.14"
        specificationStyleMap[Introspection.SPECIFICATION_VERSION] = "6.5.4-SNAPSHOT"
        specificationMajorMinorStyleMap[Introspection.SPECIFICATION_VERSION] = "16.15"

        // Act
        val result1 = Introspection.findVersionFromMap(bundleStyleMap)
        val result2 = Introspection.findVersionFromMap(bundleMajorMinorStyleMap)
        val result3 = Introspection.findVersionFromMap(specificationStyleMap)
        val result4 = Introspection.findVersionFromMap(specificationMajorMinorStyleMap)

        // Assert
        Assert.assertEquals(result1, RuntimeVersion(5, 4, 3, "SNAPSHOT"))
        Assert.assertEquals(result2, RuntimeVersion(15, 14))
        Assert.assertEquals(result3, RuntimeVersion(6, 5, 4, "SNAPSHOT"))
        Assert.assertEquals(result4, RuntimeVersion(16, 15))
    }

    @Test
    fun validateReadingManifestFileFromMavenDependencyJar() {

        // Assemble

        // Act
        val jarBasedManifest = Introspection.getManifestFrom(JoranConfigurator::class.java)
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

}