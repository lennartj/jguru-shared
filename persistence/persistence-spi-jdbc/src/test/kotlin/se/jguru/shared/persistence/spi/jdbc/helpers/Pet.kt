package se.jguru.shared.persistence.spi.jdbc.helpers

import java.io.Serializable

data class Pet(val id : Int,
               val name : String,
               val type : String) : Serializable