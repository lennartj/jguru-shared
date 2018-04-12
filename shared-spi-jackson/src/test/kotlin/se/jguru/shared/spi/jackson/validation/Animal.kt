package se.jguru.shared.spi.jackson.validation

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import se.jguru.shared.algorithms.api.Validate
import java.io.Serializable

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@JsonPropertyOrder(value = ["name", "age"])
data class Animal(

    val name: String,

    @JsonProperty("animalAge")
    val age: Int) : Serializable {

    init {
        Validate.isTrue(age >= 0, "Cannot handle negative age. (Got: $age)")
    }
}