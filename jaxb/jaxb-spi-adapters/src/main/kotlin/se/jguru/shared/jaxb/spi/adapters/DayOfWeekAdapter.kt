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

import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import javax.xml.bind.annotation.XmlTransient
import javax.xml.bind.annotation.adapters.XmlAdapter

/**
 * XML Adapter class to handle Java 8 [DayOfWeek] - which will convert to
 * and from Ints using the [DayOfWeek.getValue].
 *
 * @param formatter The [DateTimeFormatter] used to render date strings.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@XmlTransient
open class DayOfWeekAdapter() : XmlAdapter<Int, DayOfWeek>() {

    override fun marshal(instance: DayOfWeek?): Int? = when (instance == null) {
        true -> null
        else -> instance!!.value
    }

    override fun unmarshal(transportForm: Int?): DayOfWeek? = when (transportForm == null) {
        true -> null
        else -> DayOfWeek.of(transportForm!!)
    }
}
