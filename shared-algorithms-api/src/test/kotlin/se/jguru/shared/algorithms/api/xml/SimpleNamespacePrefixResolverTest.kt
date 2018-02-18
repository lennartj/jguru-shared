package se.jguru.shared.algorithms.api.xml

import org.junit.Assert
import org.junit.Test

/**
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class SimpleNamespacePrefixResolverTest {

    @Test
    fun validateNormalOperations() {

        // Assemble
        val prefix = "someuri"
        val namespaceURI = "http://some/uri"

        val unitUnderTest = SimpleNamespacePrefixResolver()
        unitUnderTest.put(namespaceURI, prefix)

        // Act
        val namespaceURIs = unitUnderTest.getRegisteredNamespaceURIs()
        val prefixes = unitUnderTest.getRegisteredPrefixes()

        // Assert
        Assert.assertEquals(1, namespaceURIs.size)
        Assert.assertEquals(1, prefixes.size)

        Assert.assertEquals(prefix, prefixes.first())
        Assert.assertEquals(namespaceURI, namespaceURIs.first())
        Assert.assertEquals(prefix, unitUnderTest.getXmlPrefix(namespaceURI))
        Assert.assertEquals(namespaceURI, unitUnderTest.getNamespaceUri(prefix))
    }
}