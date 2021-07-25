/*-
 * #%L
 * Nazgul Project: jguru-shared-restful-spi-jaxrs
 * %%
 * Copyright (C) 2018 - 2019 jGuru Europe AB
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
package se.jguru.shared.restful.spi.jaxrs

import com.fasterxml.jackson.databind.ObjectMapper
import se.jguru.shared.json.spi.jackson.ObjectMapperBuilder

/**
 * ## JSON mapper using Jackson, handling any class.
 *
 * This convenience implementation, should be used in the following manner
 * within an existing CDI-enabled application such as a WAR:
 *
 * ```java
 * @Producer
 * public class MyJsonProvider extends JacksonJsonAnyMapper {}
 * ```
 *
 * Deserializing objects of type Foo using the MyJsonProvider would be done using a
 * rather odd class-cast construct, namely:
 *
 * ```java
 *  val resurrected = myJsonProvider.readFrom(
 *      Furniture::class.java as Class<Any>,  // A bit weird, but required to coerce Kotlin's type system.
 *      String::class.java,
 *      emptyArray,
 *      jsonType,
 *      emptyMultiValuedMap,
 *      input)
 * ```
 *
 * ### Note!
 *
 * Do not use generics in your implementation. According to §3.1 of the JSR-299
 * (CDI) specification:
 *
 * > "If the managed bean class is a generic type, it must have scope @Dependent.
 * > If a managed bean with a parameterized bean class declares any scope other than @Dependent, the
 * > container automatically detects the problem and treats it as a definition error."
 */
open class JacksonJsonAnyMapper @JvmOverloads constructor(
    objectMapper: ObjectMapper = ObjectMapperBuilder.getDefault()
) : JacksonJsonMapper<Any>(objectMapper)
