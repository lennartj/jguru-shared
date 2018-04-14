package se.jguru.shared.spi.jackson

import org.junit.Assert
import org.junit.Test
import se.jguru.shared.spi.jackson.tutorial.Car
import se.jguru.shared.spi.jackson.tutorial.CarPool
import se.jguru.shared.spi.jackson.tutorial.Driver

class TutorialTest {

    @Test
    fun showSerializationUsage() {

        // Create a Car object
        val myCar = Car("Volvo", "ABC 123")

        // Serialize myCar to a JSON formatted String
        val jsonForm: String = JacksonAlgorithms.serialize(myCar)

        // Printout the JSON string.
        println(jsonForm)
    }

    @Test
    fun showJsonSchemaUsage() {

        // Create the JSON Schema for the Car class
        val schemaAsString: String = JacksonAlgorithms.getSchemaAsString(Car::class.java)

        // Printout the JSON schema.
        println(schemaAsString)
    }

    @Test
    fun showDeserializationUsage() {

        // Create a Car object
        val expected = Car("Volvo", "ABC 123")
        val jsonData = "{\n" +
            "  \"@id\" : 1,\n" +
            "  \"name\" : \"Volvo\",\n" +
            "  \"registrationPlate\" : \"ABC 123\"\n" +
            "}"

        // Serialize myCar to a JSON formatted String
        val deserialized: Car = JacksonAlgorithms.deserialize(jsonData, Car::class.java)

        // Ensure that the deserialized Car equals the expected one
        Assert.assertEquals(expected, deserialized)
    }

    @Test
    fun showReferentialIntegrity() {

        // Create a CarPool consisting of 3 cars and 2 drivers
        val volvo = Car("Volvo", "ABC 123")
        val ford = Car("Ford", "EFG 456")
        val tesla = Car("Tesla", "HIJ 789")

        val mickey = Driver("Mickey", listOf(volvo, tesla))
        val minnie = Driver("Minnie", listOf(tesla))

        val carPool = CarPool(listOf(mickey, minnie), listOf(volvo, ford, tesla))

        // Serialize the CarPool into JSON and deserialize it back again
        val jsonCarPool : String = JacksonAlgorithms.serialize(carPool)
        val deserialized : CarPool = JacksonAlgorithms.deserialize(jsonCarPool, CarPool::class.java)

        // Now ensure that
        println(jsonCarPool)

        val deserializedMickey : Driver = deserialized.drivers.find { it.name == "Mickey" }!!
        val deserializedMinnie : Driver = deserialized.drivers.find { it.name == "Minnie" }!!

        val mickeysTesla : Car = deserializedMickey.cars.find { it.name == "Tesla" }!!
        val minniesTesla : Car = deserializedMinnie.cars.find { it.name == "Tesla" }!!

        Assert.assertSame(mickeysTesla, minniesTesla)
    }
}