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
    val micro: Int? = null) : Comparable<RuntimeVersion> {

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

    companion object {

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
                    var microVersion: Int?

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
