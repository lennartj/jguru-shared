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

import java.util.Optional
import java.util.regex.Pattern

/**
 * RuntimeVersion state holder for Java Versions.
 *
 * @param major The [major] Java version.
 * @param minor Returns the [minor] version number. Defaults to 0.
 * @param security The [security] version number. Defaults to 0.
 * @param pre The optional [preRelease] information.
 * @param build The optional build number.
 */
class RuntimeVersion @JvmOverloads constructor(
    val major: Int = 0,
    val minor: Int = 0,
    val security: Int = 0,
    val preRelease: String? = null,
    val build: Int? = null) : Comparable<RuntimeVersion> {

    constructor(versionList: List<Int>) : this(versionList[0],
        if (versionList.size > 1) versionList[1] else 0,
        if (versionList.size > 2) versionList[2] else 0)

    override fun compareTo(other: RuntimeVersion): Int {

        var toReturn = this.major - other.major

        if (toReturn == 0) {
            toReturn = this.minor - other.minor
        }

        if (toReturn == 0) {

            val leftBuild = this.build ?: 0
            val rightBuild = other.build ?: 0
            toReturn = leftBuild - rightBuild
        }

        // All Done
        return toReturn
    }
}

/**
 * Trivial inspector for JVM versions, backported to work with JDK 8+.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
object RuntimeInspector {

    /**
     * Parses the given string as a valid version string containing a version number
     * followed by pre-release and build information.
     *
     * @param  s A string to interpret as a version
     *
     * @throws  IllegalArgumentException If the given string cannot be interpreted as a valid version
     * @throws  NullPointerException If the given string is `null`
     * @throws  NumberFormatException If an element of the version number or the build number cannot be represented
     * as an [Integer]
     *
     * @return  The Version of the given string
     */
    @JvmStatic
    fun parse(s: String): RuntimeVersion {

        // Shortcut to avoid initializing VersionPattern when creating
        // major version constants during startup
        if (isSimpleNumber(s)) {
            return RuntimeVersion(Integer.parseInt(s))
        }

        val m = VersionPattern.VSTR_PATTERN.matcher(s)
        if (!m.matches()) {
            throw IllegalArgumentException("Invalid version string: '$s'")
        }

        // $VNUM is a dot-separated list of integers of arbitrary length
        val split = m.group(VersionPattern.VNUM_GROUP)
            .split("\\.".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()

        val version = arrayOf(split.size)
        split.forEachIndexed { index, element -> version[index] = Integer.parseInt(element) }

        val pre = Optional.ofNullable(m.group(VersionPattern.PRE_GROUP))

        val b = m.group(VersionPattern.BUILD_GROUP)
        // $BUILD is an integer
        val build = when (b) {
            null -> Optional.empty<Int>()
            else -> Optional.of(Integer.parseInt(b))
        }

        val optional = Optional.ofNullable(m.group(VersionPattern.OPT_GROUP))

        // empty '+'
        if (m.group(VersionPattern.PLUS_GROUP) != null && !build.isPresent) {
            if (optional.isPresent) {
                if (pre.isPresent)
                    throw IllegalArgumentException("'+' found with"
                        + " pre-release and optional components:'" + s
                        + "'")
            } else {
                throw IllegalArgumentException("'+' found with neither"
                    + " build or optional components: '" + s + "'")
            }
        }

        // All Done
        val majorVersion = when {
            version.isNotEmpty() -> version[0]
            else -> 0
        }

        val minorVersion = when {
            version.size > 1 -> version[1]
            else -> 0
        }

        val securityVersion = when {
            version.size > 2 -> version[2]
            else -> 0
        }

        return RuntimeVersion(majorVersion,
            minorVersion,
            securityVersion,
            pre.orElseGet { null },
            build.orElseGet { null as Int? })
    }

    private fun isSimpleNumber(s: String): Boolean {
        for (i in 0 until s.length) {
            val c = s[i]
            val lowerBound = if (i > 0) '0' else '1'
            if (c < lowerBound || c > '9') {
                return false
            }
        }
        return true
    }


    private object VersionPattern {

        // Version expressions defined within the Runtime.Version class of JDK 9

        private val VNUM = "(?<VNUM>[1-9][0-9]*(?:(?:\\.0)*\\.[1-9][0-9]*)*)"
        private val PRE = "(?:-(?<PRE>[a-zA-Z0-9]+))?"
        private val BUILD = "(?:(?<PLUS>\\+)(?<BUILD>0|[1-9][0-9]*)?)?"
        private val OPT = "(?:-(?<OPT>[-a-zA-Z0-9.]+))?"
        private val VSTR_FORMAT = VNUM + PRE + BUILD + OPT

        internal val VSTR_PATTERN = Pattern.compile(VSTR_FORMAT)

        internal val VNUM_GROUP = "VNUM"
        internal val PRE_GROUP = "PRE"
        internal val PLUS_GROUP = "PLUS"
        internal val BUILD_GROUP = "BUILD"
        internal val OPT_GROUP = "OPT"
    }
}
