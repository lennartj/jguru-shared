package se.jguru.shared.service.spi.camel.core.features

import org.apache.camel.builder.AdviceWithRouteBuilder
import org.apache.camel.cdi.Uri
import org.apache.camel.component.mock.MockEndpoint
import org.apache.camel.component.mock.MockEndpoint.assertIsSatisfied
import org.apache.camel.management.event.CamelContextStartingEvent
import org.apache.camel.model.ModelCamelContext
import org.apache.camel.model.ProcessorDefinition
import org.junit.ClassRule
import org.junit.Test
import org.junit.rules.Verifier
import java.util.concurrent.TimeUnit
import javax.enterprise.event.Observes

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
// @RunWith(CamelCdiRunner::class)
class CamelAdviceTest {

    @Throws(Exception::class)
    fun advice(@Observes event: CamelContextStartingEvent,
               @Uri("mock:messages") messages: MockEndpoint,
               context: ModelCamelContext) {
        messages.expectedMessageCount(2)
        messages.expectedBodiesReceived("Hello", "Bye")

        verifier().messages = messages

        context.getRouteDefinition("route")
            .adviceWith(context, object : AdviceWithRouteBuilder() {
                override fun configure() {
                    weaveAddLast<ProcessorDefinition<*>>().to("mock:messages")
                }
            })
    }

    @Test
    fun test() {
    }

    class MessageVerifier : Verifier() {

        internal var messages: MockEndpoint? = null

        @Throws(InterruptedException::class)
        protected override fun verify() {
            assertIsSatisfied(2L, TimeUnit.SECONDS, messages)
        }
    }

    companion object {

        @ClassRule
        @JvmStatic
        fun verifier() = MessageVerifier()
    }
}