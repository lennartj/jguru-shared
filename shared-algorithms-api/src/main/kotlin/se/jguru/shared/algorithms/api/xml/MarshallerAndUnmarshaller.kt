/*-
 * #%L
 * Nazgul Project: jguru-shared-algorithms-api
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
package se.jguru.shared.algorithms.api.xml

import java.io.Serializable
import javax.xml.bind.JAXBContext

/**
 * Specification for how to execute marshalling and unmarshalling operations.
 * While this is normally done using JAXB, the [MarshallerAndUnmarshaller] can be
 * implemented using several different technologies.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
interface MarshallerAndUnmarshaller : Serializable {

    /**
     * The [NamespacePrefixResolver] to be used within the marshalling and unmarshalling operations.
     */
    val namespacePrefixResolver: NamespacePrefixResolver

    /**
     * Specification for how to marshal (a set of) objects, to the [MarshallingFormat] indicated.
     *
     * @param loader The [ClassLoader] used to harvest types as required for the marshalling.
     * @param format The desired output [MarshallingFormat]
     * @param toMarshal The object(s) to marshal.
     * @return The marshalled transport form of the supplied [toMarshal] objects.
     */
    @Throws(IllegalArgumentException::class)
    fun marshal(loader: ClassLoader = Thread.currentThread().contextClassLoader,
                format: MarshallingFormat = MarshallingFormat.XML,
                vararg toMarshal: Any): String

    /**
     * Specification for how to unmarshal a previously marshalled (set of) objects.
     *
     * @param loader The [ClassLoader] used to harvest types as required for the unmarshalling.
     * @param format The expected input [MarshallingFormat]
     * @param resultType The type of object which should be resurrected from the supplied [toUnmarshal] string
     * @return The fully unmarshalled object.
     */
    @Throws(IllegalArgumentException::class)
    fun <T> unmarshal(loader: ClassLoader = Thread.currentThread().contextClassLoader,
                      format: MarshallingFormat = MarshallingFormat.XML,
                      resultType: Class<T>,
                      toUnmarshal: String): T

    /**
     * Adds the supplied type information classes to this [MarshallerAndUnmarshaller] in order to perform marshalling
     * or unmarshalling operations. Typically, classes required within the [JAXBContext] should be added here.
     *
     * @param typeInformation a (set of) Classes required for proper operation.
     */
    fun add(vararg typeInformation: Class<in Any>)
}
