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

import java.util.Locale
import jakarta.xml.bind.annotation.XmlTransient
import jakarta.xml.bind.annotation.adapters.XmlAdapter

/**
 * XML Adapter class to handle [Locale] objects.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@XmlTransient
open class LocaleAdapter : XmlAdapter<String, Locale>() {

    /**
     * {@inheritDoc}
     */
    @Throws(Exception::class)
    override fun unmarshal(transportForm: String?): Locale? = when (transportForm) {
        null -> null
        else -> Locale.forLanguageTag(transportForm)
    }

    /**
     * {@inheritDoc}
     */
    @Throws(Exception::class)
    override fun marshal(objectForm: Locale?): String? = when (objectForm) {
        null -> null
        else -> objectForm.toLanguageTag()
    }
}
