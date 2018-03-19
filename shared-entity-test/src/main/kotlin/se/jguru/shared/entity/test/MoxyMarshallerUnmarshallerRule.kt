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
import se.jguru.shared.spi.jaxb.eclipselink.MoxyMarshallerAndUnmarshaller
import javax.xml.bind.JAXBContext

/**
 * jUnit Rule for running JAXB tests using the [MoxyMarshallerAndUnmarshaller] under Kotlin.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class MoxyMarshallerUnmarshallerRule(val delegate: MoxyMarshallerAndUnmarshaller = MoxyMarshallerAndUnmarshaller())
    : TestWatcher() {

    // Internal state
    lateinit var originalContextFactory : String

    // Internal state
    override fun starting(description: Description?) {
        System.setProperty(JAXBContext.JAXB_CONTEXT_FACTORY, "org.eclipse.persistence.jaxb.JAXBContextFactory")
        super.starting(description)
    }

    override fun finished(description: Description?) {
        super.finished(description)
    }

    override fun failed(e: Throwable?, description: Description?) {
        super.failed(e, description)
    }
}
