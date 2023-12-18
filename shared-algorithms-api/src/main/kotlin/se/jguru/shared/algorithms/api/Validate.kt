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
package se.jguru.shared.algorithms.api

/**
 * Simple argument validator, inspired by the commons-lang.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se)
 */
object Validate {

    /**
     * Validates that the supplied obj is not null, and throws a NullPointerException otherwise.
     *
     * @param obj The obj to validate for `null`-ness.
     * @param argumentName The argument name of the obj to validate. If supplied (i.e. non-`null`),
     * this value is used in composing a better exception message.
     * @return The supplied obj - if it is was not null.
     * @throws NullPointerException if the supplied obj was null.
     */
    @JvmStatic
    @Throws(NullPointerException::class)
    fun <T> notNull(obj: T?, argumentName: String): T = when (obj) {
        null -> throw NullPointerException(getMessage("null", argumentName))
        else -> obj
    }

    /**
     * Validates that the supplied object is not null, and throws an IllegalArgumentException otherwise.
     *
     * @param aMap         The Map to validate for emptyness.
     * @param argumentName The argument name of the object to validate. If supplied (i.e. non-`null`), this value is
     * used in composing a better exception message.
     * @param <K>          the type of keys found within the Map.
     * @param <V>          the type of values found within the Map.
     * @param <M>          the exact type of Map submitted for emptyness check.
     * @return The Collection submitted.
     * @throws IllegalArgumentException if the submitted Map is empty.
     * @throws NullPointerException     if the supplied Map is null.
     */
    @JvmStatic
    @Throws(NullPointerException::class, IllegalArgumentException::class)
    fun <K, V, M : Map<in K, V>> notEmpty(aMap: M?, argumentName: String): M {

        // Check sanity
        notNull(aMap, argumentName)

        // All Done.
        return when {
            aMap!!.isEmpty() -> throw IllegalArgumentException(getMessage("empty", argumentName))
            else -> aMap
        }
    }

    /**
     * Validates that the supplied object is not null, and throws a NullPointerException otherwise.
     * If the supplied collection is empty, then an IllegalArgumentException is thrown.
     *
     * @param aCollection  The Collection to validate for emptyness.
     * @param argumentName The argument name of the object to validate.
     * If supplied (i.e. non-`null`), this value is used in composing
     * a better exception message.
     * @param <C>          The type of Collection to validate for emptyness.
     * @param <T>          The type elements within the Collection.
     * @return The Collection submitted.
     * @throws IllegalArgumentException if the submitted Collection is empty.
     * @throws NullPointerException     if the supplied Collection is null.
     */
    @JvmStatic
    @Throws(NullPointerException::class, IllegalArgumentException::class)
    fun <T, C : Collection<T>> notEmpty(aCollection: C?, argumentName: String): C {

        // Check sanity
        notNull(aCollection, argumentName)

        return when {
            aCollection!!.isEmpty() -> throw IllegalArgumentException(getMessage("empty", argumentName))
            else -> aCollection
        }
    }

    /**
     * Validates that the supplied object is not null, and throws an IllegalArgumentException otherwise.
     *
     * @param aString      The string to validate for emptyness.
     * @param argumentName The argument name of the object to validate.
     * If supplied (i.e. non-`null`), this value is used in composing
     * a better exception message.
     * @return The non-empty String submitted.
     * @throws IllegalArgumentException if the submitted `aString` is empty.
     * @throws NullPointerException     if the supplied `aString` is null.
     */
    @JvmStatic
    @Throws(NullPointerException::class, IllegalArgumentException::class)
    fun notEmpty(aString: String?, argumentName: String): String {

        // Check sanity
        notNull(aString, argumentName)

        // All Done.
        return when (aString!!.isEmpty()) {
            true -> throw IllegalArgumentException(getMessage("empty", argumentName))
            else -> aString
        }
    }

    /**
     * Validates that the supplied array is not null, and throws a NullPointerException otherwise.
     * If the supplied array is empty, then an IllegalArgumentException is thrown.
     *
     * @param anArray      The Array to validate for empty-ness.
     * @param argumentName The argument name of the object to validate.
     * If supplied (i.e. non-`null`), this value is used in composing
     * a better exception message.
     * @param <T>          The type elements within the Array.
     * @return The Array submitted.
     * @throws IllegalArgumentException if the submitted Array is empty.
     * @throws NullPointerException     if the supplied Array is null.
     */
    @JvmStatic
    @Throws(NullPointerException::class, IllegalArgumentException::class)
    fun <T> notEmpty(anArray: Array<T>?, argumentName: String): Array<T> {

        // Check sanity
        notNull(anArray, argumentName)

        // All Done.
        return when (anArray!!.isEmpty()) {
            true -> throw IllegalArgumentException(getMessage("empty", argumentName))
            else -> anArray
        }
    }

    /**
     * Validates that the supplied condition is true, and throws an IllegalArgumentException otherwise.
     *
     * @param condition The condition to validate for truth.
     * @param message   The exception message used within the IllegalArgumentException if the condition is false.
     */
    @JvmStatic
    @Throws(NullPointerException::class, IllegalArgumentException::class)
    fun isTrue(condition: Boolean?, message: String) {

        if (condition == null || !condition) {
            throw IllegalArgumentException(message)
        }
    }

    //
    // Private helpers
    //

    @JvmStatic
    private fun getMessage(exceptionDefinition: String, argumentName: String = ""): String {

        // All Done.
        val suffix = if (argumentName.isNotEmpty()) " '$argumentName'" else argumentName
        return "Cannot handle $exceptionDefinition$suffix argument."
    }
}
