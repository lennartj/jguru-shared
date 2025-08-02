package se.jguru.shared.json.jackson.example

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import se.jguru.shared.algorithms.api.resources.PropertyResources
import se.jguru.shared.json.spi.jackson.JacksonAlgorithms

class ExampleCode {

    private val log : Logger = LoggerFactory.getLogger(ExampleCode::class.java)

    @Test
    fun showSerializationUsage() {

        // Create a Car object, and serialize it into a JSON formatted String
        val myCar = Car("Volvo", "ABC 123")
        val jsonForm: String = JacksonAlgorithms.serialize(myCar)

        // Assert
        printActualDataAndAssertEquality(
            "Serialization: Car",
            jsonForm,
            "testdata/carpool/single_car.json")
    }

    @Test
    fun showJsonSchemaUsage() {

        // Create the JSON Schema for the Car class
        val carpoolSchema: String = JacksonAlgorithms.getSchemaAsString(CarPool::class.java)

        // Assert
        printActualDataAndAssertEquality(
            "JSON Schema: CarPool",
            carpoolSchema,
            "testdata/carpool/carpool_json_schema.json")
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

        // Deserialize myCar from a JSON formatted String
        val deserialized: Car = JacksonAlgorithms.deserialize(jsonData, Car::class.java)

        // Ensure that the deserialized Car equals the expected one
        assertThat(deserialized).isEqualTo(expected)
        assertThat(deserialized).isNotSameAs(expected)
    }

    @Test
    fun showReferentialIntegrity() {

        // Assemble test data:
        // 1) Create a CarPool consisting of 3 cars and 2 drivers
        //
        val volvo = Car("Volvo", "ABC 123", "SpongeBob")
        val ford = Car("Ford", "EFG 456")
        val tesla = Car("Tesla", "HIJ 789")

        val mickey = Driver("Mickey", listOf(volvo, tesla))
        val minnie = Driver("Minnie", listOf(tesla))

        val carPool = CarPool(listOf(mickey, minnie), listOf(volvo, ford, tesla))

        // Act
        // 2) Serialize the CarPool into JSON and deserialize it back again
        val jsonCarPool: String = JacksonAlgorithms.serialize(carPool)
        // println("Serialized CarPool object:\n\n$jsonCarPool")

        val deserialized: CarPool = JacksonAlgorithms.deserialize(jsonCarPool, CarPool::class.java)

        // Assert
        // 3) Now ensure that the Deserialized JSON objects adhere to referential integrity
        val deserializedMickey: Driver = deserialized.drivers.find { it.name == "Mickey" }!!
        val deserializedMinnie: Driver = deserialized.drivers.find { it.name == "Minnie" }!!

        val mickeysTesla: Car = deserializedMickey.cars.find { it.name == "Tesla" }!!
        val minniesTesla: Car = deserializedMinnie.cars.find { it.name == "Tesla" }!!

        assertThat(mickeysTesla)
            .isSameAs(minniesTesla)
            .withFailMessage { "Mickey's tesla was not the same as Minnie's tesla. Referential integrity compromised." }

        assertThat(deserialized.cars.find { it.name == "Volvo" }!!.nickname).isEqualTo("SpongeBob")
        assertThat(deserialized.cars.find { it.name == "Tesla" }!!.nickname).isNull()
        assertThat(deserialized.cars.find { it.name == "Ford" }!!.nickname).isNull()
    }

    //
    // Shared functions
    //

    private fun printActualDataAndAssertEquality(title : String, actual : String, resourcePathToExpectedData : String) {

        //
        // Printout the actual data.
        //
        log.info("\n[$title]\n\n$actual\n")

        //
        // Assert that the actual data is identical to the resourcePathToExpectedData
        //
        // a) Read expected JSON data from a file
        // b) Assert that the expected structure matches the jsonForm
        //
        val expected = PropertyResources.readFully(resourcePathToExpectedData)
        JSONAssert.assertEquals(expected, actual, true)
    }
}