package se.jguru.shared.json.spi.jackson.people

import org.assertj.core.api.Assertions.assertThat
import java.io.Serializable

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
data class DrinkingPreferences(
    var people: List<Person> = mutableListOf()) : Serializable, Comparable<DrinkingPreferences> {

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

            val ale = Beverage("Avenyn Ale", "Dugges")
            val stout = Beverage("Idjit", "Dugges")
            val porter = Beverage("Ostronporter", "Göteborgs Nya")

            val lennart = Person("Lennart", 254, ale)
            val malin = Person("Malin", 32, stout)
            val lasse = Person("Lasse", 52, porter)
            val anders = Person("Anders", 42, ale)

            return DrinkingPreferences(listOf(lennart, malin, lasse, anders))
        }
    }
}