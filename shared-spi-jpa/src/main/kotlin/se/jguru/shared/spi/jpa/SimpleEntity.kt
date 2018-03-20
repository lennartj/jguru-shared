/*-
 * #%L
 * Nazgul Project: jguru-shared-spi-jpa
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
package se.jguru.shared.spi.jpa

import se.jguru.nazgul.tools.validation.api.Validatable
import se.jguru.shared.spi.jaxb.JaxbPatterns
import java.io.Serializable
import javax.annotation.PostConstruct
import javax.persistence.Id
import javax.persistence.PrePersist
import javax.persistence.Version
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlType

/**
 * Abstract superclass for simple entity (domain class) implementations.
 * Every SimpleEntity can be converted to SQL through JPA and to XML/JSON through JAXB.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@StandardAbstractEntity
@XmlType(namespace = JaxbPatterns.NAMESPACE_SHARED, propOrder = ["id", "version"])
abstract class SimpleEntity @JvmOverloads constructor(

    /**
     * The synthetic primary key of [SimpleEntity] objects is given as a [Long].
     */
    @field:Id
    @field:XmlAttribute
    var id : Long = 0,

    /**
     * The JPA version is provided as a [Long], rather than a timestamp.
     */
    @field:Version
    @field:XmlAttribute
    var version : Long = 0) : Serializable, Validatable {

    /**
     * Override this method to provide another validation to be invoked after loading this [SimpleEntity] from
     * persistent state (typically, the database). The default implementation invokes the [validateInternalState]
     * method.
     *
     * @see validateInternalState
     */
    @PostConstruct
    protected open fun validateInternalStateAfterConstruction() = validateInternalState()

    /**
     * Override this method to provide custom validation logic to be invoked before persisting this [SimpleEntity] to
     * database/persistent state. The default implementation invokes the [validateInternalState] method.
     *
     * @see validateInternalState
     */
    @PrePersist
    protected open fun validateInternalStateBeforePersisting() = validateInternalState()

    /**
     * Standard JAXB listener method, automagically invoked immediately before this object is Marshalled.
     *
     * @param marshaller The active Marshaller.
     */
    // private fun beforeMarshal(marshaller: Marshaller)
}
