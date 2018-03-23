package se.jguru.shared.spi.jpa.people

import se.jguru.shared.spi.jpa.SimpleEntity
import se.jguru.shared.spi.jpa.StandardEntity
import java.io.Serializable
import javax.persistence.Basic
import javax.persistence.Column
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

/**
 * A simple model of a Person
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@StandardEntity
@XmlType(namespace = "http://foo/bar", propOrder = ["name", "age", "beverage"])
@XmlAccessorType(XmlAccessType.FIELD)
class Person @JvmOverloads constructor(

    @field:Basic(optional = false)
    @field:Column(nullable = false)
    @XmlElement(nillable = false, required = true)
    var name: String,

    @field:Basic(optional = false)
    @field:Column(nullable = false)
    @XmlAttribute(required = true)
    var age: Int,

    @field:Basic(optional = false)
    @field:Column(nullable = false)
    @XmlElement(required = true)
    var beverage: Beverage,

    id : Long = 0,
    version : Long = 0) : SimpleEntity(), Serializable, Comparable<Person> {

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
        val that = other as Person
        return this.name == that.name && this.age == that.age
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + age
        result = 31 * result + beverage.hashCode()
        return result
    }

    override fun validateInternalState() {

    }
}
