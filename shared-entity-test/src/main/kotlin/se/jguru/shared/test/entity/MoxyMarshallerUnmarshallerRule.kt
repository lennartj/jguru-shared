/*-
 * #%L
 * Nazgul Project: jguru-shared-entity-test
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
package se.jguru.shared.test.entity

import se.jguru.shared.jaxb.spi.eclipselink.MoxyMarshallerAndUnmarshaller

/**
 * jUnit Rule for running JAXB tests using the [MoxyMarshallerAndUnmarshaller] under Kotlin.
 * For most operations, simply use the [delegate] member.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
open class MoxyMarshallerUnmarshallerRule @JvmOverloads constructor(
    marshallerAndUnmarshaller: MoxyMarshallerAndUnmarshaller = MoxyMarshallerAndUnmarshaller()) :
    AbstractMarshallerAndUnmarshallerRule(
        marshallerAndUnmarshaller,
        "org.eclipse.persistence.jaxb.JAXBContextFactory") {

    constructor(jaxbContextPath : String) : this(MoxyMarshallerAndUnmarshaller())
}
