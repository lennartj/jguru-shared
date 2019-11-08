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

/**
 * JSON mapper using Jackson, handling any class.
 * This also implies that the mapper can use any CDI scope, not only Dependent
 * which is sometimes required when the producer class has generic annotations.
 */
open class JacksonJsonAnyMapper : JacksonJsonMapper<Any>()
