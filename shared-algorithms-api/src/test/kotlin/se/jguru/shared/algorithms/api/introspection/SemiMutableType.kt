package se.jguru.shared.algorithms.api.introspection

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
data class SemiMutableType(val immutableString : String,
                           var mutableString : String,
                           val immutableBoolean : Boolean,
                           var mutableBoolean : Boolean)