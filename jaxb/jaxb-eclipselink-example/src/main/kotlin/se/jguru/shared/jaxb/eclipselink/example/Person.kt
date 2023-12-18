/*-
 * #%L
 * Nazgul Project: jguru-shared-jaxb-eclipselink-example
 * %%
 * Copyright (C) 2018 - 2023 jGuru Europe AB
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
import jakarta.xml.bind.annotation.XmlAttribute
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlType

/**
 * A simple model of a Person
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@XmlType(namespace = "http://typical/people", propOrder = ["name", "age", "beverage"])
@XmlAccessorType(XmlAccessType.FIELD)
open class Person(

    @field:XmlElement(nillable = false, required = true)
    var name: String,

    @field:XmlAttribute(required = true)
    var age: Int,

    @field:XmlElement(required = true, namespace = Beverage.NAMESPACE)
    var beverage: Beverage) : Serializable, Comparable<Person> {

    constructor() : this("none", -1, Beverage("none"))

    override fun compareTo(other: Person): Int {

        var toReturn : Int = this.name.compareTo(other.name)

        if(toReturn == 0) {
            toReturn = this.age - other.age
        }

        if(toReturn == 0) {
            toReturn = this.beverage.compareTo(other.beverage)
        }

        // All Done.
        return toReturn
    }

    override fun equals(other: Any?): Boolean {

        // Check sanity
        if(other == null || other !is Person) {
            return false
        }

        // Delegate to internal state
        val that = other
        return this.name == that.name && this.age == that.age
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + age
        result = 31 * result + beverage.hashCode()
        return result
    }
}
