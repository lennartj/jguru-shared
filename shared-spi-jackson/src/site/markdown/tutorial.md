# Tutorial: Creating Entities and JSON conversion

The simplest way to get acquainted with the Jackson SPI is showing the process to 
Serialize and Deserialize an entity class into JSON. 

### Step 1: Serializing a simple entity

An example of a really simple entity class _Car_ is shown below. Despite only being 
a single row, the _Car_ class bytecode will contain setters, getters, toString and 
hashcode methods.

    data class Car(val name : String, val registrationPlate : String)

Using the `JacksonAlgorithms` utility, a _Car_ instance can be serialized in a 
simple manner:

    // Create a Car object
    val myCar = Car("Volvo", "ABC 123")

    // Serialize myCar to a JSON formatted String
    val jsonForm : String = JacksonAlgorithms.serialize(myCar)

The content of the `jsonForm` string is as can be expected:  

    {
      "name" : "Volvo",
      "registrationPlate" : "ABC 123"
    }
    
### Step 2: Adding ID and JSON Schema capability      

While it is technically possible to use the _Car_ class as illustrated above, we normally
add two more annotations to ensure that _Car_ objects can be properly deserialized from JSON
to objects. These annotations are:

1. **JsonIdentityInfo**: Supplies an automatic `@id` attribute for each _Car_ in JSON form, to 
   guarantee referential integrity/identity when deserializing JSON strings into Objects. 
   (See the section _Referential integrity when deserializing complex objects_ for an example of this).
2. **JsonPropertyOrder**: Defines the order of the serialized properties, to ensure that a 
   well-formed [JSON Schema](http://json-schema.org) can be synthesized from the _Car_ class.

Adding the two annotations to our example _Car_ class yields: 

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator::class, property = "@id")
    @JsonPropertyOrder(value = ["name", "registrationPlate"])
    data class Car(val name : String, val registrationPlate : String) 
  
Using the same `JacksonAlgorithms` utility, the _Car_ instance is serialized in identical manner:

    // Create a Car object
    val myCar = Car("Volvo", "ABC 123")

    // Serialize myCar to a JSON formatted String
    val jsonForm : String = JacksonAlgorithms.serialize(myCar)

However, the content of the `jsonForm` string now has an added `@id` field, due to the `@JsonIdentityInfo` annotation:  

    {
      "@id" : 1,
      "name" : "Volvo",
      "registrationPlate" : "ABC 123"
    }
    
Since we also added the `@JsonPropertyOrder` annotation, we can use the `JacksonAlgorithms` to retrieve
the JSON Schema:

    // Create the JSON Schema for the Car class
    val schemaAsString : String = JacksonAlgorithms.getSchemaAsString(Car::class.java)    

The resulting schema is:

    {
      "$schema" : "http://json-schema.org/draft-04/schema#",
      "title" : "Car",
      "type" : "object",
      "additionalProperties" : false,
      "properties" : {
        "name" : {
          "type" : "string"
        },
        "registrationPlate" : {
          "type" : "string"
        }
      },
      "required" : [ "name", "registrationPlate" ]
    }
    
### Step 3: Deserializing a simple entity

Deserializing a JSON string into a _Car_ object is as simple as serializing, although
we need to use another method within the `JacksonAlgorithms`. Given the following JSON: 

    {
      "@id" : 1,
      "name" : "Volvo",
      "registrationPlate" : "ABC 123"
    }
    
... a _Car_ object is deserialized and compared to the JSON data as per above:

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

### Step 4: Referential integrity when deserializing complex objects

To illustrate referential integrity and the need for the 
`@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator::class, property = "@id")` annotation, let us 
implement a 3-class model, as shown below.

![Carpool Classes](./images/plantuml/carpool_diagram.png "Carpool Classes")

Let us first meet the classes. Car:

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator::class, property = "@id")
    @JsonPropertyOrder(value = ["name", "registrationPlate"])
    data class Car(val name : String, val registrationPlate : String)
    
Driver:

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator::class, property = "@id")
    @JsonPropertyOrder(value = ["name", "cars"])
    data class Driver(val name : String, val cars : List<Car>)
    
CarPool:

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator::class, property = "@id")
    @JsonPropertyOrder(value = ["drivers", "cars"])
    data class CarPool(val drivers : List<Driver>, val cars : List<Car>)

Let us now create some objects, namely a CarPool with 3 cars and 2 drivers.
In this case, both drivers (_mickey_ and _minnie_) prefers to drive the _tesla_ car, 
and _mickey_ also fancies the _volvo_.  

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
  
Serializing the carPool object yields the structure below. Note that the data of each car
is only given in 1 place. For example, the values of the _tesla_ car with `@id` value `4` is only 
supplied within the array of the _mickey_ driver. Since the _minnie_ driver prefers the same
car, an _id reference_ with the value `4` is rendered within _minnie_'s cars array.

The same principle holds for the _cars_ array within the carpool root element. Only the _ ford_ car
with `@id` value `6` is given there, since it has not occurred within the JSON structure before.   

    {
      "@id" : 1,
      "drivers" : [ {
        "@id" : 2,
        "name" : "Mickey",
        "cars" : [ {
          "@id" : 3,
          "name" : "Volvo",
          "registrationPlate" : "ABC 123"
        }, {
          "@id" : 4,
          "name" : "Tesla",
          "registrationPlate" : "HIJ 789"
        } ]
      }, {
        "@id" : 5,
        "name" : "Minnie",
        "cars" : [ 4 ]
      } ],
      "cars" : [ 3, {
        "@id" : 6,
        "name" : "Ford",
        "registrationPlate" : "EFG 456"
      }, 4 ]
    }
    
After deserializing the CarPool object, Jackson preserves the references which means that
both drivers reference _the same_ tesla car - not two different copies:

    val deserializedMickey : Driver = deserialized.drivers.find { it.name == "Mickey" }!!
    val deserializedMinnie : Driver = deserialized.drivers.find { it.name == "Minnie" }!!

    val mickeysTesla : Car = deserializedMickey.cars.find { it.name == "Tesla" }!!
    val minniesTesla : Car = deserializedMinnie.cars.find { it.name == "Tesla" }!!

    Assert.assertSame(mickeysTesla, minniesTesla)

          