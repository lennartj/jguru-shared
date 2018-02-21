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

/**
 * Algorithms aimed at type introspection, extracting type information as required.
 *
 * @author [Lennart Jörelid](mailto:lj@jguru.se), jGuru Europe AB
 */
object Introspection {

    fun getTypesFrom(classLoader: ClassLoader = Thread.currentThread().contextClassLoader,
                     vararg objects: Any) : SortedSet<Class<*>> {
    }

    fun getTypeInformationFrom(classLoader: ClassLoader = Thread.currentThread().contextClassLoader,
                               vararg objects: Any): SortedSet<String> {
    }
}