package se.jguru.shared.service.spi.camel

import org.apache.camel.Consumer
import org.apache.camel.Endpoint
import org.apache.camel.Navigate
import org.apache.camel.Processor
import org.apache.camel.Route
import org.apache.camel.Service
import org.apache.camel.spi.RouteContext
import org.junit.Assert
import org.junit.Test

class RouteValidatorTest {

    @Test
    fun validateRouteValidation() {

        // Assemble
        val aRoute : Route = DummyRoute("route-1")
        val validator = HasNonStandardIdValidator()

        // Act
        val result = validator.isValid(aRoute)

        // Assert
        Assert.assertFalse(result)
    }
}

class DummyRoute(val routeID : String) : Route {

    override fun getGroup(): String {
        TODO("not implemented")
    }

    override fun getConsumer(): Consumer {
        TODO("not implemented") 
    }

    override fun warmUp() {
        TODO("not implemented") 
    }

    override fun getId(): String = routeID

    override fun supportsSuspension(): Boolean {
        TODO("not implemented") 
    }

    override fun getProperties(): MutableMap<String, Any> {
        TODO("not implemented") 
    }

    override fun onStartingServices(p0: MutableList<Service>?) {
        TODO("not implemented") 
    }

    override fun addService(p0: Service?) {
        TODO("not implemented") 
    }

    override fun navigate(): Navigate<Processor> {
        TODO("not implemented") 
    }

    override fun getRouteContext(): RouteContext {
        TODO("not implemented") 
    }

    override fun getServices(): MutableList<Service> {
        TODO("not implemented") 
    }

    override fun getDescription(): String {
        TODO("not implemented") 
    }

    override fun filter(p0: String?): MutableList<Processor> {
        TODO("not implemented") 
    }

    override fun getUptimeMillis(): Long {
        TODO("not implemented") 
    }

    override fun getUptime(): String {
        TODO("not implemented") 
    }

    override fun getEndpoint(): Endpoint {
        TODO("not implemented") 
    }
}