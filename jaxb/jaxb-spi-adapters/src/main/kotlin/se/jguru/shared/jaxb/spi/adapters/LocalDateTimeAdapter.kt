/*-
 * #%L
 * Nazgul Project: jguru-shared-jaxb-spi-adapters
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
package se.jguru.shared.jaxb.spi.adapters

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.xml.bind.annotation.XmlTransient
import javax.xml.bind.annotation.adapters.XmlAdapter

/**
 * XML Adapter class to handle Java 8 [LocalDateTime] - which will convert to
 * and from Strings using the [DateTimeFormatter.ISO_LOCAL_DATE_TIME].
 *
 * @param formatter The [DateTimeFormatter] used to render date strings.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@XmlTransient
open class LocalDateTimeAdapter @JvmOverloads constructor(
    private val formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
) : XmlAdapter<String, LocalDateTime>() {

    /**
     * {@inheritDoc}
     */
    @Throws(Exception::class)
    override fun unmarshal(transportForm: String?): LocalDateTime? = when (transportForm) {
        null -> null
        else -> LocalDateTime.parse(transportForm, formatter)
    }

    /**
     * {@inheritDoc}
     */
    @Throws(Exception::class)
    override fun marshal(objectForm: LocalDateTime?): String? = when (objectForm) {
        null -> null
        else -> formatter.format(objectForm)
    }
}

