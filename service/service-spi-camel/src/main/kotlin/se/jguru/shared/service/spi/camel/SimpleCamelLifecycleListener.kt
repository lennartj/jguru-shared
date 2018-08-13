/*-
 * #%L
 * Nazgul Project: jguru-shared-service-spi-camel
 * %%
 * Copyright (C) 2018 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * 
 *       http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package se.jguru.shared.service.spi.camel

import org.apache.camel.CamelContext
import org.apache.camel.main.MainListenerSupport

/**
 * Type alias for a [MainListener], providing the option to pre-configure the [CamelContext].
 *
 * @param humanReadableDescription A short - but human-readable - description of this SimpleCamelLifecycleListener.
 * The value is typically used within log statements to identify the SimpleCamelLifecycleListener.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
open class SimpleCamelLifecycleListener(val humanReadableDescription: String) : MainListenerSupport()

/**
 * SimpleCamelLifecycleListener implementation which validates all Routes within the given CamelContext using a List
 * of [RouteValidator] objects, wrapped within a CompoundRouteValidator.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class RoutesValidatorListener @JvmOverloads constructor(
    humanReadableDescription: String,
    validators: MutableList<RouteValidator> = mutableListOf(HasNonStandardIdValidator()))

    : SimpleCamelLifecycleListener(humanReadableDescription) {

    // Internal state
    val compoundRouteValidator = CompoundRouteValidator(validators)

    /**
     * Adds the supplied [RouteValidator] to the CompoundRouteValidator internals.
     */
    fun addValidator(validator: RouteValidator) {
        compoundRouteValidator.validators.add(validator)
    }

    override fun configure(context: CamelContext?) {

        // Check sanity
        if (context == null) {
            throw IllegalArgumentException("Cannot handle null CamelContext. Please check configuration.")
        }

        // Validate the Routes within the CamelContext
        for (aRoute in context.routes) {

            val validRoute = compoundRouteValidator.isValid(aRoute)
            if (!validRoute) {
                throw IllegalStateException("Invalid Route(s) detected within CamelContext ${context.name}.")
            }
        }
    }
}
