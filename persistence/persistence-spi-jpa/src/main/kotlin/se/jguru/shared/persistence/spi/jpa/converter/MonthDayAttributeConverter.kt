/*-
 * #%L
 * Nazgul Project: jguru-shared-persistence-spi-jpa
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
package se.jguru.shared.persistence.spi.jpa.converter

import java.io.Serializable
import java.time.MonthDay
import javax.persistence.AttributeConverter
import javax.persistence.Converter
import javax.xml.bind.annotation.XmlTransient

/**
 * JPA AttributeConverter class to handle Java 8 [MonthDay] - which will convert to and from [String]s.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@XmlTransient
@Converter(autoApply = true)
open class MonthDayAttributeConverter : AttributeConverter<MonthDay, String>, Serializable {

    override fun convertToDatabaseColumn(attribute: MonthDay?): String? = when (attribute) {
        null -> null
        else -> attribute.toString()
    }

    override fun convertToEntityAttribute(dbData: String?): MonthDay? = when (dbData) {
        null -> null
        else -> MonthDay.parse(dbData)
    }
}
