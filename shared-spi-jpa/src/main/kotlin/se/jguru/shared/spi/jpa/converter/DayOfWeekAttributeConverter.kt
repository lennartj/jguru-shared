/*-
 * #%L
 * Nazgul Project: jguru-shared-spi-jpa
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
package se.jguru.shared.spi.jpa.converter

import java.time.DayOfWeek
import javax.persistence.AttributeConverter
import javax.persistence.Converter
import javax.xml.bind.annotation.XmlTransient

/**
 * JPA AttributeConverter class to handle Java 8 [java.time.DayOfWeek] - which
 * will convert to and from [Int]s.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@XmlTransient
@Converter(autoApply = true)
open class DayOfWeekAttributeConverter : AttributeConverter<DayOfWeek, Int> {

    override fun convertToDatabaseColumn(attribute: DayOfWeek?): Int? = when(attribute) {
        null -> null
        else -> attribute.value
    }

    override fun convertToEntityAttribute(dbData: Int?): DayOfWeek? = when(dbData) {
        null -> null
        else -> DayOfWeek.of(dbData)
    }
}