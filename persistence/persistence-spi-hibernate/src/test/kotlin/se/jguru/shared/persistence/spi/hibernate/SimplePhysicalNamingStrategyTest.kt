package se.jguru.shared.persistence.spi.hibernate

import org.hibernate.boot.model.naming.Identifier
import org.junit.Assert
import org.junit.jupiter.api.Test

open class SimplePhysicalNamingStrategyTest {

    // Shared state
    val struct = DbStructure.TABLE

    @Test
    fun validateConvertingIdentifierToLowerCase() {

        // Assemble
        val camelCase = "thisIsCamelCase"
        val supplied = Identifier.toIdentifier(camelCase)

        // Act
        val result = SimplePhysicalNamingStrategy.toLowerCase(supplied, struct)

        // Assert
        Assert.assertNotNull(result)
        Assert.assertEquals("thisiscamelcase", result!!.render())
    }

    @Test
    fun validateHandlingNullTextToLowerCase() {

        // Assemble

        // Act
        val resultWithNull = SimplePhysicalNamingStrategy.toLowerCase(null, struct)
        val resultWithNullText = SimplePhysicalNamingStrategy.toLowerCase(Identifier.toIdentifier(null), struct)

        // Assert
        Assert.assertNull(resultWithNull)
        Assert.assertNull(resultWithNullText)
    }

    @Test
    fun validateConvertingIdentifierToSnakeCase() {

        // Assemble
        val camelCase = "thisIsCamelCase"
        val supplied = Identifier.toIdentifier(camelCase)

        // Act
        val result = SimplePhysicalNamingStrategy.toSnakeCase(supplied, struct)

        // Assert
        Assert.assertNotNull(result)
        Assert.assertEquals("this_is_camel_case", result!!.render())
    }

    @Test
    fun validateHandlingNullTextToSnakeCase() {

        // Assemble

        // Act
        val resultWithNull = SimplePhysicalNamingStrategy.toSnakeCase(null, struct)
        val resultWithNullText = SimplePhysicalNamingStrategy.toSnakeCase(Identifier.toIdentifier(null), struct)

        // Assert
        Assert.assertNull(resultWithNull)
        Assert.assertNull(resultWithNullText)
    }
}