/*-
 * #%L
 * Nazgul Project: jguru-shared-persistence-spi-jpa
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

package se.jguru.shared.persistence.spi.jpa.converter

import java.time.DayOfWeek
import java.util.SortedSet
import java.util.StringTokenizer
import java.util.TreeSet
import javax.persistence.AttributeConverter
import javax.persistence.Converter
import javax.xml.bind.annotation.XmlTransient

/**
 * Converter for [DayOfWeek] Sets, compacted into a comma-separated String
 * representation, such as `1,2,4,5,6`.
 * 
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@XmlTransient
@Converter(autoApply = true)
open class DaysOfWeekAttributeConverter : AttributeConverter<SortedSet<DayOfWeek>, String> {

    override fun convertToDatabaseColumn(attribute: SortedSet<DayOfWeek>?): String? = when (attribute) {

        null -> null
        else -> attribute.map { a -> "${a.value}" }.reduce { sum, current -> "$sum,$current" }
    }

    override fun convertToEntityAttribute(dbData: String?): SortedSet<DayOfWeek> {

        val toReturn = TreeSet<DayOfWeek>()

        if(dbData != null) {

            val tokenizer = StringTokenizer(dbData, ",", false)
            while(tokenizer.hasMoreTokens()) {

                val currentToken = tokenizer.nextToken()

                try {
                    toReturn.add(DayOfWeek.of(Integer.parseInt(currentToken)));
                } catch (e: Exception) {

                    val permittedValues = DayOfWeek.values()
                        .sorted()
                        .map { a -> "${a.value}" }
                        .reduce { acc, current -> "$acc,$current" }

                    throw IllegalArgumentException("Could not interpret [$currentToken] as a DayOfWeek value. " +
                        "Permitted values are: " + permittedValues, e)
                }
            }
        }

        // All Done.
        return toReturn
    }
}
