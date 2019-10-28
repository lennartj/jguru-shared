package se.jguru.shared.restful.spi.jaxrs

import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonPropertyOrder(value = ["name", "type"])
data class Furniture(val name : String, val type : String)