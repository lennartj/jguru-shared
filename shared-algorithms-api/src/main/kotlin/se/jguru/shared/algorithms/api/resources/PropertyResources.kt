/*-
 * #%L
 * Nazgul Project: jguru-shared-algorithms-api
 * %%
 * Copyright (C) 2018 jGuru Europe AB
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
package se.jguru.shared.algorithms.api.resources

import org.slf4j.LoggerFactory
import java.io.InputStream
import java.net.URL
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.SortedMap
import java.util.TreeMap

/**
 * Algorithms to simplify extracting data from property files embedded within JARs.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
object PropertyResources {

    // Our Logger
    private val log = LoggerFactory.getLogger(PropertyResources::class.java)

    /**
     * Default separator within property files.
     */
    const val DEFAULT_SEPARATOR = "="

    /**
     * Retrieves a Set containing all URLs matching the resources with the supplied path and accepted by the given
     * urlFilter, as found by the supplied [ClassLoader].
     *
     * @param classLoader The [ClassLoader] which should be used to load resources.
     * Defaults to `Thread.currentThread().contextClassLoader`
     * @param urlFilter A filter accepting or rejecting individual URLs.
     * @param resourcePath The resource path used to retrieve the resource URLs from the supplied [ClassLoader].
     *
     * @return A Set of the URLs on the supplied resourcePath which were also accepted by the supplied urlFilter.
     */
    @JvmStatic
    @JvmOverloads
    fun getResourceURLs(resourcePath: String,
                        urlFilter: (URL) -> Boolean = { true },
                        classLoader: ClassLoader = Thread.currentThread().contextClassLoader): Set<URL> {

        val toReturn = mutableSetOf<URL>()

        for (current in classLoader.getResources(resourcePath)) {

            // Only add the accepted URLs.
            if (urlFilter.invoke(current)) {
                toReturn.add(current)
            }
        }

        // All Done.
        return toReturn
    }

    /**
     * Utility method to read all (string formatted) data from the given classpath-relative
     * resource file and return the data as a String.
     *
     * @param resourcePath The resource path where the resource/file is found.
     * @param classLoader The [ClassLoader] used to read the property file
     * @param charset The charset expected within resource file. Defaults to [StandardCharsets.UTF_8]
     * @param useFunction The converter function transforming the [InputStream]
     * from the [resourcePath] URL to a String.
     * @return A [String] containing the key/value pairs read from the property file found.
     * @throws ResourceNotFoundException if the supplied [classLoader] could not load the resource
     * from the supplied [resourcePath]
     */
    @JvmStatic
    @JvmOverloads
    @Throws(IllegalArgumentException::class)
    fun readFully(resourcePath: String,
                  classLoader: ClassLoader = Thread.currentThread().contextClassLoader,
                  charset: Charset = StandardCharsets.UTF_8,
                  useFunction: (_: InputStream) -> String =
                  { it.bufferedReader(charset).use { it.readText() } }): String {

        // Open an InputStream to the URL at the given resourcePath.
        val resourceURL = classLoader.getResource(resourcePath)
            ?: throw ResourceNotFoundException("No resource found at path [$resourcePath]")

        // Open the stream, wrap it into an InputStreamReader and pass it to the handler method.
        return resourceURL.openStream().use { useFunction(it) }
    }

    /**
     * Parses a property file into a SortedMap with string keys and values.
     * The property file is expected to be stored at the [resourcePath] (which should not start with a "/") within
     * the classpath accessed by [classLoader].
     *
     * @param classLoader The [ClassLoader] used to read the property file
     * @param charset The charset expected within the property files. Defaults to [StandardCharsets.UTF_8]
     * @param urlFilter The filter applied to each URL found by the classLoader to isolate the property
     * resource to read/parse. Defaults to `true`.
     * @param commentFilter The filter applied to each line within the property file, to determine which lines are
     * comments (and will therefore be ignored). Defaults to accepting non-empty, trimmed lines not starting with "#".
     * @param separator The separator dividing key and value within a single property line.
     * Defaults to [DEFAULT_SEPARATOR].
     * @param resourcePath The resource path where the property file is found.
     * @return A SortedMap containing the key/value pairs read from the property file found.
     */
    @JvmStatic
    @JvmOverloads
    @Throws(IllegalArgumentException::class)
    fun parseResource(
        classLoader: ClassLoader = Thread.currentThread().contextClassLoader,
        charset: Charset = StandardCharsets.UTF_8,
        urlFilter: (URL) -> Boolean = { true },
        commentFilter: (String) -> Boolean = { aLine ->
            val trimmed = aLine.trim()
            trimmed.isNotEmpty() && !trimmed.startsWith("#")
        },
        separator: String = DEFAULT_SEPARATOR,
        resourcePath: String): SortedMap<String, String> {

        // Find the (single!) matching resource URL
        //
        val matchingURLs = getResourceURLs(resourcePath, urlFilter, classLoader)
        if (matchingURLs.size > 1) {
            throw IllegalArgumentException("Found ${matchingURLs.size} matching resource path $resourcePath. " +
                "Expected exactly 1. " +
                "Please adjust the urlFilter parameter to narrow the search result.")
        }

        val toReturn = TreeMap<String, String>()

        // Now read the contents of the URLs
        matchingURLs
            .first()
            .openStream()
            .bufferedReader(charset)
            .use {

                it.lines() // Read all lines
                    .filter { aLine -> commentFilter(aLine) }
                    .forEach { aLine ->

                        // Split and handle.
                        val pair = splitPropertyLine(aLine, separator)
                        if (pair != null) {
                            toReturn[pair.first] = pair.second
                        }
                    }
            }

        // All Done.
        return toReturn
    }

    /**
     * Splits a property line into a Pair(key, value).
     *
     * @param aLine The line to split into key and value. Should not be empty.
     * @param separator The String acting as separator. Defaults to [DEFAULT_SEPARATOR].
     *
     * @return a [Pair] containing the key and value from the supplied property line, or null if none could be parsed.
     */
    @JvmStatic
    @JvmOverloads
    fun splitPropertyLine(aLine: String, separator: String = DEFAULT_SEPARATOR): Pair<String, String>? {

        // The line should have the structure [key][separator][value]
        if (!aLine.contains(separator)) {
            log.warn("Disregarding property line [$aLine], since it does not contain required separator [$separator]")
            return null
        }

        val trimmed = aLine.trim()
        val separatorIndex = trimmed.indexOf(separator)

        val key = trimmed.substring(0, separatorIndex).trim()
        val value = trimmed.substring(separatorIndex + separator.length).trim()

        if (key.isEmpty()) {
            log.warn("Disregarding property line [$aLine], since it contains an empty key")
            return null
        }

        // All Done.
        return Pair(key, value)
    }
}
