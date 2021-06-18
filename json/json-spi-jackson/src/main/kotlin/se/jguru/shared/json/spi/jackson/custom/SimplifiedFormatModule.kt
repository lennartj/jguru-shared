/*-
 * #%L
 * Nazgul Project: jguru-shared-json-spi-jackson
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
package se.jguru.shared.json.spi.jackson.custom

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.module.SimpleModule
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import se.jguru.shared.algorithms.api.introspection.Introspection
import java.time.*
import java.util.Locale
import java.util.TreeMap
import java.util.jar.Manifest

/**
 * Module containing Jackson serializers and deserializers, intended to simplify
 * some Jackson transport formats.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class SimplifiedFormatModule : SimpleModule(SimplifiedFormatModule::class.java.simpleName, findLocalJarVersion()) {

    init {

        // Add Serializers
        addSerializer(ZonedDateTime::class.java, ZonedDateTimeSerializer())
        addSerializer(LocalDateTime::class.java, LocalDateTimeSerializer())
        addSerializer(LocalDate::class.java, LocalDateSerializer())
        addSerializer(LocalTime::class.java, LocalTimeSerializer())
        addSerializer(Duration::class.java, DurationSerializer())
        addSerializer(Period::class.java, PeriodSerializer())
        addSerializer(MonthDay::class.java, MonthDaySerializer())

        // Add Deserializers
        addDeserializer(ZonedDateTime::class.java, ZonedDateTimeDeserializer())
        addDeserializer(LocalDateTime::class.java, LocalDateTimeDeserializer())
        addDeserializer(LocalDate::class.java, LocalDateDeserializer())
        addDeserializer(LocalTime::class.java, LocalTimeDeserializer())
        addDeserializer(Duration::class.java, DurationDeserializer())
        addDeserializer(Period::class.java, PeriodDeserializer())
        addDeserializer(MonthDay::class.java, MonthDayDeserializer())
    }

    companion object {

        @JvmStatic
        private val log: Logger = LoggerFactory.getLogger(SimplifiedFormatModule::class.java)

        /**
         * Finds the [Version] by reading the `Bundle-Version` property from the Manifest file.
         * Typically, this has the format `Bundle-Version: 0.10.1.SNAPSHOT`
         */
        @JvmStatic
        fun findLocalJarVersion(): Version {

            // Extract the Map from the Manifest, or a fallback
            val mapFromManifest = when (val mf = findManifest()) {
                null -> {
                    val fallbackMap = TreeMap<String, String>()
                    fallbackMap[Introspection.SPECIFICATION_VERSION] = "0.10.1"
                    fallbackMap
                }
                else -> Introspection.extractMapOf(mf)
            }

            // Extract relevant data
            val runtimeVersion = Introspection.findVersionFromMap(mapFromManifest)
            val groupID = mapFromManifest["groupId"] ?: "se.jguru.shared.json.spi.jackson"
            val artifactID = mapFromManifest["artifactId"] ?: "jguru-shared-json-spi-jackson"

            // All Done.
            return Version(runtimeVersion.major,
                runtimeVersion.minor ?: 0,
                runtimeVersion.micro ?: 0,
                runtimeVersion.qualifier ?: "",
                groupID,
                artifactID)
        }

        @JvmStatic
        internal fun findManifest(): Manifest? {

            val localClassLoader = SimplifiedFormatModule::class.java.classLoader

            return try {

                // This is quite a hardcoded approach, but it works as
                // long as the artifactID of this JAR is stable.
                val mfResource = localClassLoader.getResources(Introspection.MANIFEST_RESOURCE).toList()
                    .firstOrNull { it.file.toLowerCase(Locale.ENGLISH).contains("jguru-shared-json-spi-jackson") }

                when(mfResource) {
                    null -> null
                    else -> Manifest(mfResource.openStream())
                }

            } catch (e: Exception) {

                val msg = "Could not find SimplifiedFormatModule Manifest using ClassLoader of type " +
                    "[${localClassLoader::class.java.simpleName}] of class " +
                    "[${SimplifiedFormatModule::class.java.name}]"

                if (log.isDebugEnabled) {
                    log.debug(msg, e)
                } else if (log.isWarnEnabled) {
                    log.warn(msg)
                }

                // Ignore this
                null
            }
        }
    }
}
