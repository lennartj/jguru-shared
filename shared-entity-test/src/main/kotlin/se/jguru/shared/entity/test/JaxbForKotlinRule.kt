package se.jguru.shared.entity.test

import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * jUnit Rule for running JAXB tests under Kotlin.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class JaxbForKotlinRule(val delegate : MarshallerAndUnmarshaller) : TestWatcher() {
}
