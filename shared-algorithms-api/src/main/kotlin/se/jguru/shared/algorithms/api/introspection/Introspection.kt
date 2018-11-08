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

import java.security.CodeSource
import java.security.ProtectionDomain
import java.util.SortedMap
import java.util.SortedSet
import java.util.TreeMap
import java.util.concurrent.atomic.AtomicBoolean
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
    val CLASSNAME_COMPARATOR: Comparator<Class<*>> = Comparator({ l, r -> l.name.compareTo(r.name) })

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
    fun <T : Any> updateProperties(aClass: KClass<T>, source: T, target: T) : Boolean {

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
    fun <T, P> updateProperty(source: T, target: T, setter: KMutableProperty1<T, P>) : Boolean {

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
}