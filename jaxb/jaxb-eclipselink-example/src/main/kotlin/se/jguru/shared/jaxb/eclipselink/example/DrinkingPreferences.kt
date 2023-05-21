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

import java.io.Serializable
import jakarta.xml.bind.annotation.XmlAccessType
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlElementWrapper
import jakarta.xml.bind.annotation.XmlRootElement
import jakarta.xml.bind.annotation.XmlType

/**
 * Example compound implementation creating a relation between two classes.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@XmlRootElement(namespace = "http://typical/people")
@XmlType(namespace = Beverage.NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
open class DrinkingPreferences(

    @field:XmlElementWrapper
    @field:XmlElement(nillable = false, required = true, name = "person")
    var people: List<Person> = mutableListOf()) : Serializable, Comparable<DrinkingPreferences> {

    /**
     * JAXB-friendly constructor
     */
    constructor() : this(mutableListOf())

    override fun compareTo(other: DrinkingPreferences): Int {

        var toReturn: Int = this.people.size - other.people.size

        if (toReturn == 0) {

            for (index in 0..(this.people.size - 1)) {
                toReturn = this.people[index].compareTo(other.people[index])

                if (toReturn != 0) {
                    break
                }
            }
        }

        // All Done.
        return toReturn
    }
}
