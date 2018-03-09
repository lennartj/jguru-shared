package se.jguru.shared.entity.test.people

import java.io.Serializable
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlIDREF
import javax.xml.bind.annotation.XmlType

/**
 * A simple model of a Person
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@XmlType(namespace = "http://foo/bar", propOrder = ["name", "age", "beverage"])
@XmlAccessorType(XmlAccessType.FIELD)
class Person(
    @XmlElement(nillable = false, required = true)
    var name: String,

    @XmlAttribute(required = true)
    var age: Int,

    @XmlElement(required = true)
    var beverage: Beverage) : Serializable {

    constructor() : this("none", -1, Beverage("none"))

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
}
