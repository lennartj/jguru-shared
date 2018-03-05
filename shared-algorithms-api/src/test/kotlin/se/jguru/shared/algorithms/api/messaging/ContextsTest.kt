package se.jguru.shared.algorithms.api.messaging

import org.junit.AfterClass
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.springframework.mock.jndi.SimpleNamingContextBuilder
import java.util.Hashtable
import javax.naming.Context
import javax.naming.spi.InitialContextFactory


/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class ContextsTest {

    // Shared state
    companion object {

        lateinit var jndiBuilder: SimpleNamingContextBuilder

        @JvmStatic
        @BeforeClass
        fun setUpClass() {

            jndiBuilder = SimpleNamingContextBuilder()
            jndiBuilder.bind("java:foo", "fooValue")
            jndiBuilder.bind("java:bar", "barValue")
            jndiBuilder.bind("java:theStringBuilder", StringBuilder("tjo"))
            jndiBuilder.activate()
        }

        @JvmStatic
        @AfterClass
        fun tearDownClass() {
            jndiBuilder.deactivate()
        }
    }

    // Shared state
    lateinit var initContextFactory : InitialContextFactory
    lateinit var ctx: Context

    @Before
    fun setupSharedState() {

        initContextFactory = jndiBuilder.createInitialContextFactory(Hashtable<Any, Any>())
        ctx = initContextFactory.getInitialContext(Hashtable<Any, Any>())
    }

    @Test
    fun validateLookup() {

        // Assemble

        // Act
        val fooValue = Contexts.lookup(ctx, "java:foo", String::class.java)
        val stringBuilderValue = Contexts.lookup(ctx, "java:theStringBuilder", StringBuilder::class.java)

        // Assert
        Assert.assertEquals("fooValue", fooValue)
        Assert.assertEquals("tjo", stringBuilderValue.toString())
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
        Assert.assertEquals(value, result)

    }
}