/*-
 * #%L
 * Nazgul Project: jguru-shared-jaxb-spi-shared
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
package se.jguru.shared.jaxb.spi.shared.adapters

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import jakarta.xml.bind.annotation.XmlTransient
import jakarta.xml.bind.annotation.adapters.XmlAdapter

/**
 * XML Adapter class to handle Java 8 [ZonedDateTime] - which will convert to
 * and from Strings using the [DateTimeFormatter.ISO_ZONED_DATE_TIME].
 *
 * @param formatter The [DateTimeFormatter] used to render date strings.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 * @see DateTimeFormatter.ISO_ZONED_DATE_TIME
 */
@XmlTransient
open class ZonedDateTimeAdapter @JvmOverloads constructor(
    private val formatter: DateTimeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME
) : XmlAdapter<String, ZonedDateTime>() {

    /**
     * {@inheritDoc}
     */
    @Throws(Exception::class)
    override fun unmarshal(transportForm: String?): ZonedDateTime? = when (transportForm) {
        null -> null
        else -> ZonedDateTime.parse(transportForm, formatter)
    }

    /**
     * {@inheritDoc}
     */
    @Throws(Exception::class)
    override fun marshal(dateTime: ZonedDateTime?): String? = when (dateTime) {
        null -> null
        else -> formatter.format(dateTime)
    }
}
