package se.jguru.shared.service.spi.camel.core.features

import org.apache.camel.RoutesBuilder
import org.apache.camel.builder.AdviceWithRouteBuilder
import org.apache.camel.component.mock.MockEndpoint
import org.apache.camel.model.ProcessorDefinition
import se.jguru.shared.service.spi.camel.core.CamelFileMoverTest

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class CamelFileMoverAdviceTest : CamelFileMoverTest() {

    // Shared state
    lateinit var theMockEndpoint: MockEndpoint

    override fun setUp() {
        super.setUp()

        theMockEndpoint = getMockEndpoint("mock:messages")
    }

    override fun createRouteBuilder(): RoutesBuilder {

        val toReturn = super.createRouteBuilder()

        context.getRouteDefinition("route")
            .adviceWith(context, object : AdviceWithRouteBuilder() {
                override fun configure() {
                    weaveAddLast<ProcessorDefinition<*>>().to("mock:messages")
                }
            })

        return toReturn
    }
}