package se.jguru.shared.algorithms.api.introspection

import org.apache.logging.log4j.core.Core
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
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
                assertThat(currentClass.name).isEqualTo(expected[index])
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
        assertThat(typesFound.size).isEqualTo(expected.size)

        typesFound
            .sorted()
            .forEachIndexed { index, currentClassName ->
                assertThat(currentClassName).isEqualTo(expected[index])
            }
    }

    @Test
    fun validateNullCodeSourceForSystemClass() {

        // Act & Assert
        // println("String .protectionDomain: ${String::class.java.protectionDomain}")
        assertThat(Introspection.getCodeSourceFor(String::class.java)).isNull()
    }

    @Test
    fun validateNonNullCodeSourceForNonSystemClass() {

        // Act & Assert
        assertThat(Introspection.getCodeSourceFor(Validate::class.java)).isNotNull
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
        assertThat(nonSystemResult.isNotEmpty()).isTrue
        assertThat(systemResult.isNotEmpty()).isTrue
    }

    @Test
    fun validateSystemPropertiesAndFiltering() {

        // Assemble
        val versionFilter : (String) -> Boolean = { aKey -> aKey.toLowerCase().contains("version")}

        // Act
        val allSystemProperties = Introspection.getSystemProperties()
        val versionSystemProperties = Introspection.getSystemProperties(versionFilter)

        // Assert
        assertThat(allSystemProperties).isNotEmpty
        assertThat(versionSystemProperties).isNotEmpty
        assertThat(versionSystemProperties.size).isLessThan(allSystemProperties.size)

        // versionSystemProperties.forEach { (key, value) -> println("[$key]: $value") }
    }

    @Test
    fun validateRetrievingMutableProperties() {

        // Assemble
        val expectedNames = arrayOf("mutableString", "mutableBoolean")

        // Act
        val mutableProperties = Introspection.getMutablePropertiesFor(SemiMutableType::class)

        // Assert
        assertThat(mutableProperties).isNotNull
        assertThat(mutableProperties.size).isEqualTo(2)

        mutableProperties.map { it.name }.forEach { assertThat(expectedNames).contains(it) }
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
        assertThat(updated).isTrue
        assertThat(localState).isEqualTo(expected)
    }

    @Test
    fun validateParsingSemanticVersionFromManifestFile() {

        // Assemble
        val manifest = Introspection.getManifestFrom(Introspection::class.java)

        // Act
        val semVer = Introspection.findVersionFromManifestProperty(manifest)

        // Assert
        assertThat(semVer).isNotNull
        assertThat(semVer.major).isNotNull
        assertThat(semVer.minor).isNotNull
        assertThat(semVer.micro).isNotNull
    }

    @Test
    fun validateReadingManifestFile() {

        // Assemble

        // Act
        val fileBasedManifest = Introspection.getManifestFrom(Introspection::class.java)
        val manifestMap = Introspection.extractMapOf(fileBasedManifest)

        // Assert
        assertThat(fileBasedManifest).isNotNull
        assertThat(manifestMap).isNotNull

        assertThat(manifestMap["Bundle-Version"]).isNotNull()
        assertThat(manifestMap.size).isEqualTo(fileBasedManifest.mainAttributes.size)

        fileBasedManifest.mainAttributes
            .forEach { key, value -> assertThat(manifestMap["" + key]).isEqualTo("" + value) }

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
        assertThat(result1).isEqualTo(RuntimeVersion(5, 4, 3, "SNAPSHOT"))
        assertThat(result2).isEqualTo(RuntimeVersion(15, 14))
        assertThat(result3).isEqualTo(RuntimeVersion(6, 5, 4, "SNAPSHOT"))
        assertThat(result4).isEqualTo(RuntimeVersion(16, 15))
    }

    @Test
    fun validateReadingManifestFileFromMavenDependencyJar() {

        // Assemble

        // Act
        val jarBasedManifest = Introspection.getManifestFrom(Core::class.java)
        val manifestMap = Introspection.extractMapOf(jarBasedManifest)

        // Assert
        assertThat(jarBasedManifest).isNotNull
        assertThat(manifestMap).isNotNull

        assertThat(manifestMap["Bundle-Version"]).isNotNull()
        assertThat(jarBasedManifest.mainAttributes.size).isEqualTo(manifestMap.size)

        jarBasedManifest.mainAttributes
            .forEach { key, value -> assertThat(manifestMap["" + key]).isEqualTo("" + value) }

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