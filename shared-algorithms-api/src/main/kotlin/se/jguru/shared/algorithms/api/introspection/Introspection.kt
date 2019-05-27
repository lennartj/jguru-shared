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
package se.jguru.shared.algorithms.api.introspection

import java.net.URL
import java.security.CodeSource
import java.security.ProtectionDomain
import java.util.SortedMap
import java.util.SortedSet
import java.util.TreeMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.jar.JarFile
import java.util.jar.Manifest
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

/**
 * Algorithms aimed at type introspection, extracting type information as required.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
object Introspection {

    /**
     * Standard [Comparator] for classes, comparing their respective names.
     */
    val CLASSNAME_COMPARATOR: Comparator<Class<*>> = Comparator { l, r -> l.name.compareTo(r.name) }

    /**
     * The location of the MANIFEST.MF resource within a JAR.
     */
    const val MANIFEST_RESOURCE = "/META-INF/MANIFEST.MF"

    /**
     * The name of the bundle version property within a MANIFEST.MF file.
     */
    const val BUNDLE_VERSION = "Bundle-Version"

    /**
     * The name of the specification version property within a MANIFEST.MF file.
     */
    const val SPECIFICATION_VERSION = "Specification-Version"

    /**
     * Retrieves type information from the supplied objects.
     *
     * @param classLoader The classloader used to harvest the type information
     * @param objects The objects from which to harvest type information.
     */
    @JvmStatic
    fun getTypesFrom(vararg objects: Any): Set<Class<*>> {

        val toReturn = HashSet<Class<*>>()

        for (current in objects) {
            populateTypeInformationFrom(toReturn, current)
        }

        // All Done.
        return toReturn
    }

    /**
     * Retrieves type information from the supplied objects, but typecast to the fully qualified class names.
     *
     * @param classLoader The classloader used to harvest the type information
     * @param objects The objects from which to harvest type information.
     */
    @JvmStatic
    fun getTypeNamesFrom(vararg objects: Any): SortedSet<String> =
        getTypesFrom(objects).map { it.name }.toSortedSet()

    /**
     * Updates all mutable/"var" properties within the supplied target object with the values from the source.
     * Both are expected to be of type T.
     *
     * @param aClass The class defining the properties.
     * @param source The source object.
     * @param target The target object.
     *
     * @return `true` if any properties were updated, and `false` otherwise.
     */
    @JvmStatic
    fun <T : Any> updateProperties(aClass: KClass<T>, source: T, target: T): Boolean {

        val toReturn = AtomicBoolean(false)
        val varProps = getMutablePropertiesFor(aClass)

        varProps
            .map { updateProperty(source, target, it) }
            .filter { it }
            .forEach { _ -> toReturn.set(true) }

        // All Done.
        return toReturn.get()
    }

    /**
     * Retrieves all mutable properties (i.e. "var"s) defined within the supplied [aClass].
     * Each property is assumed to be a [KMutableProperty1], compliant with the JavaBean
     * paradigm for setters and getters.
     *
     * @param aClass The class defining the properties.
     *
     * @return a List of [KMutableProperty1] representing the setters and getters of the property.
     */
    @JvmStatic
    fun <T : Any> getMutablePropertiesFor(aClass: KClass<T>): List<KMutableProperty1<T, *>> =
        aClass.memberProperties
            .filter { it is KMutableProperty1 }
            .map { it as KMutableProperty1<T, *> }
            .toList()

    /**
     * Updates all mutable/"var" properties within the supplied target object with
     * the values from the source. Both are expected to be of type T.
     *
     * @param source The source object.
     * @param target The target object.
     * @param setter The mutable property setter.
     *
     * @return `true` if any properties were updated, and `false` otherwise.
     */
    @JvmStatic
    fun <T, P> updateProperty(source: T, target: T, setter: KMutableProperty1<T, P>): Boolean {

        val getter = setter as KProperty1<T, P>

        // Find the current values
        val existingProperty = getter.get(target)
        val incomingProperty = getter.get(source)

        if (existingProperty != incomingProperty) {

            // Update the target instance
            setter.set(target, incomingProperty)

            // All done.
            return true
        }

        // All Done
        return false
    }

    /**
     * Populates the supplied typeSet with all types found within the supplied [anObject]
     */
    @JvmStatic
    fun populateTypeInformationFrom(typeSet: MutableSet<Class<*>> = HashSet(), anObject: Any) {

        // Check sanity
        val immediateClass = anObject::class.java

        // Add self first.
        typeSet.add(immediateClass)

        // Recurse if required.
        when (anObject) {
            is Collection<*> -> anObject
                .filterNotNull()
                .forEach { populateTypeInformationFrom(typeSet, it) }
            is Map<*, *> -> anObject.entries
                .filter { e -> e.key != null && e.value != null }
                .forEach { populateTypeInformationFrom(typeSet, it.value!!) }
            is Array<*> -> anObject.filterNotNull().forEach { populateTypeInformationFrom(typeSet, it) }
        }
    }

    /**
     * Retrieves the [CodeSource] for the given [Class].
     *
     * @return The [CodeSource] of for the [ProtectionDomain] of the supplied [Class], or `null` for
     * system-loaded classes.
     * @throws SecurityException if a [SecurityManager] exists and its [SecurityManager.checkPermission] method
     * doesn't allow getting the ProtectionDomain.
     */
    @JvmStatic
    @Throws(SecurityException::class)
    fun getCodeSourceFor(aClass: Class<*>): CodeSource? = aClass.protectionDomain.codeSource

    /**
     * Retrieves a string relating a diagnostic message regarding the CodeSource Location of the supplied Class.
     *
     * @param aClass The class for which to retrieve a diagnostic message containing the CodeSource's Location, if
     * that could be extracted from the supplied aClass.
     * @return a string relating a diagnostic message regarding the CodeSource Location of the supplied Class.
     */
    @JvmStatic
    fun getCodeSourcePrintoutFor(aClass: Class<*>): String {

        val builder = StringBuilder()

        // #1) Fetch the ProtectionDomain
        //
        val protectionDomain = aClass.protectionDomain

        when (protectionDomain) {

            null -> builder.append("Null ProtectionDomain for [${aClass.name}]. No CodeSource info can be retrieved.")
            else -> try {

                // #2) Fetch the ClassLoader and CodeSource of the retrieved ProtectionDomain.
                //
                val classLoader = protectionDomain.classLoader
                val codeSource = protectionDomain.codeSource

                // #3) Ensure we have output results even if the
                //
                val classLoaderType = when (classLoader) {
                    null -> "SystemClassLoader (<null> result for protectionDomain.getClassLoader())"
                    else -> classLoader.javaClass.name
                }

                val codeSourceLocation = when (codeSource) {
                    null -> "<null> CodeSource"
                    else -> codeSource.location.toString() + " --- " + codeSource.toString()
                }

                builder.append("Class ${aClass.name} is loaded by [$classLoaderType] from [$codeSourceLocation]")

            } catch (e: Throwable) {

                builder.append("Could not acquire ClassLoader or CodeSource from class ["
                    + aClass.name + "]. This is weird.")
            }
        }

        // All Done.
        return builder.toString()
    }

    /**
     * Retrieves the System Properties, typecast as a SortedMap of strings.
     *
     * @param propertyKeyFilter An optional filter indicating which property keys should be included in the result.
     * @return A SortedMap containing all System Properties.
     */
    @JvmStatic
    @JvmOverloads
    fun getSystemProperties(propertyKeyFilter: (String) -> Boolean = { true }): SortedMap<String, String> {

        val toReturn = TreeMap<String, String>()

        // Filter
        val sysPropKeys = System.getProperties().propertyNames()
            .asSequence()
            .filter { aKey -> propertyKeyFilter.invoke(aKey as String) }
            .map { c -> c as String }
            .toSet()

        // Collect
        sysPropKeys.forEach { toReturn[it] = System.getProperty(it) }

        // All Done.
        return toReturn
    }

    /**
     * Retrieves the Manifest created from the Manifest.MF file residing within the CodeSource containing the aClass.
     *
     * @param aClass A Class within the CodeSource for which a Manifest.MF file should be retreived.
     * @param loader The ClassLoader used to load the `MANIFEST_RESOURCE`. Defaults to the thread context classloader.
     *
     * @return the Manifest object wrapping the MANIFEST.MF file.
     */
    @JvmStatic
    @JvmOverloads
    @Throws(IllegalArgumentException::class)
    fun getManifestFrom(aClass: Class<*>, loader: ClassLoader = Thread.currentThread().contextClassLoader)
        : Manifest {

        val codeSource = getCodeSourceFor(aClass)

        val manifestFileURL = when (codeSource) {
            null -> loader.getResource(MANIFEST_RESOURCE)
            else -> {

                val codeSourceLocation = codeSource.location
                val stringSourceURL = codeSourceLocation.toString()

                // We currently only support file and jar protocols.
                when (isJarFile(codeSourceLocation)) {
                    false -> {

                        val slashSeparator = "/target/"
                        val targetIndex = stringSourceURL.indexOf(slashSeparator)
                        URL(stringSourceURL.substring(0, targetIndex) + "/target/classes" + MANIFEST_RESOURCE)
                    }
                    true -> {

                        val jarDashIndex = stringSourceURL.indexOf("!")

                        // Peel off the JarFile '!' mark, if it is present within the URL
                        val uriSource = when (jarDashIndex) {
                            -1 -> URL(stringSourceURL)
                            else -> URL(stringSourceURL.substring(0, jarDashIndex))
                        }

                        // This is a JarFile; use standard mechanics to get its Manifest.
                        return JarFile(uriSource.path).manifest
                    }
                }
            }
        }

        // All Done.
        return Manifest(manifestFileURL.openStream())
    }

    /**
     * Finds the [RuntimeVersion] as defined by the given [propNames] property within the supplied Manifest.
     *
     * @param propNames The name of the property holding the version.
     * @param theManifest The [Manifest] from which to read the [RuntimeVersion].
     *
     * @return the [RuntimeVersion] parsed from the given propNames.
     *
     * @see BUNDLE_VERSION
     * @see SPECIFICATION_VERSION
     */
    @JvmStatic
    fun findVersionFromManifestProperty(
        theManifest: Manifest,
        propNames: List<String> = listOf(BUNDLE_VERSION, SPECIFICATION_VERSION)): RuntimeVersion {

        return findVersionFromMap(extractMapOf(theManifest), propNames)
    }

    /**
     * Finds the [RuntimeVersion] as defined by the given [propNames] property within the supplied Map, which
     * is typically retrieved from a MANIFEST.MF file.
     *
     * @param propNames The names of the properties holding the version.
     * @param propertyMap The Map holding the Version string, from which to parse the [RuntimeVersion].
     *
     * @return the [RuntimeVersion] parsed from the given propNames.
     *
     * @see BUNDLE_VERSION
     * @see SPECIFICATION_VERSION
     */
    @JvmStatic
    fun findVersionFromMap(propertyMap: Map<String, String>,
                           propNames: List<String> = listOf(BUNDLE_VERSION, SPECIFICATION_VERSION)): RuntimeVersion {

        if(propNames.isEmpty()) {
            throw IllegalArgumentException("Cannot handle empty 'propNames' argument.")
        }

        val toParse = propNames.map { propertyMap[it] }.firstOrNull { it != null }
            ?: throw IllegalArgumentException("Found no value within property $propNames of given Map: $propertyMap")

        return RuntimeVersion.parseVersionString(toParse)
    }

    /**
     * Converts the main attributes supplied Manifest to a SortedMap.
     *
     * Frequently used with the `getManifestFromCodeSourceOf` method, to yield something like:
     * `val theMap = Introspection.extractMapOf(Introspection.getManifestFromCodeSourceOf(SomeClass::class.java))`
     *
     * @param aManifest The Manifest from which to extract attributes into a Map
     *
     * @return a SortedMap relating Manifest attributes.
     */
    @JvmStatic
    fun extractMapOf(aManifest: Manifest): SortedMap<String, String> {

        val toReturn = TreeMap<String, String>()

        // Copy the Main Attributes
        aManifest.mainAttributes.entries.forEach { (k, v) -> toReturn["" + k] = "" + v }

        // All Done.
        return toReturn
    }

    /**
     * ## As defined within the URLClassLoader documentation
     *
     * Any URL that ends with a '/' is assumed to refer to a directory.
     * Otherwise, the URL is assumed to refer to a JAR file which will be opened as needed.
     */
    @JvmStatic
    fun isJarFile(aURL: URL): Boolean {

        val lcProtocol = aURL.protocol.toLowerCase()

        if (lcProtocol != "jar" && lcProtocol != "file") {
            throw IllegalArgumentException("Unsupported protocol [${aURL.protocol}]. " +
                "Can only handle 'file' and 'jar' protocols.")
        }

        return lcProtocol == "jar" || (lcProtocol == "file" && !aURL.path.endsWith("/"))
    }
}