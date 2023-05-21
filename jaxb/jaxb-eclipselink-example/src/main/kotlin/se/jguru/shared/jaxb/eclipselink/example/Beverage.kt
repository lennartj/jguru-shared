/*-
 * #%L
 * Nazgul Project: jguru-shared-jaxb-eclipselink-example
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
package se.jguru.shared.jaxb.eclipselink.example

import jakarta.xml.bind.annotation.XmlAccessType
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlType

/**
 * A simple model of a Beverage
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@XmlType(namespace = Beverage.NAMESPACE, propOrder = ["name"])
@XmlAccessorType(XmlAccessType.FIELD)
open class Beverage(var name: String) : Comparable<Beverage> {

    /**
     * JAXB-friendly constructor
     */
    constructor() : this("unknown")

    override fun compareTo(other: Beverage): Int {
        return this.name.compareTo(other.name)
    }

    companion object {

        const val NAMESPACE = "http://some/good/beverage"
    }
}
