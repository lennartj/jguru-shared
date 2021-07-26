package se.jguru.shared.persistence.spi.hibernate

import org.assertj.core.api.Assertions.assertThat
import org.hibernate.boot.model.naming.Identifier
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
        assertThat(result).isNotNull
        assertThat(result!!.render()).isEqualTo("thisiscamelcase")
    }

    @Test
    fun validateHandlingNullTextToLowerCase() {

        // Assemble

        // Act
        val resultWithNull = SimplePhysicalNamingStrategy.toLowerCase(null, struct)
        val resultWithNullText = SimplePhysicalNamingStrategy.toLowerCase(Identifier.toIdentifier(null), struct)

        // Assert
        assertThat(resultWithNull).isNull()
        assertThat(resultWithNullText).isNull()
    }

    @Test
    fun validateConvertingIdentifierToSnakeCase() {

        // Assemble
        val camelCase = "thisIsCamelCase"
        val supplied = Identifier.toIdentifier(camelCase)

        // Act
        val result = SimplePhysicalNamingStrategy.toSnakeCase(supplied, struct)

        // Assert
        assertThat(result).isNotNull
        assertThat(result!!.render()).isEqualTo("this_is_camel_case")
    }

    @Test
    fun validateHandlingNullTextToSnakeCase() {

        // Assemble

        // Act
        val resultWithNull = SimplePhysicalNamingStrategy.toSnakeCase(null, struct)
        val resultWithNullText = SimplePhysicalNamingStrategy.toSnakeCase(Identifier.toIdentifier(null), struct)

        // Assert
        assertThat(resultWithNull).isNull()
        assertThat(resultWithNullText).isNull()
    }
}