package se.jguru.shared.jaxb.spi.eclipselink.people

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlType

/**
 * A simple model of a Beverage
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@XmlType(namespace = Beverage.NAMESPACE, propOrder = ["name"])
@XmlAccessorType(XmlAccessType.FIELD)
class Beverage(var name: String) : Comparable<Beverage> {

    /**
     * JAXB-friendly constructor
     */
    constructor() : this("unknown")

    override fun compareTo(other: Beverage): Int {
        return this.name.compareTo(other.name)
    }

    companion object {

        const val NAMESPACE = "http://some/good/beverage"
    }
}