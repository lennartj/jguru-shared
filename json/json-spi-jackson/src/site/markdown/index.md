# About `jguru-shared-spi-jackson`

The Shared Jackson SPI simplifies converting objects to and from JSON using the 
[Jackson](https://github.com/FasterXML/jackson) framework. 
Using JSON as the transport form between systems may yield more light-weight processing than
a corresponding JAXB/XML format. Finally, Jackson is a mature implementation to work with.

The SPI contains algorithms to simplify the following operations:

1. **Serialization**: Convert an Object to JSON. (Also known as _Marshalling_).
2. **Deserialization**: Convert a JSON structure (back) to an object. (Also known as _Unmarshalling_)
3. **Generating JSON Schema**: Extract the JSON schema for a class.
4. **Building ObjectMappers**: The Entrypoint to the Jackson framework, in a simple-to-use builder pattern.

### Example / typical use

The typical use of the Jackson SPI is illustrated in the `json-jackson-example` project.
However, stereotypical use of the two main operations (serialization and deserialization) are found below:

#### Serializing an object to a JSON string

    // Create a Car object, and serialize it into a JSON formatted String
    val myCar = Car("Volvo", "ABC 123")
    val jsonForm: String = JacksonAlgorithms.serialize(myCar)  
    
#### Deserializing an object from a JSON string

    // Deserialize myCar from a JSON formatted String 
    val deserialized: Car = JacksonAlgorithms.deserialize(jsonForm, Car::class.java)      