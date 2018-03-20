package se.jguru.shared.spi.jpa.people

import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException
import se.jguru.shared.spi.jpa.SimpleEntity
import se.jguru.shared.spi.jpa.StandardEntity
import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Version
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlType

/**
 * A simple model of a Beverage
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@StandardEntity
@XmlType(namespace = Beverage.NAMESPACE, propOrder = ["name"])
class Beverage @JvmOverloads constructor(

    @field:Basic(optional = false)
    @field:Column(nullable = false)
    @field:XmlAttribute(required = true)
    var name: String,

    id : Long = 0,
    version : Long = 0) : SimpleEntity(id, version), Comparable<Beverage> {

    override fun compareTo(other: Beverage): Int {
        return this.name.compareTo(other.name)
    }

    override fun validateInternalState() {
        InternalStateValidationException.create()
            .notNullOrEmpty(name, "name")
            .endExpressionAndValidate()
    }

    companion object {
        const val NAMESPACE = "http://some/good/beverage"
    }
}