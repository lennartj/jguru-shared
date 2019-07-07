/*-
 * #%L
 * Nazgul Project: jguru-shared-jaxb-spi-shared
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
package se.jguru.shared.jaxb.spi.shared.adapters

import java.time.format.DateTimeFormatter
import java.util.Currency
import javax.xml.bind.annotation.XmlTransient
import javax.xml.bind.annotation.adapters.XmlAdapter

/**
 * XML Adapter class to handle Java 8 [Currency] - which will convert to
 * and from Strings using the [Currency.currencyCode].
 *
 * @param formatter The [DateTimeFormatter] used to render date strings.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@XmlTransient
open class CurrencyAdapter : XmlAdapter<String, Currency>() {

    override fun marshal(instance: Currency?): String? = when (instance == null) {
        true -> null
        else -> instance.currencyCode
    }

    override fun unmarshal(transportForm: String?): Currency? = when (transportForm == null) {
        true -> null
        else -> Currency.getInstance(transportForm)
    }
}
