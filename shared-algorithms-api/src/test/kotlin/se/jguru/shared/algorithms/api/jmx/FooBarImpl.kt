package se.jguru.shared.algorithms.api.jmx

/**
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
class FooBarImpl(private var bar : String = "bar") : FooBarMXBean {

    override fun getFoo(): String {
        return "foo!"
    }

    override fun getBar(): String {
        return bar
    }

    override fun setBar(aBar: String) {
        this.bar = aBar
    }
}