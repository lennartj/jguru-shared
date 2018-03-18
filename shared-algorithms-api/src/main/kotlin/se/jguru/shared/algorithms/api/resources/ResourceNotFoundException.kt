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

import java.net.URL

/**
 * Exception indicating that a resource (or resource URL) was not found.
 * The cause is not initialized, and may subsequently be initialized by a call to {@link #initCause}.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 * @param message The Exception's detail message.
 */
class ResourceNotFoundException(message: String) : RuntimeException(message) {

    /**
     * Convenience constructor generating a standard Exception message from the supplied [URL]
     *
     * @param resourceURL The URL of the resource which was not found.
     */
    constructor(resourceURL: URL) : this("Could not find resource ${resourceURL.toURI()}")
}
