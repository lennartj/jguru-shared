package se.jguru.shared.entity.test.people

import java.io.Serializable
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@XmlRootElement(namespace = Beverage.NAMESPACE)
@XmlType(namespace = Beverage.NAMESPACE)
class DrinkingPreferences(
    @XmlElement(nillable = false, required = true)
    var people: List<Person> = mutableListOf()) : Serializable {

    /**
     * JAXB-friendly constructor
     */
    constructor() : this(mutableListOf())

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