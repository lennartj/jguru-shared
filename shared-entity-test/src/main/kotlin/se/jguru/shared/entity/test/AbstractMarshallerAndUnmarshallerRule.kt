/*-
 * #%L
 * Nazgul Project: jguru-shared-entity-test
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
package se.jguru.shared.entity.test

import org.junit.rules.TestWatcher
import org.junit.runner.Description
import se.jguru.shared.algorithms.api.xml.MarshallerAndUnmarshaller
import se.jguru.shared.algorithms.api.xml.NamespacePrefixResolver
import javax.xml.bind.JAXBContext

/**
 * Abstract [TestWatcher] implementation which wraps a [MarshallerAndUnmarshaller] to which all JAXB-related
 * calls are delegated. For most operations, simply use the public [delegate] member.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
abstract class AbstractMarshallerAndUnmarshallerRule(val delegate: MarshallerAndUnmarshaller,
                                                     private val jaxbContextFactoryClass : String) : TestWatcher() {

    // Internal state
    private var originalContextFactory: String? = null

    /**
     * The NamespacePrefixResolver used within this AbstractMarshallerAndUnmarshallerRule
     */
    val namespacePrefixResolver: NamespacePrefixResolver
        get() = delegate.namespacePrefixResolver

    // Internal state
    override fun starting(description: Description?) {

        // If we had a JAXB context factory propert set, ensure that we can restore it after the test.
        originalContextFactory = System.getProperty(JAXBContext.JAXB_CONTEXT_FACTORY)

        // Set the JAXB ContextFactory.
        // This is likely not required as the [delegate] uses a strictly typecast version of the JAXBContextFactory ...
        // ... but is done as a precaution.
        System.setProperty(JAXBContext.JAXB_CONTEXT_FACTORY, jaxbContextFactoryClass)
        super.starting(description)
    }

    override fun finished(description: Description?) {

        // First, delegate.
        super.finished(description)

        // Restore the JAXBContext
        when (originalContextFactory) {
            null -> System.clearProperty(JAXBContext.JAXB_CONTEXT_FACTORY)
            else -> System.setProperty(JAXBContext.JAXB_CONTEXT_FACTORY, originalContextFactory)
        }
    }
}
