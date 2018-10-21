package se.jguru.shared.algorithms.api.introspection

import org.junit.Assert
import org.junit.Test
import se.jguru.shared.algorithms.api.Validate

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
}