package se.jguru.shared.algorithms.api.xml

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

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
        assertThat(namespaceURIs.size).isEqualTo(1)
        assertThat(prefixes.size).isEqualTo(1)

        assertThat(prefixes.first()).isEqualTo(prefix)
        assertThat(namespaceURIs.first()).isEqualTo(namespaceURI)
        assertThat(unitUnderTest.getXmlPrefix(namespaceURI)).isEqualTo(prefix)
        assertThat(unitUnderTest.getNamespaceUri(prefix)).isEqualTo(namespaceURI)
    }
}