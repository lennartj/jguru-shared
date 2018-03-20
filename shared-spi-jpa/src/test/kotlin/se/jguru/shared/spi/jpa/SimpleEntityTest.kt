package se.jguru.shared.spi.jpa

import org.junit.Before
import org.junit.Rule
import se.jguru.shared.entity.test.MoxyMarshallerUnmarshallerRule

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class SimpleEntityTest {

    @Rule
    val jaxbRule = MoxyMarshallerUnmarshallerRule()

    @Before
    fun setupSharedState() {

    }
}