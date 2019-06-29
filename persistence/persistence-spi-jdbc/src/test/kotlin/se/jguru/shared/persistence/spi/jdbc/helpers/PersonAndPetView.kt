package se.jguru.shared.persistence.spi.jdbc.helpers

data class PersonAndPetView(
    val personId: Int,
    val firstName: String,
    val lastName: String,
    val petId: Int,
    val petName: String,
    val petType: String)