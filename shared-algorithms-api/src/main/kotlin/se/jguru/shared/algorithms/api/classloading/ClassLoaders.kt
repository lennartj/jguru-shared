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
package se.jguru.shared.algorithms.api.classloading

import org.slf4j.LoggerFactory
import se.jguru.shared.algorithms.api.introspection.RuntimeVersion
import java.security.PrivilegedAction

/**
 * Convenience methods to retrieve [ClassLoader]s
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
object ClassLoaders {

    // Our Logger
    @JvmStatic
    private val log = LoggerFactory.getLogger(ClassLoaders::class.java)

    /**
     * Retrieves the [ClassLoader] which loaded the supplied aClass.
     *
     * @return the [ClassLoader] which loaded the supplied [aClass].
     */
    @JvmStatic
    fun getClassLoaderFor(aClass: Class<*>): ClassLoader = getClassLoader { aClass.classLoader }

    /**
     * Retrieves the current Thread context [ClassLoader].
     *
     * @return the current Thread context [ClassLoader].
     */
    @JvmStatic
    fun getContextClassLoader(): ClassLoader = getClassLoader { Thread.currentThread().contextClassLoader }

    /**
     * Retrieves the system [ClassLoader].
     *
     * @return the system [ClassLoader].
     */
    @JvmStatic
    fun getSystemClassLoader(): ClassLoader = getClassLoader { ClassLoader.getSystemClassLoader() }

    /**
     * The platform [ClassLoader]. Note that this method will only work properly when called on JRE9+.
     *
     * @return the JDK 9+ platform [ClassLoader].
     * @throws IllegalStateException if executed on a [Runtime.version] less than 9.
     */
    @JvmStatic
    @Throws(IllegalStateException::class)
    fun getPlatformClassLoader(): ClassLoader {

        val version = RuntimeVersion.parseJavaVersion()
        if (version.major < 9) {
            throw IllegalStateException("Platform ClassLoader is only available for Java 9+. " +
                "(You currently run $version).")
        }

        // Fetch using reflection to enable compiling for JDK8
        var platformClassLoader: ClassLoader? = null
        try {
            val platformClassLoaderMethod = ClassLoader::class.java.getMethod("getPlatformClassLoader")
            platformClassLoader = platformClassLoaderMethod.invoke(null, null) as ClassLoader
        } catch (e: Exception) {
            throw IllegalArgumentException("Could not retrieve platform ClassLoader", e)
        }

        // All Done.
        return getClassLoader { platformClassLoader }
    }

    //
    // Private helpers
    //

    @JvmStatic
    private fun getClassLoader(loaderRetriever: () -> ClassLoader): ClassLoader =
        when (System.getSecurityManager() == null) {
            true -> loaderRetriever.invoke()
            false -> java.security.AccessController.doPrivileged(PrivilegedAction { loaderRetriever.invoke() })
        }
}
