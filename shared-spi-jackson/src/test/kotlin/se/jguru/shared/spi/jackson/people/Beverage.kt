package se.jguru.shared.spi.jackson.people

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.annotation.ObjectIdGenerators

/**
 * A simple model of a Beverage
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator::class, property = "@id")
@JsonPropertyOrder(value = ["name", "brand"])
data class Beverage(var name: String, var brand: String) : Comparable<Beverage> {

    override fun compareTo(other: Beverage): Int {

        val toReturn = this.name.compareTo(other.name)

        return when (toReturn) {
            0 -> this.brand.compareTo(other.brand)
            else -> toReturn
        }
    }

    /**
     * Unnecessary override, but illustrates overriding the data class default implementation.
     */
    override fun toString(): String {
        return "Beverage [$name, brand: $brand]"
    }
}