package se.jguru.shared.persistence.test.jpa.db

import java.io.Serializable
import java.util.SortedMap

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
interface DatabaseInfo : Serializable {

    fun getJdbcURL(parameters: SortedMap<String, Any>): String
}