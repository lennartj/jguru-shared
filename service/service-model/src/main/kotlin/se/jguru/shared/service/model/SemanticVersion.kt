/*-
 * #%L
 * Nazgul Project: jguru-shared-service-model
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
package se.jguru.shared.service.model

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import se.jguru.shared.algorithms.api.Validate
import java.io.Serializable
import java.lang.IllegalArgumentException
import javax.validation.constraints.Min

/**
 * Class defining semantic versioning.
 *
 * Uses 3 version numbers and an optional qualifier on the form
 * `major.minor.micro[.qualifier]`
 *
 * @param major The Major version number. Must be greater or equal to 0.
 * @param minor The Minor version number. Must be greater or equal to 0.
 * @param micro The Micro version number. Must be greater or equal to 0.
 * @param qualifier The optional qualifier of this SemanticVersion.
 *
 * @see <a href="https://semver.org">Semantic Versioning website</a>
 * @author @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@JsonPropertyOrder(value = ["major", "minor", "micro", "qualifier"])
data class SemanticVersion @JvmOverloads constructor(
        @Min(0) val major: Int = 0,
        @Min(0) val minor: Int = 0,
        @Min(0) val micro: Int = 0,
        val qualifier: String? = null) : Serializable, Comparable<SemanticVersion> {

    init {
        Validate.isTrue(major >= 0, "Cannot handle negative major version. (Got: $major)")
        Validate.isTrue(minor >= 0, "Cannot handle negative minor version. (Got: $minor)")
        Validate.isTrue(micro >= 0, "Cannot handle negative micro version. (Got: $micro)")
    }

    /**
     * Checks if this SemanticVersion is the [UNDEFINED], indicating that no version information is known.
     */
    val isUndefined: Boolean
        get() = UNDEFINED.compareTo(this) == 0

    override fun toString(): String {

        val prefix = "$major.$minor.$micro"

        return if (qualifier == null) {
            prefix
        } else {
            "$prefix.$qualifier"
        }
    }

    /**
     * Compares all version numbers in order.
     * Also compares the qualifiers if present (and non-null).
     *
     * @see [Comparable]
     */
    override fun compareTo(other: SemanticVersion): Int {

        var toReturn = this.major - other.major

        if (toReturn == 0) {
            toReturn = this.minor - other.minor
        }

        if (toReturn == 0) {
            toReturn = this.micro - other.micro
        }

        if (toReturn == 0) {
            val thisQualifier = this.qualifier ?: ""
            val thatQualifier = other.qualifier ?: ""
            toReturn = thisQualifier.compareTo(thatQualifier)
        }

        // All Done.
        return toReturn
    }

    companion object {

        /**
         * The undefined SemanticVersion, implying that it contains no version information at all.
         */
        @JvmStatic
        val UNDEFINED = SemanticVersion()

        /**
         * The delimiter separating version segments from each other.
         */
        const val SEGMENT_DELIMITER = "."

        /**
         * Factory method parsing a version string on the form `major.minor.micro[.qualifier]` to
         * a [SemanticVersion] object
         *
         * @param versionString a String on the form `major.minor.micro[.qualifier]` to be
         * resurrected into a [SemanticVersion].
         * @return A SemanticVersion corresponding to the versionString given.
         * @throws IllegalArgumentException if the given versionString could not be parsed into a [SemanticVersion]
         */
        @JvmStatic
        @Throws(IllegalArgumentException::class)
        fun parseFrom(versionString: String): SemanticVersion {

            // The versionString must have the form "1[.2[.3[.Final]]]"
            val split = versionString.split(SEGMENT_DELIMITER)

            // Crude, but visible parsing.
            return when (split.size) {

                1 -> SemanticVersion(major = Integer.parseInt(split[0]))
                2 -> SemanticVersion(major = Integer.parseInt(split[0]),
                        minor = Integer.parseInt(split[1]))
                3 -> SemanticVersion(major = Integer.parseInt(split[0]),
                        minor = Integer.parseInt(split[1]),
                        micro = Integer.parseInt(split[2]))
                4 -> SemanticVersion(major = Integer.parseInt(split[0]),
                        minor = Integer.parseInt(split[1]),
                        micro = Integer.parseInt(split[2]),
                        qualifier = split[3])

                else -> throw IllegalArgumentException("Could not parse '$versionString' into a SemanticVersion. " +
                                                               "Expected the form '1[.2[.3[.Final]]]'")
            }
        }
    }
}
