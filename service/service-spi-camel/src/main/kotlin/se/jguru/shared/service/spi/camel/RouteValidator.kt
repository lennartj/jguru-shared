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

import org.apache.camel.Route
import org.slf4j.LoggerFactory
import java.io.Serializable

// Our log
private val log = LoggerFactory.getLogger(CompoundRouteValidator::class.java)

/**
 * Validation specification for an Apache Camel [Route].
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
interface RouteValidator : Serializable {

    /**
     * Validation method to indicate if the supplied Route is valid or not.
     *
     * @return `true` if the supplied [Route] is valid.
     */
    fun isValid(route: Route): Boolean
}

/**
 * RouteValidator requiring that Routes should not start with "Route", by a case insensitive match.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class HasNonStandardIdValidator : RouteValidator {

    /**
     * Routes should not start with "Route", by a case insensitive match.
     */
    override fun isValid(route: Route): Boolean = !route.id.toLowerCase().startsWith("route")
}

/**
 * Compound RouteValidator holder.
 * Use this as a simple means of combining several [RouteValidator]s, to act as one (i.e. all RouteValidators must
 * pass in order for the route to pass).
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class CompoundRouteValidator(val validators: MutableList<RouteValidator> = mutableListOf()) : RouteValidator {

    override fun isValid(route: Route): Boolean {

        validators.forEachIndexed { index, current ->

            if (!current.isValid(route)) {

                if (log.isWarnEnabled) {
                    log.warn("Validator $index of ${validators.size} claimed Route ${route.id} was invalid.")
                }

                // All Done.
                return false
            }
        }

        // All Done.
        return true
    }
}
