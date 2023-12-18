/*-
 * #%L
 * Nazgul Project: jguru-shared-algorithms-api
 * %%
 * Copyright (C) 2018 - 2023 jGuru Europe AB
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
package se.jguru.shared.algorithms.api.introspection

import java.io.Serializable

/**
 * ## Serializable Void type
 *
 * Type definition to be used whenever `java.lang.Void` is desired, but a `java.io.Serializable`
 * instance must be provided. An example of this situation is prevalent in clustering, when generic
 * methods are specified to return a value.  If `void` is desired as a return value, use this
 * SerializableVoid class instead
 *
 * > public interface SomeActorDefinition&lt;T extends Serializable&gt; extends Serializable { T perform(); }
 *
 * ### A concrete implementation
 *
 * If we desire a concrete implementation of the SomeActorDefinition, which should not return a value from the
 * perform method (which would require much more resources than a fully asynchronous call returning void) we must
 * declare the implementation like so:
 *
 * > public class AConcreteActor implements SomeActorDefinition&lt;SerializableVoid&gt; {
 * >        public SerializableVoid perform() {
 * >             // Do something
 * >            return null;
 * >         }
 * >    }
 *
 * The SerializableVoid should not be instantiated.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
sealed class SerializableVoid : Serializable {
}
