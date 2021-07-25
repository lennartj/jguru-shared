package se.jguru.shared.algorithms.api.messaging

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.osjava.sj.loader.JndiLoader
import java.util.Properties
import javax.naming.Context
import javax.naming.InitialContext

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class ContextsTest {

    // Shared state
    lateinit var ctx: Context
    lateinit var props: Properties

    @BeforeEach
    fun setupSharedState() {

        // Create the properties
        props = Properties()

        props["java:foo"] = "fooValue"
        props["java:bar"] = "barValue"
        props["java:theStringBuilder"] = StringBuilder("tjo")
        props["java:theStringBuilder.type"] = StringBuilder::class.java.name

        // Create the empty InitialContext
        ctx = InitialContext()

        // Load the properties into the InitialContext
        val loader = JndiLoader(ctx.environment)
        loader.load(props, ctx)
    }

    @AfterEach
    fun teardownSharedState() {
        ctx.close()
    }

    @Test
    fun validateLookup() {

        // Assemble

        // Act
        val fooValue = Contexts.lookup(ctx, "java:foo", String::class.java)
        val stringBuilderValue = Contexts.lookup(ctx, "java:theStringBuilder", StringBuilder::class.java)

        // Assert
        assertThat(fooValue).isEqualTo("fooValue")
        assertThat(stringBuilderValue.toString()).isEqualTo("tjo")
    }

    @Test
    fun validateBinding() {

        // Assemble
        val key = "java:something"
        val value = "someValue"

        // Act
        Contexts.put(ctx, key, value)
        val result = Contexts.lookup(ctx, key, String::class.java)

        // Assert
        assertThat(result).isEqualTo(value)
    }
}