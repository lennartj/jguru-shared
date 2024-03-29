/*-
 * #%L
 * Nazgul Project: jguru-shared-json-jackson-example
 * %%
 * Copyright (C) 2018 - 2023 jGuru Europe AB
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
package se.jguru.shared.json.jackson.example

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.annotation.ObjectIdGenerators

/**
 * Driver entity.
 *
 * @param name The name of this Driver.
 * @param cars A List of Cars that this Driver prefers.
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator::class, property = "@id")
@JsonPropertyOrder(value = ["name", "cars"])
data class Driver(val name : String, val cars : List<Car>)
