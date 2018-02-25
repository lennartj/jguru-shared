package se.jguru.shared.algorithms.api

import org.junit.Assert
import org.junit.Test
import se.jguru.shared.algorithms.api.introspection.Introspection
import java.lang.StringBuilder

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class IntrospectionTest {

    @Test
    fun validateTypesCorrectlyExtracted() {

        // Assemble
        val expected = mutableListOf("java.lang.Integer",
            "java.lang.String",
            "java.lang.StringBuilder",
            "java.util.ArrayList")
        val objectList = mutableListOf("FooBar!", 42, StringBuilder())

        // Act
        val typesFrom = Introspection.getTypesFrom(objectList)

        // Assert
        Assert.assertEquals(expected.size, typesFrom.size)
        typesFrom
            .sortedWith(Introspection.CLASSNAME_COMPARATOR)
            .forEachIndexed { index, currentClass ->
                Assert.assertEquals(expected[index], currentClass.name)
            }
    }

    @Test
    fun validateTypeNamesCorrectlyExtracted() {

        // Assemble
        // , java.lang.Integer, java.lang.String, java.lang.StringBuilder
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
}