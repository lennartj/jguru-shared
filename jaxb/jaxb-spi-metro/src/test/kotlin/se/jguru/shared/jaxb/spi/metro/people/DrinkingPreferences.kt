package se.jguru.shared.jaxb.spi.metro.people

import org.assertj.core.api.Assertions.assertThat
import java.io.Serializable
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlRootElement
import jakarta.xml.bind.annotation.XmlType

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@XmlRootElement(namespace = Beverage.NAMESPACE)
@XmlType(namespace = Beverage.NAMESPACE)
class DrinkingPreferences(
    @XmlElement(nillable = false, required = true)
    var people: List<Person> = mutableListOf()
) : Serializable, Comparable<DrinkingPreferences> {

    /**
     * JAXB-friendly constructor
     */
    constructor() : this(mutableListOf())

    override fun compareTo(other: DrinkingPreferences): Int {

        val toReturn: Int = this.people.size - other.people.size

        if (toReturn == 0) {
            people.forEachIndexed { index, aPerson -> assertThat(other.people[index]).isEqualTo(aPerson) }
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