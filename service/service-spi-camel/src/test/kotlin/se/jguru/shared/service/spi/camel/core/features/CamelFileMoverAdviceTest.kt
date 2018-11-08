package se.jguru.shared.service.spi.camel.core.features

import org.apache.camel.builder.AdviceWithRouteBuilder
import org.apache.camel.component.mock.MockEndpoint
import org.apache.camel.model.ProcessorDefinition
import org.junit.After
import org.junit.Assert
import org.junit.Test
import se.jguru.shared.service.spi.camel.core.CamelFileMoverTest
import se.jguru.shared.service.spi.camel.core.ROUTEID

private const val mockEndpointUri : String = "mock:messages"

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class CamelFileMoverAdviceTest : CamelFileMoverTest() {

    // Shared state
    lateinit var theMockEndpoint: MockEndpoint

    override fun isUseAdviceWith(): Boolean = true

    override fun setUp() {

        // Use the MOXy JAXBContext
        // This is required for Java 11 compliance and compilability.
        System.setProperty("javax.xml.bind.context.factory", "org.eclipse.persistence.jaxb.JAXBContextFactory")

        // Delegate & inject
        super.setUp()
        
        theMockEndpoint = getMockEndpoint(mockEndpointUri)
    }

    override fun doPostSetup() {

        val adviceRouteBuilder = object : AdviceWithRouteBuilder() {
            override fun configure() {
                log.info("====> Weaving addLast <====")
                weaveAddLast<ProcessorDefinition<*>>().to(mockEndpointUri)
            }
        }

        // Advice the existing route
        val theRouteToAdvice = context.getRouteDefinition(ROUTEID)
        theRouteToAdvice.adviceWith(context, adviceRouteBuilder)

        // Launch
        context.start()
    }

    @After
    fun stopContext() {
        context.stop()
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