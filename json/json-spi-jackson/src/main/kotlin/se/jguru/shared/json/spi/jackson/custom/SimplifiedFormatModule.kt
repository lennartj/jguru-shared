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
import se.jguru.shared.algorithms.api.introspection.Introspection
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Module containing Jackson serializers and deserializers, intended to simplify
 * some Jackson transport formats.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class SimplifiedFormatModule : SimpleModule(SimplifiedFormatModule::class.java.simpleName, findLocalJarVersion()) {

    init {

        // Add Serializers
        addSerializer(LocalDateTime::class.java, LocalDateTimeSerializer())
        addSerializer(LocalDate::class.java, LocalDateSerializer())
        addSerializer(LocalTime::class.java, LocalTimeSerializer())
        addSerializer(Duration::class.java, DurationSerializer())

        // Add Deserializers
        addDeserializer(LocalDateTime::class.java, LocalDateTimeDeserializer())
        addDeserializer(LocalDate::class.java, LocalDateDeserializer())
        addDeserializer(LocalTime::class.java, LocalTimeDeserializer())
        addDeserializer(Duration::class.java, DurationDeserializer())
    }

    companion object {

        /**
         * Finds the [Version] by reading the `Bundle-Version` property from the Manifest file.
         * Typically, this has the format `Bundle-Version: 0.9.6.SNAPSHOT`
         */
        @JvmStatic
        fun findLocalJarVersion(): Version {

            val manifest = Introspection.getManifestFrom(SimplifiedFormatModule::class.java)
            val runtimeVersion = Introspection.findVersionFromManifestProperty(manifest)

            val manifestMap = Introspection.extractMapOf(manifest)
            val groupID = manifestMap["groupId"]
            val artifactID = manifestMap["artifactId"]

            // All Done.
            return Version(runtimeVersion.major,
                runtimeVersion.minor ?: 0,
                runtimeVersion.minor ?: 0,
                runtimeVersion.qualifier ?: "",
                groupID,
                artifactID)
        }
    }
}
