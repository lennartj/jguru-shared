package se.jguru.shared.spi.jackson.people

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import java.io.Serializable

/**
 * A simple model of a Person
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
// @JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator::class, property = "@id")
@JsonPropertyOrder(value = ["name", "age", "beverage"])
data class Person(

    var name: String,

    var age: Int,

    var beverage: Beverage) : Serializable, Comparable<Person> {


    override fun compareTo(other: Person): Int {

        var toReturn: Int = this.name.compareTo(other.name)

        if (toReturn == 0) {
            toReturn = this.age - other.age
        }

        if (toReturn == 0) {
            toReturn = this.beverage.compareTo(other.beverage)
        }

        // All Done.
        return toReturn
    }

    override fun equals(other: Any?): Boolean {

        // Check sanity
        if (other == null || other !is Person) {
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
