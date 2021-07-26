package se.jguru.shared.restful.spi.jaxrs

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import se.jguru.shared.json.spi.jackson.JacksonAlgorithms
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedHashMap

open class JacksonJsonMapperTest {

    // Shared state
    val chesterfield = Furniture("Chesterfield", "Sofa")
    val tove = Furniture("Tove", "Sofa")

    val jsonType = MediaType.APPLICATION_JSON_TYPE

    val unitUnderTest = JacksonJsonAnyMapper()
    val emptyArray = emptyArray<Annotation>()


    lateinit var serializedChesterfield : String

    @BeforeEach
    fun setupSharedState() {
        serializedChesterfield = JacksonAlgorithms.serialize(chesterfield)
    }

    @Test
    fun validateSerializingObjectToJson() {

        // Assemble
        val emptyMultiValuedMap = MultivaluedHashMap<String, Any>()
        val charset = Charset.defaultCharset()
        val out = ByteArrayOutputStream()

        // Act
        val isWriteable = unitUnderTest.isWriteable(Furniture::class.java, String::class.java, emptyArray, jsonType)
        unitUnderTest.writeTo(chesterfield,
            Furniture::class.java,
            String::class.java,
            emptyArray,
            jsonType,
            emptyMultiValuedMap,
            out)

        out.flush()
        val result = out.toString(charset.name())
        // println("Got: $result")

        // Assert
        assertThat(isWriteable).isTrue
        JSONAssert.assertEquals(serializedChesterfield, result, true)
    }

    @Test
    fun validateDeserializingJsonToObject() {

        // Assemble
        val emptyMultiValuedMap = MultivaluedHashMap<String, String>()
        val charset = Charset.defaultCharset()
        val input = serializedChesterfield.toByteArray(charset).inputStream()

        // Act
        val isReadable = unitUnderTest.isReadable(Furniture::class.java, String::class.java, emptyArray, jsonType)
        val resurrected = unitUnderTest.readFrom(
            Furniture::class.java as Class<Any>,  // A bit weird, but required to coerce Kotlin's type system.
            String::class.java,
            emptyArray,
            jsonType,
            emptyMultiValuedMap,
            input)

        // Assert
        assertThat(isReadable).isTrue
        assertThat(resurrected).isEqualTo(chesterfield)
    }
}