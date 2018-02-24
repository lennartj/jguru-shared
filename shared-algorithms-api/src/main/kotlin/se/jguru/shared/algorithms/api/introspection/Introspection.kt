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

import java.util.SortedSet
import java.util.TreeSet

/**
 * Algorithms aimed at type introspection, extracting type information as required.
 *
 * @author [Lennart Jörelid](mailto:lj@jguru.se), jGuru Europe AB
 */
object Introspection {

    /**
     * Retrieves type information from the supplied objects.
     *
     * @param classLoader The classloader used to harvest the type information
     * @param objects The objects from which to harvest type information.
     */
    fun getTypesFrom(vararg objects: Any): SortedSet<Class<*>> {

        val toReturn = TreeSet<Class<*>>()

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
    fun populateTypeInformation(classLoader: ClassLoader = Thread.currentThread().contextClassLoader,
                                vararg objects: Any): SortedSet<String> =
        getTypesFrom(classLoader, objects).map { it.name }.toSortedSet()

    /**
     * Populates the supplied typeSet with all types found within the supplied [anObject]
     */
    fun populateTypeInformationFrom(typeSet: SortedSet<Class<*>> = TreeSet({ l, r -> l.name.compareTo(r.name) }),
                                    anObject: Any) {

        // Add self first.
        typeSet.add(anObject.javaClass)

        // Recurse if required.
        when (anObject) {
            is Collection<*> -> anObject.filterNotNull().forEach { populateTypeInformationFrom(typeSet, it) }
            is Map<*, *> -> anObject.entries
                .filter { e -> e.key != null && e.value != null }
                .forEach { populateTypeInformationFrom(typeSet, it.value!!) }
            is Array<*> -> anObject.filterNotNull().forEach { populateTypeInformationFrom(typeSet, it) }
        }
    }
}