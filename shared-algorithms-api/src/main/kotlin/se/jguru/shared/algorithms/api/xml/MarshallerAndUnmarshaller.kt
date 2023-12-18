/*-
 * #%L
 * Nazgul Project: jguru-shared-algorithms-api
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
package se.jguru.shared.algorithms.api.xml

import java.io.Serializable

/**
 * Specification for how to execute marshalling and unmarshalling operations.
 * While this is normally done using JAXB, the [MarshallerAndUnmarshaller] can be
 * implemented using several different technologies.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
abstract class MarshallerAndUnmarshaller : Serializable {

    /**
     * Specification for how to marshal (a set of) objects, to the [MarshallingFormat] indicated.
     *
     * @param loader The [ClassLoader] used to harvest types as required for the marshalling.
     * @param format The desired output [MarshallingFormat]
     * @param toMarshal The object(s) to marshal.
     * @return The marshalled transport form of the supplied [toMarshal] objects.
     */
    @Throws(IllegalArgumentException::class)
    abstract fun marshal(loader: ClassLoader, format: MarshallingFormat, toMarshal: Array<Any>): String

    /**
     * Specification for how to marshal (a set of) objects, to the [MarshallingFormat] indicated.
     * Uses the current Thread context [ClassLoader].
     *
     * @param format The desired output [MarshallingFormat]
     * @param toMarshal The object(s) to marshal.
     * @return The marshalled transport form of the supplied [toMarshal] objects.
     */
    @Throws(IllegalArgumentException::class)
    fun marshal(format: MarshallingFormat, toMarshal: Array<Any>): String =
        marshal(Thread.currentThread().contextClassLoader, format, toMarshal)

    /**
     * Specification for how to marshal (a set of) objects, to the [MarshallingFormat] indicated.
     * Uses the current Thread context [ClassLoader], and [MarshallingFormat.XML].
     *
     * @param toMarshal The object(s) to marshal.
     * @return The marshalled transport form of the supplied [toMarshal] objects.
     */
    @Throws(IllegalArgumentException::class)
    fun marshal(toMarshal: Array<Any>): String = marshal(
        Thread.currentThread().contextClassLoader,
        MarshallingFormat.XML,
        toMarshal)

    /**
     * Specification for how to unmarshal a previously marshalled (set of) objects.
     *
     * @param loader The [ClassLoader] used to harvest types as required for the unmarshalling.
     * @param format The expected input [MarshallingFormat]
     * @param resultType The type of object which should be resurrected from the supplied [toUnmarshal] string
     * @return The fully unmarshalled object.
     */
    @Throws(IllegalArgumentException::class)
    abstract fun <T> unmarshal(loader: ClassLoader,
                               format: MarshallingFormat,
                               resultType: Class<T>,
                               toUnmarshal: String): T

    /**
     * Specification for how to unmarshal a previously marshalled (set of) objects.
     * Uses the current Thread context [ClassLoader] to load type information.
     *
     * @param format The expected input [MarshallingFormat]
     * @param resultType The type of object which should be resurrected from the supplied [toUnmarshal] string
     * @return The fully unmarshalled object.
     */
    @Throws(IllegalArgumentException::class)
    fun <T> unmarshal(format: MarshallingFormat, resultType: Class<T>, toUnmarshal: String): T = unmarshal(
        Thread.currentThread().contextClassLoader, format, resultType, toUnmarshal)

    /**
     * Specification for how to unmarshal a previously marshalled (set of) objects.
     * Uses the current Thread context [ClassLoader] to load type information, and [MarshallingFormat.XML].
     *
     * @param resultType The type of object which should be resurrected from the supplied [toUnmarshal] string
     * @return The fully unmarshalled object.
     */
    @Throws(IllegalArgumentException::class)
    fun <T> unmarshal(resultType: Class<T>, toUnmarshal: String): T = unmarshal(
        Thread.currentThread().contextClassLoader, MarshallingFormat.XML, resultType, toUnmarshal)

    /**
     * Adds the supplied type information classes to this [MarshallerAndUnmarshaller] in order to perform marshalling
     * or unmarshalling operations. Typically, classes required within the JAXBContext should be added here.
     *
     * @param typeInformation a (set of) Classes required for proper operation.
     */
    abstract fun add(vararg typeInformation: Class<in Any>)
}
