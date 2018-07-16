/*-
 * #%L
 * Nazgul Project: jguru-shared-persistence-spi-jpa
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
package se.jguru.shared.persistence.spi.jpa.classloading

import org.slf4j.LoggerFactory
import java.io.IOException
import java.lang.NullPointerException
import java.net.URL
import java.util.Enumeration

/**
 * Creates a new PersistenceRedirectionClassLoader delegating all class loads to the
 * provided parent, except loading of "META-INF/persistence.xml", which is redirected
 * to the provided [redirectTo] resource path.
 *
 * @param parent The normal classloader, used for all resource loading except "META-INF/persistence.xml"
 * @param redirectTo The location to use when loading "META-INF/persistence.xml". An example would be
 * "META-INF/dbprimer_persistence.xml"
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class PersistenceRedirectionClassLoader(

    parent: ClassLoader,

    val redirectTo: String,

    val caseInsensitive :Boolean = true

) : ClassLoader(parent) {

    init {

        if (redirectTo.isEmpty()) {
            throw IllegalArgumentException("Cannot handle empty 'redirectTo' parameter.")
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun findResource(name: String?): URL? = when (PERSISTENCE_XML.equals(name, caseInsensitive)) {

        true -> {

            if (log.isDebugEnabled) {
                log.debug("Redirected [$name] ==> $redirectTo")
            }

            // Retrieve amn URL to the redirected resource
            parent.getResource(redirectTo)
        }
        else -> {

            // Signal to the Parent classloader that
            // this one does not know anything about
            // the name/resource supplied.
            null
        }
    }


    /**
     * Finds all the resources with the given name. A resource is some data
     * (images, audio, text, etc) that can be accessed by class code in a way
     * that is independent of the location of the code.
     *
     * <p> The name of a resource is a {@code /}-separated path name that
     * identifies the resource. </p>
     *
     * <p> Resources in named modules are subject to the encapsulation rules
     * specified by {@link Module#getResourceAsStream Module.getResourceAsStream}.
     * Additionally, and except for the special case where the resource has a
     * name ending with "{@code .class}", this method will only find resources in
     * packages of named modules when the package is {@link Module#isOpen(String)
     * opened} unconditionally (even if the caller of this method is in the
     * same module as the resource). </p>
     *
     * @implSpec The default implementation will first search the parent class
     * loader for the resource; if the parent is {@code null} the path of the
     * class loader built into the virtual machine is searched. It then
     * invokes {@link #findResources(String)} to find the resources with the
     * name in this class loader. It returns an enumeration whose elements
     * are the URLs found by searching the parent class loader followed by
     * the elements found with {@code findResources}.
     *
     * @apiNote Where several modules are defined to the same class loader,
     * and where more than one module contains a resource with the given name,
     * then the ordering is not specified and may be very unpredictable.
     * When overriding this method it is recommended that an
     * implementation ensures that any delegation is consistent with the {@link
     * #getResource(java.lang.String) getResource(String)} method. This should
     * ensure that the first element returned by the Enumeration's
     * {@code nextElement} method is the same resource that the
     * {@code getResource(String)} method would return.
     *
     * @param  name
     *         The resource name
     *
     * @return  An enumeration of {@link java.net.URL URL} objects for
     *          the resource. If no resources could  be found, the enumeration
     *          will be empty. Resources for which a {@code URL} cannot be
     *          constructed, are in package that is not opened unconditionally,
     *          or access to the resource is denied by the security manager,
     *          are not returned in the enumeration.
     *
     * @throws  IOException
     *          If I/O errors occur
     * @throws  NullPointerException If {@code name} is {@code null}
     *
     * @since  1.2
     * @revised 9
     * @spec JPMS
     */
    @Throws(IOException::class)
    override fun getResources(name: String): Enumeration<URL>? = when (PERSISTENCE_XML == name) {

        true -> {

            if (log.isDebugEnabled) {
                log.debug("Redirected [$name] ==> $redirectTo")
            }
            // Redirect to the desired resource
            parent.getResources(redirectTo)
        }

        false -> {

            if (log.isDebugEnabled) {
                log.debug("Delegating [$name] ==> parent")
            }

            // Delegate to the parent classloader.
            parent.getResources(name)
        }
    }

    override fun toString(): String {
        return "PersistenceRedirectionClassLoader(" +
            "redirectTo='$redirectTo', " +
            "caseInsensitive=$caseInsensitive, " +
            "parentType=${parent::class.java.name})"
    }


    companion object {

        // Our log
        @JvmStatic
        private val log = LoggerFactory.getLogger(PersistenceRedirectionClassLoader::class.java)

        /**
         * The standard URL of the persistence.xml.
         */
        const val PERSISTENCE_XML = "META-INF/persistence.xml"
    }
}
