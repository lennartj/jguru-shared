package se.jguru.shared.algorithms.api.jmx

import javax.management.MXBean

/**
 * The JMX interface spec.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@MXBean
interface FooBarMXBean {

    fun getFoo(): String

    fun getBar(): String

    fun setBar(aBar: String)
}