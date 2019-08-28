package se.jguru.shared.service.spi.camel

import org.apache.camel.Body
import org.apache.camel.ProducerTemplate
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.cdi.ContextName
import org.apache.camel.cdi.Uri
import org.apache.camel.impl.event.CamelContextStartedEvent
import org.apache.camel.impl.event.CamelContextStoppingEvent
import org.apache.camel.spi.CamelEvent
import javax.enterprise.event.Observes
import javax.inject.Inject
import javax.inject.Named

/**
 * Trivial, basic Camel CDI application for test purposes.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class CamelCdiTestApplication {

    @ContextName("camelCdiTestContext")
    internal class Hello : RouteBuilder() {

        override fun configure() {

            from("direct:message")
                .routeId("someTestRoute")
                .log("\${body} from \${camelContext.name}")

            from("direct:in")
                .routeId("mainProcessingRoute")
                .bean("directBodyProcessor")
                .to("direct:out")
        }
    }

    @Inject
    @Uri("direct:message")
    var producer: ProducerTemplate? = null

    fun hello(@Observes event: CamelContextStartedEvent) {
        producer?.sendBody("Hello")
    }

    fun bye(@Observes event: CamelContextStoppingEvent) {
        producer?.sendBody("Bye")
    }

    @Named("directBodyProcessor")
    class Bean {

        fun process(@Body body: String): String {
            return body
        }
    }
}