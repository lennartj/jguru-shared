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

    // Shared state
    val unitUnderTest = HasNonStandardIdValidator()

    @Test
    fun validateRouteValidation() {

        // Assemble
        val anInvalidRoute : Route = DummyRoute("route-1")
        val aValidRoute : Route = DummyRoute("NonDefaultRouteID")

        // Act
        val shouldBeInvalid = unitUnderTest.isValid(anInvalidRoute)
        val shouldBeValid = unitUnderTest.isValid(aValidRoute)

        // Assert
        Assert.assertFalse(shouldBeInvalid)
        Assert.assertTrue(shouldBeValid)
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

    override fun supportsSuspension(): Boolean = true

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