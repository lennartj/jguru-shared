package se.jguru.shared.persistence.spi.jdbc.helpers

import java.io.Serializable

class Person @JvmOverloads constructor(
    val id : Int,
    val firstName : String,
    val lastName : String,
    val pets : MutableSet<Pet> = mutableSetOf()) : Serializable, Comparable<Person> {

    override fun toString(): String {
        val petDesc = when(pets.isNullOrEmpty()) {
            true -> "<no pets>"
            else -> pets.map { "${it.type}: ${it.name}" }.sorted().reduce { acc, s -> "$acc, $s" }
        }
        return "Person [$id, firstName: $firstName, lastName: $lastName, pets: $petDesc]"
    }

    override fun compareTo(other: Person): Int = id - other.id
}