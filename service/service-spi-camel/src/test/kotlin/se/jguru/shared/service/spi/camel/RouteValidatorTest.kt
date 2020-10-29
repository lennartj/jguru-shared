package se.jguru.shared.service.spi.camel

import org.apache.camel.CamelContext
import org.apache.camel.Consumer
import org.apache.camel.Endpoint
import org.apache.camel.ErrorHandlerFactory
import org.apache.camel.NamedNode
import org.apache.camel.Navigate
import org.apache.camel.Processor
import org.apache.camel.Route
import org.apache.camel.Service
import org.apache.camel.ShutdownRoute
import org.apache.camel.ShutdownRunningTask
import org.apache.camel.spi.InterceptStrategy
import org.apache.camel.spi.ManagementInterceptStrategy
import org.apache.camel.spi.RouteController
import org.apache.camel.spi.RouteError
import org.apache.camel.spi.RoutePolicy
import org.junit.Assert
import org.junit.Test

class RouteValidatorTest {

    // Shared state
    val unitUnderTest = HasNonStandardIdValidator()

    @Test
    fun validateRouteValidation() {

        // Assemble
        val anInvalidRoute: Route = DummyRoute("route-1")
        val aValidRoute: Route = DummyRoute("NonDefaultRouteID")

        // Act
        val shouldBeInvalid = unitUnderTest.isValid(anInvalidRoute)
        val shouldBeValid = unitUnderTest.isValid(aValidRoute)

        // Assert
        Assert.assertFalse(shouldBeInvalid)
        Assert.assertTrue(shouldBeValid)
    }
}

class DummyRoute(val routeID: String) : Route {

    override fun getId(): String = routeID

    override fun setStreamCaching(cache: Boolean?) {
        TODO("Not yet implemented")
    }

    override fun isStreamCaching(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setTracing(tracing: Boolean?) {
        TODO("Not yet implemented")
    }

    override fun isTracing(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getTracingPattern(): String {
        TODO("Not yet implemented")
    }

    override fun setTracingPattern(tracePattern: String?) {
        TODO("Not yet implemented")
    }

    override fun setBacklogTracing(backlogTrace: Boolean?) {
        TODO("Not yet implemented")
    }

    override fun isBacklogTracing(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setDebugging(debugging: Boolean?) {
        TODO("Not yet implemented")
    }

    override fun isDebugging(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setMessageHistory(messageHistory: Boolean?) {
        TODO("Not yet implemented")
    }

    override fun isMessageHistory(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setLogMask(logMask: Boolean?) {
        TODO("Not yet implemented")
    }

    override fun isLogMask(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setLogExhaustedMessageBody(logExhaustedMessageBody: Boolean?) {
        TODO("Not yet implemented")
    }

    override fun isLogExhaustedMessageBody(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setDelayer(delay: Long?) {
        TODO("Not yet implemented")
    }

    override fun getDelayer(): Long {
        TODO("Not yet implemented")
    }

    override fun setAutoStartup(autoStartup: Boolean?) {
        TODO("Not yet implemented")
    }

    override fun isAutoStartup(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setShutdownRoute(shutdownRoute: ShutdownRoute?) {
        TODO("Not yet implemented")
    }

    override fun getShutdownRoute(): ShutdownRoute {
        TODO("Not yet implemented")
    }

    override fun setShutdownRunningTask(shutdownRunningTask: ShutdownRunningTask?) {
        TODO("Not yet implemented")
    }

    override fun getShutdownRunningTask(): ShutdownRunningTask {
        TODO("Not yet implemented")
    }

    override fun setAllowUseOriginalMessage(allowUseOriginalMessage: Boolean?) {
        TODO("Not yet implemented")
    }

    override fun isAllowUseOriginalMessage(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isCaseInsensitiveHeaders(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setCaseInsensitiveHeaders(caseInsensitiveHeaders: Boolean?) {
        TODO("Not yet implemented")
    }

    override fun getGroup(): String {
        TODO("Not yet implemented")
    }

    override fun getUptime(): String {
        TODO("Not yet implemented")
    }

    override fun getUptimeMillis(): Long {
        TODO("Not yet implemented")
    }

    override fun getConsumer(): Consumer {
        TODO("Not yet implemented")
    }

    override fun getProcessor(): Processor {
        TODO("Not yet implemented")
    }

    override fun supportsSuspension(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getProperties(): MutableMap<String, Any> {
        TODO("Not yet implemented")
    }

    override fun getDescription(): String {
        TODO("Not yet implemented")
    }

    override fun getCamelContext(): CamelContext {
        TODO("Not yet implemented")
    }

    override fun getEndpoint(): Endpoint {
        TODO("Not yet implemented")
    }

    override fun onStartingServices(services: MutableList<Service>?) {
        TODO("Not yet implemented")
    }

    override fun getServices(): MutableList<Service> {
        TODO("Not yet implemented")
    }

    override fun addService(service: Service?) {
        TODO("Not yet implemented")
    }

    override fun navigate(): Navigate<Processor> {
        TODO("Not yet implemented")
    }

    override fun filter(pattern: String?): MutableList<Processor> {
        TODO("Not yet implemented")
    }

    override fun warmUp() {
        TODO("Not yet implemented")
    }

    override fun getLastError(): RouteError {
        TODO("Not yet implemented")
    }

    override fun setLastError(error: RouteError?) {
        TODO("Not yet implemented")
    }

    override fun getStartupOrder(): Int {
        TODO("Not yet implemented")
    }

    override fun setStartupOrder(startupOrder: Int?) {
        TODO("Not yet implemented")
    }

    override fun getRouteController(): RouteController {
        TODO("Not yet implemented")
    }

    override fun setRouteController(controller: RouteController?) {
        TODO("Not yet implemented")
    }

    override fun getRouteId(): String {
        TODO("Not yet implemented")
    }

    override fun getRouteDescription(): String {
        TODO("Not yet implemented")
    }

    override fun getRoute(): NamedNode {
        TODO("Not yet implemented")
    }

    override fun getEventDrivenProcessors(): MutableList<Processor> {
        TODO("Not yet implemented")
    }

    override fun getInterceptStrategies(): MutableList<InterceptStrategy> {
        TODO("Not yet implemented")
    }

    override fun setManagementInterceptStrategy(interceptStrategy: ManagementInterceptStrategy?) {
        TODO("Not yet implemented")
    }

    override fun getManagementInterceptStrategy(): ManagementInterceptStrategy {
        TODO("Not yet implemented")
    }

    override fun getRoutePolicyList(): MutableList<RoutePolicy> {
        TODO("Not yet implemented")
    }

    override fun setErrorHandlerFactory(errorHandlerFactory: ErrorHandlerFactory?) {
        TODO("Not yet implemented")
    }

    override fun getErrorHandlerFactory(): ErrorHandlerFactory {
        TODO("Not yet implemented")
    }

    override fun createErrorHandler(processor: Processor?): Processor {
        TODO("Not yet implemented")
    }

    override fun getOnCompletions(): MutableCollection<Processor> {
        TODO("Not yet implemented")
    }

    override fun getOnCompletion(onCompletionId: String?): Processor {
        TODO("Not yet implemented")
    }

    override fun setOnCompletion(onCompletionId: String?, processor: Processor?) {
        TODO("Not yet implemented")
    }

    override fun getOnExceptions(): MutableCollection<Processor> {
        TODO("Not yet implemented")
    }

    override fun getOnException(onExceptionId: String?): Processor {
        TODO("Not yet implemented")
    }

    override fun setOnException(onExceptionId: String?, processor: Processor?) {
        TODO("Not yet implemented")
    }

    override fun addErrorHandler(factory: ErrorHandlerFactory?, exception: NamedNode?) {
        TODO("Not yet implemented")
    }

    override fun getErrorHandlers(factory: ErrorHandlerFactory?): MutableSet<NamedNode> {
        TODO("Not yet implemented")
    }

    override fun addErrorHandlerFactoryReference(source: ErrorHandlerFactory?, target: ErrorHandlerFactory?) {
        TODO("Not yet implemented")
    }
}