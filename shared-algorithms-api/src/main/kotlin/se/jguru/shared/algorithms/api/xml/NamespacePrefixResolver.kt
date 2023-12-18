/*-
 * #%L
 * Nazgul Project: jguru-shared-algorithms-api
 * %%
 * Copyright (C) 2018 - 2023 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * 
 *       http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package se.jguru.shared.algorithms.api.xml

import java.util.Collections
import java.util.SortedMap
import java.util.TreeMap

/**
 *
 * Specification for how to map XML Namespace URIs to XML Namespace Prefixes.
 * XML namespaces are provided in the form of an URI-formatted String, such as
 * `http://www.jguru.se/nazgul/core`, and XML prefixes are simply identifiers
 * for these URIs (such as `core`). In this sense, all XML binding implementations
 * simply need a means to relate the prefix to the NamespaceURI and vice versa.
 *
 * Some XmlBinder implementations [notably JAXB] have rather sketchy implementations of
 * these mapping mechanics, so we provide this NamespacePrefixResolver specification
 * to provide a unified specification for relating XML namespace URLs to Prefixes.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 * @see SimpleNamespacePrefixResolver
 */
interface NamespacePrefixResolver {

    /**
     * @return A non-modifiable List holding all currently registered XML namespaceURIs.
     */
    fun getRegisteredNamespaceURIs(): Set<String>

    /**
     * @return A non-modifiable List holding all currently registered XML prefixes.
     */
    fun getRegisteredPrefixes(): Set<String>

    /**
     * Adds the provided mapping between a single xmlNamespaceUri and corresponding xmlPrefix.
     *
     * @param xmlNamespaceUri the unique URI of an XML namespace. Should not be empty.
     * @param xmlPrefix       the unique prefix of the provided XML namespace. Should not be empty.
     * @throws NullPointerException     if any argument was `null`.
     * @throws IllegalArgumentException if the xmlNamespaceUri was already registered to another prefix, or
     * if any argument was empty.
     */
    @Throws(NullPointerException::class, IllegalArgumentException::class)
    fun put(xmlNamespaceUri: String, xmlPrefix: String)

    /**
     * Convenience method, adding all provided mappings between xmlNamespaceUris and corresponding xmlPrefixes.
     *
     * @param xmlUri2PrefixMap A Map relating XML namespace URIs to corresponding prefixes.
     * @throws NullPointerException     if any argument or element was `null`.
     * @throws IllegalArgumentException if any xmlNamespaceUri was already registered to another prefix, or
     * if any argument was empty.
     */
    @Throws(NullPointerException::class, IllegalArgumentException::class)
    fun putAll(xmlUri2PrefixMap: Map<String, String>)

    /**
     * Retrieves the XML namespace URI for the provided xmlPrefix, or `null` if none was found.
     *
     * @param xmlPrefix The XML prefix for which to obtain the corresponding XML namespace.
     * @return the XML namespace URI for the provided xmlPrefix, or `null` if none was found.
     */
    fun getNamespaceUri(xmlPrefix: String): String?

    /**
     * Retrieves the XML prefix for the provided xmlNamespaceUri, or `null` if none was found.
     *
     * @param xmlNamespaceUri The XML namespace URI for which to obtain the corresponding XML prefix.
     * @return the XML prefix for the provided xmlNamespaceUri, or `null` if none was found.
     */
    fun getXmlPrefix(xmlNamespaceUri: String): String?

    /**
     * Retrieves a [Map] relating String NamespaceURIs (as keys) to String Prefixes (as values).
     *
     * @return A map relating String NamespaceURIs to String Prefixes.
     */
    fun toMap(): Map<String, String>

    companion object {

        /**
         * Empty/undeterminate namespace URI.
         */
        val EMPTY_NAMESPACE = ""
    }
}

/**
 * Simple NamespacePrefixResolver implementation sporting full functionality, but
 * not tailored to any particular JAXB implementation. Extend this class to implement
 * particular integrations into Metro, EclipseLink or other JAXB implementation.
 * Alternatively, delegate to this implementation if that suits the needs better.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
open class SimpleNamespacePrefixResolver(
    protected open val namespaceUri2PrefixMap: SortedMap<String, String>
) : NamespacePrefixResolver {

    /**
     * Default constructor which uses an empty [TreeMap] to store the namespaceURIs and their corresponding prefixes.
     */
    constructor() : this(TreeMap())

    override fun getRegisteredNamespaceURIs(): Set<String> = namespaceUri2PrefixMap.keys

    override fun getRegisteredPrefixes(): Set<String> = Collections.unmodifiableSet(
        namespaceUri2PrefixMap.values.toSet())

    override fun put(xmlNamespaceUri: String, xmlPrefix: String) {
        namespaceUri2PrefixMap[xmlNamespaceUri] = xmlPrefix
    }

    override fun putAll(xmlUri2PrefixMap: Map<String, String>) {
        namespaceUri2PrefixMap.putAll(xmlUri2PrefixMap)
    }

    override fun getNamespaceUri(xmlPrefix: String): String? {

        return namespaceUri2PrefixMap
            .entries
            .firstOrNull { it.value.equals(xmlPrefix, ignoreCase = true) }?.key
    }

    override fun getXmlPrefix(xmlNamespaceUri: String): String? = namespaceUri2PrefixMap[xmlNamespaceUri]

    override fun toString(): String = "SimpleNamespacePrefixResolver with [${namespaceUri2PrefixMap.size}] entries: " +
        namespaceUri2PrefixMap.entries.map { e -> "\n [${e.key}]: ${e.value}" }

    override fun toMap(): Map<String, String> = Collections.unmodifiableMap(namespaceUri2PrefixMap)
}
