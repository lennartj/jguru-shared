package se.jguru.shared.service.spi.camel.core.features

import org.apache.camel.builder.AdviceWithRouteBuilder
import org.apache.camel.component.mock.MockEndpoint
import org.apache.camel.model.ProcessorDefinition
import org.junit.Assert
import org.junit.Test
import se.jguru.shared.service.spi.camel.core.CamelFileMoverTest
import se.jguru.shared.service.spi.camel.core.ROUTEID

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

    override fun doPostSetup() {

        context.getRouteDefinition(ROUTEID)
            .adviceWith(context, object : AdviceWithRouteBuilder() {
                override fun configure() {
                    weaveAddLast<ProcessorDefinition<*>>().to("mock:messages")
                }
            })
    }

    @Test
    fun validateMockEndpointReceivedAdvicedRouteMessage() {

        // Assemble
        theMockEndpoint.expectedCount = 1

        // Act
        super.validateMovingOtherFiles()

        // Assert
        Assert.assertEquals(1, this.theMockEndpoint.receivedCounter)
        theMockEndpoint.assertIsSatisfied()
    }
}