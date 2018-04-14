package se.jguru.shared.spi.jackson.tutorial

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.annotation.ObjectIdGenerators

@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator::class, property = "@id")
@JsonPropertyOrder(value = ["drivers", "cars"])
data class CarPool(val drivers : List<Driver>, val cars : List<Car>)