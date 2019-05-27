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
package se.jguru.shared.algorithms.api.introspection

/**
 * Simple runtime version holder for a Java VM.
 *
 * @param major The [major] Java version.
 * @param minor The optional [minor] version number.
 * @param micro The optional [micro] version number.
 * @param build The optional build number.
 */
class RuntimeVersion @JvmOverloads constructor(
    val major: Int = 0,
    val minor: Int? = null,
    val micro: Int? = null,
    val qualifier : String? = null) : Comparable<RuntimeVersion> {

    constructor(versionList: List<Int>) : this(versionList[0],
        if (versionList.size > 1) versionList[1] else 0,
        if (versionList.size > 2) versionList[2] else 0)

    override fun compareTo(other: RuntimeVersion): Int {

        var toReturn = this.major - other.major

        if (toReturn == 0) {

            val leftMinor = this.minor ?: 0
            val rightMinor = other.minor ?: 0
            toReturn = leftMinor - rightMinor
        }

        if (toReturn == 0) {

            val leftMicro = this.micro ?: 0
            val rightMicro = other.micro ?: 0
            toReturn = leftMicro - rightMicro
        }

        // All Done
        return toReturn
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RuntimeVersion

        if (major != other.major) return false
        if (minor != other.minor) return false
        if (micro != other.micro) return false
        if (qualifier != other.qualifier) return false

        return true
    }

    override fun hashCode(): Int {
        var result = major
        result = 31 * result + (minor ?: 0)
        result = 31 * result + (micro ?: 0)
        result = 31 * result + (qualifier?.hashCode() ?: 0)
        return result
    }


    companion object {

        /**
         * Parses the Bundle-Version or Implementation-Version property typically found within the
         * Manifest file, into a runtime version.
         *
         * @param bundleVersion The bundle version, such as `0.9.6.SNAPSHOT`, `1.52.1-SNAPSHOT` or the like.
         * @return a [RuntimeVersion] parsed from the supplied bundleVersion.
         */
        @JvmStatic
        fun parseVersionString(bundleVersion: String): RuntimeVersion {

            // Bundle-Version: 0.9.6.SNAPSHOT
            // Implementation-Version: 0.9.6-SNAPSHOT
            val tokens = bundleVersion.split('.', '-')
                .map { it.trim() }
            if (tokens.isEmpty()) {
                throw IllegalArgumentException("Expected argument on the form [major].[minor].[micro].[qualifier], " +
                    "where only the major version is mandatory. Got: [$bundleVersion]")
            }

            val major = tokens[0].toInt()
            val minor = when {
                tokens.size > 1 -> toInt(tokens[1])
                else            -> null
            }
            val micro = when {
                tokens.size > 2 -> toInt(tokens[2])
                else            -> null
            }
            val qualifier = when {
                tokens.size > 3 -> tokens[3]
                else            -> null
            }

            // All Done.
            return RuntimeVersion(major, minor, micro, qualifier)
        }

        /**
         * Parses a Java version, typically on the form `1.8.0_181`, to a RuntimeVersion.
         */
        @JvmStatic
        @JvmOverloads
        @Throws(IllegalArgumentException::class)
        fun parseJavaVersion(versionProperty: String = System.getProperty("java.version")): RuntimeVersion {

            // Check sanity
            if (versionProperty.trim().isEmpty()) {

                // Known values for the java.version property:
                // 1.8.0_181,
                // 9.0.4,
                // 10.0.2,
                // 11

                throw IllegalArgumentException("Version property [$versionProperty] could not be parsed " +
                    "into a RuntimeVersion. Expected something like 1.8.0_181, 9.0.4, 10.0.2 or 11.")
            }

            // No .'s?
            if (!versionProperty.contains(".")) {
                return RuntimeVersion(versionProperty.toInt())
            }

            val parts = versionProperty.split(".").toList()
            return when (parts.size) {
                1 -> RuntimeVersion(versionProperty.toInt())
                2, 3 -> {

                    // Split the parts
                    val firstPart = toInt(parts[0]) ?: 0
                    val secondPart = toInt(parts[1])
                    val thirdPart = when(parts.size) {
                        3 -> parts[2]
                        else -> ""
                    }

                    var majorVersion: Int = firstPart
                    var minorVersion: Int? = secondPart
                    val microVersion: Int?

                    // Translate "1.7" and "1.8" into "7" and "8" respectively.
                    if (firstPart == 1) {

                        majorVersion = secondPart ?: 0

                        when (thirdPart.contains("_")) {

                            true -> {
                                minorVersion = toInt(thirdPart.substring(0, thirdPart.indexOf("_")))
                                microVersion = toInt(thirdPart.substring(thirdPart.indexOf("_") + 1))
                            }
                            else -> {
                                minorVersion = toInt(thirdPart)
                                microVersion = null
                            }
                        }
                    } else {
                        microVersion = toInt(thirdPart)
                    }

                    // All Done.
                    RuntimeVersion(majorVersion, minorVersion, microVersion)
                }
                else -> throw IllegalArgumentException("Version property [$versionProperty] could not be parsed " +
                    "into a RuntimeVersion. Expected something like 1.8.0_181, 9.0.4, 10.0.2 or 11.")
            }
        }

        @JvmStatic
        private fun toInt(part: String): Int? = try {
            Integer.parseInt(part)
        } catch (e: Exception) {
            null
        }
    }
}
