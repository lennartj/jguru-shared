/*-
 * #%L
 * Nazgul Project: jguru-shared-json-spi-jackson
 * %%
 * Copyright (C) 2018 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * 
 *       http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package se.jguru.shared.json.spi.jackson

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.TypeFactory
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator
import java.io.InputStream
import java.io.OutputStream


object JacksonAlgorithms {

    /**
     * Converts the JSON-formatted String into an Object of the type given.
     * In Jackson terminology, this process is called "Deserialization".
     *
     * @param json The JSON-formatted String to convert/resurrect back into an object.
     * @param expected The type of object expected after de-serialization is done.
     * @param objectMapper The ObjectMapper used to de-serialize the JSON string.
     * @return The deserialized/resurrected object.
     */
    @JvmStatic
    @JvmOverloads
    fun <T> deserialize(json: String,
                        expected: Class<T>,
                        objectMapper: ObjectMapper = ObjectMapperBuilder.getDefault()): T {

        // Retrieve the standard reader
        val objectReader = objectMapper.readerFor(expected)

        // All Done.
        return objectReader.readValue(json)
    }

    /**
     * Converts the JSON-formatted String into a java Map of the types given.
     * In Jackson terminology, this process is called "Deserialization".
     *
     * @param json The JSON-formatted String to convert/resurrect back into a Map.
     * @param mapType The type of Map retrieved by deserialization.
     * @param keyType The type of Key held within the Map.
     * @param valueType The type of Value held within the Map.
     * @param objectMapper The ObjectMapper used to de-serialize the JSON string.
     * @return The deserialized/resurrected object.
     */
    @JvmStatic
    @JvmOverloads
    @Suppress("UNCHECKED_CAST")
    fun <K, V> deserializeMap(
        json: String,
        keyType: Class<K>,
        valueType: Class<V>,
        mapType: Class<MutableMap<K, V>> = HashMap::class.java as Class<MutableMap<K, V>>,
        objectMapper: ObjectMapper = ObjectMapperBuilder.getDefault()): MutableMap<K, V> {

        // Construct the MapType
        val jsonMapType = TypeFactory.defaultInstance().constructMapType(mapType, keyType, valueType)

        // All Done.
        return objectMapper.readValue(json, jsonMapType) as MutableMap<K, V>
    }

    /**
     * Serializes the supplied object into a JSON-formatted String.
     * In Jackson terminology, this is called "Serialization".
     *
     * @param anObject The object to serialize into a JSON-formatted String.
     * @param objectMapper The ObjectMapper used to serialize the object.
     * @param compactOutput `true` to indicate that the output should be compact, and false to
     * use pretty printed JSON which should be human-readable.
     * @return The JSON-formatted string, serialized from the supplied Object.
     */
    @JvmStatic
    @JvmOverloads
    fun serialize(anObject: Any,
                  objectMapper: ObjectMapper = ObjectMapperBuilder.getDefault(),
                  compactOutput: Boolean = false): String {

        // Fetch the relevant printer
        val objectWriter = when (compactOutput) {
            false -> objectMapper.writerWithDefaultPrettyPrinter()
            true -> objectMapper.writer()
        }

        // All Done.
        return objectWriter.writeValueAsString(anObject)
    }

    /**
     * Converts the JSON-formatted Stream into an Object of the type given.
     * In Jackson terminology, this process is called "Deserialization".
     *
     * @param jsonStream A stream holding JSON-formatted data to convert/resurrect back into an object.
     * @param expected The type of object expected after de-serialization is done.
     * @param objectMapper The ObjectMapper used to de-serialize the JSON string.
     * @return The deserialized/resurrected object.
     */
    @JvmStatic
    @JvmOverloads
    fun <T> deserializeFromStream(jsonStream: InputStream,
                                  expected: Class<T>,
                                  objectMapper: ObjectMapper = ObjectMapperBuilder.getDefault()): T {

        // Retrieve the standard reader
        val objectReader = objectMapper.readerFor(expected)

        // All Done.
        return objectReader.readValue(jsonStream)
    }

    /**
     * "Serializes" (in Jackson terminology) the supplied object onto a given OutputStream.
     *
     * @param anObject The object to serialize into a JSON-formatted String.
     * @param targetStream The stream onto which the result should be written.
     * @param objectMapper The ObjectMapper used to serialize the object.
     * @param compactOutput `true` to indicate that the output should be compact, and false to
     * use pretty printed JSON which should be human-readable.
     */
    @JvmStatic
    @JvmOverloads
    fun <T> serializeToStream(anObject: T,
                              targetStream: OutputStream,
                              objectMapper: ObjectMapper = ObjectMapperBuilder.getDefault(),
                              compactOutput: Boolean = false) {

        // Fetch the relevant printer
        val objectWriter = when (compactOutput) {
            false -> objectMapper.writerWithDefaultPrettyPrinter()
            true -> objectMapper.writer()
        }

        // All Done.
        return objectWriter.writeValue(targetStream, anObject)
    }

    /**
     * Synthesizes a JSON Schema for the supplied class.
     *
     * @param aClass The class for which to synthesize a JSON schema
     * @param objectMapper The ObjectMapper to perform the synthesis.
     * @param title An optional title property for the schema generated.
     * @param description An optional description property for the schema generated.
     *
     * @return The JsonNode containing the JSON Schema.
     */
    @JvmStatic
    @JvmOverloads
    fun <T> getSchema(aClass: Class<T>,
                      objectMapper: ObjectMapper = ObjectMapperBuilder.getDefault(),
                      title: String? = null,
                      description: String? = null): JsonNode {

        // Wrap the ObjectMapper
        val jsonSchemaGenerator = JsonSchemaGenerator(objectMapper)

        // All Done.
        return jsonSchemaGenerator.generateJsonSchema(aClass, title, description)
    }

    /**
     * Synthesizes a JSON Schema for the supplied class, retrieving the result as a String.
     *
     * @param aClass The class for which to synthesize a JSON schema
     * @param objectMapper The ObjectMapper to perform the synthesis.
     * @param title An optional title property for the schema generated.
     * @param description An optional description property for the schema generated.
     *
     * @return The JSON Schema string
     */
    @JvmStatic
    @JvmOverloads
    fun <T> getSchemaAsString(aClass: Class<T>,
                              objectMapper: ObjectMapper = ObjectMapperBuilder.getDefault(),
                              title: String? = null,
                              description: String? = null): String {

        return objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(getSchema(aClass, objectMapper, title, description))
    }
}
