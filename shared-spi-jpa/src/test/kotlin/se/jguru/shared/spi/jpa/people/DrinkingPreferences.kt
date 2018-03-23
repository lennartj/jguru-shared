package se.jguru.shared.spi.jpa.people

import se.jguru.shared.spi.jpa.StandardEntity
import java.io.Serializable
import javax.persistence.Basic
import javax.persistence.Column
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@StandardEntity
@XmlRootElement(namespace = Beverage.NAMESPACE)
@XmlType(namespace = Beverage.NAMESPACE)
class DrinkingPreferences(


    @field:Basic(optional = false)
    @field:Column(nullable = false)
    @field:XmlElement(nillable = false, required = true)
    var people: List<Person> = mutableListOf()) : Serializable, Comparable<DrinkingPreferences> {

    /**
     * JAXB-friendly constructor
     */
    constructor() : this(mutableListOf())

    override fun compareTo(that: DrinkingPreferences): Int {

        var toReturn : Int = this.people.size - that.people.size

        if(toReturn == 0) {

        }

        // All Done.
        return toReturn
    }

    companion object {

        fun createPrefs(): DrinkingPreferences {

            val ale = Beverage("Avenyn Ale")
            val stout = Beverage("Idjit")
            val porter = Beverage("Ostronporter")

            val lennart = Person("Lennart", 254, ale)
            val malin = Person("Malin", 32, stout)
            val lasse = Person("Lasse", 52, porter)
            val anders = Person("Anders", 42, ale)

            return DrinkingPreferences(listOf(lennart, malin, lasse, anders))
        }
    }
}