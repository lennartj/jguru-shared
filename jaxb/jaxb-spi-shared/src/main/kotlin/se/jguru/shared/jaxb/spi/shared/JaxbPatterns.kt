/*-
 * #%L
 * Nazgul Project: jguru-shared-jaxb-spi-shared
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
package se.jguru.shared.jaxb.spi.shared

import javax.xml.bind.annotation.XmlTransient

/**
 * Utility type holding a suite of reusable patterns, constants and algorithms.
 *
 * @author [Lennart Jörelid](mailto:lj@jguru.se), jGuru Europe AB
 */
@XmlTransient
object JaxbPatterns {

    /**
     * The XML namespace used by shared model objects.
     */
    const val NAMESPACE_SHARED = "http://xmlns.jguru.se/xml/ns/shared"

    /**
     * The XML namespace used by shared transport model objects.
     */
    const val NAMESPACE_SHARED_TRANSPORT = "http://xmlns.jguru.se/xml/ns/transport"
}
