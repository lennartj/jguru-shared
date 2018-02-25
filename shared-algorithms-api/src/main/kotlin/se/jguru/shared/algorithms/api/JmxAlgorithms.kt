/*-
 * #%L
 * Nazgul Project: jguru-shared-algorithms-api
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
package se.jguru.shared.algorithms.api

import org.slf4j.LoggerFactory
import java.lang.management.ManagementFactory
import java.util.Hashtable
import javax.management.JMX
import javax.management.MBeanServer
import javax.management.MalformedObjectNameException
import javax.management.ObjectInstance
import javax.management.ObjectName
import javax.management.QueryExp


/**
 * Collection of algorithms that simplify working with JMX, typically MXBeans, to enhance and harmonize monitoring
 * and application-level statistics.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
object JmxAlgorithms {

    // Our Logger
    private val log = LoggerFactory.getLogger(JmxAlgorithms::class.java)

    /**
     * The standard JMX property for the Interface type.
     */
    const val JMX_INTERFACE_TYPE = "jmxInterfaceType"

    /**
     * The descriptor field name of the MXBean class name for an [ObjectName]
     */
    const val JMX_INTERFACE_TYPENAME = "interfaceClassName"

    /**
     * Retrieves the platform MBeanServer.
     *
     * @return The Platform MBeanServer.
     */
    @JvmStatic
    fun getPlatformServer(): MBeanServer = ManagementFactory.getPlatformMBeanServer()

    /**
     * Synthesizes a JMX ObjectName for the given MXBean interface.
     * Also indicates which system it is
     *
     * @param interfaceType The public interface type of the MXBean for which to retrieve an ObjectName.
     * @param properties    An optional (i.e. nullable) Map containing JMX properties for the ObjectName.
     * @param <T>           The type of the MXBean interface.
     * @return The ObjectName synthesized from the supplied InterfaceType.
     */
    @JvmStatic
    @JvmOverloads
    fun <T> getNaturalObjectNameFor(interfaceType: Class<T>,
                                    properties: Map<String, String> = mutableMapOf()): ObjectName {

        // Convert the inbound properties, since JMX requires a Hashtable.
        //
        val props = Hashtable<String, String>()
        props.putAll(properties)

        // Add the interface type
        props[JMX_INTERFACE_TYPE] = interfaceType.simpleName

        try {
            return ObjectName(interfaceType.`package`.name, props)
        } catch (e: MalformedObjectNameException) {
            throw IllegalArgumentException("Could not create JMX ObjectName", e)
        }
    }

    /**
     * Registers an MXBean using the supplied JMX public type (i.e. the MXBean interface) and
     * the supplied implementation (object).
     *
     * @param mxBeanType           The public interface type of the MXBean to register.
     * @param mxBeanImplementation The actual JMX object implementation.
     * @param jmxAttributes        An optional (i.e. nullable) Map containing ObjectName properties for use in
     * binding the MXBean within the JMX Platform server.
     * @param <T>                  The MXBean interface type, which must pass the `JMX.isMXBeanInterface(mxBeanType)`
     * method to be compliant.
     * @param <I>                  The MXBean object to register, which must implement the supplied JMX type.
     * @return The JMX ObjectInstance to register.
    </I></T> */
    @JvmStatic
    @JvmOverloads
    @Throws(IllegalArgumentException::class)
    fun <T, I : T> registerMXBean(mxBeanType: Class<T>,
                                  mxBeanImplementation: I,
                                  jmxAttributes: Map<String, String> = mutableMapOf()): ObjectInstance {

        // Check sanity
        if (!JMX.isMXBeanInterface(mxBeanType)) {
            throw IllegalArgumentException("Class [$mxBeanType] was not an MXBean interface.")
        }

        try {

            // Register the bean using its "natural" JMX ObjectName
            val toReturn = getPlatformServer().registerMBean(
                mxBeanImplementation,
                getNaturalObjectNameFor(mxBeanType, jmxAttributes))

            if (log.isDebugEnabled) {
                log.debug("Registered JMX ObjectInstance '" + toReturn.toString() + "'")
            }

            // All Done.
            return toReturn

        } catch (e: Exception) {
            throw IllegalArgumentException("Could not register MXBean. ", e)
        }
    }

    /**
     * Retrieves an MXBean Proxy for the supplied interfaceType within the local/platform MBean server.
     *
     * @param interfaceType The type of interface
     * @param objectName    The object name to use to retrieve the MXBean from the MBeanServer.
     * @param <T>           The type of interface for the MXBean.
     * @return The T object retrieved from JMX.
     */
    @JvmStatic
    fun <T> getMXBeanProxy(interfaceType: Class<T>, objectName: ObjectName): T = JMX.newMXBeanProxy(
        getPlatformServer(), objectName, interfaceType, true)

    /**
     * Retrieves a Set containing the [ObjectInstance]s within the supplied jmxDomain.
     *
     * @param jmxDomain The JMX domain for which we should retrieve [ObjectInstance]s.
     * @param queryExp The JMX query expression - or null to retrieve all MBeans within the jmxDomain.
     * @return A set of [ObjectInstance]s for the objects within the supplied domain.
     */
    @JvmStatic
    @JvmOverloads
    fun getMBeansInDomain(jmxDomain: String, queryExp: QueryExp? = null): Set<ObjectInstance> =
        getPlatformServer().queryMBeans(getSearchObjectNameFor(jmxDomain), queryExp)

    /**
     * Retrieves a Set containing the [ObjectName]s within the supplied jmxDomain.
     *
     * @param jmxDomain The JMX domain for which we should retrieve [ObjectName]s.
     * @return A Set of [ObjectName]s for the objects within the supplied domain.
     */
    @JvmStatic
    @JvmOverloads
    fun getNamesInDomain(jmxDomain: String, queryExp: QueryExp? = null): Set<ObjectName> = getPlatformServer()
        .queryNames(getSearchObjectNameFor(jmxDomain), queryExp)

    /**
     * Fetches the MXBean (or MBean) interface which is the public API of the supplied [ObjectName]
     */
    @JvmStatic
    fun getMBeanInterfaceName(objectName: ObjectName) : String {

        val info = JmxAlgorithms.getPlatformServer().getMBeanInfo(objectName)
        return "" + info.descriptor.getFieldValue(JMX_INTERFACE_TYPENAME)
    }

    //
    // Private helpers
    //

    @JvmStatic
    private fun getSearchObjectNameFor(jmxDomain: String): ObjectName {

        try {
            return ObjectName(jmxDomain + ":*")
        } catch (ex: MalformedObjectNameException) {
            throw IllegalArgumentException("Could not create ObjectName for jmxDomain '$jmxDomain'", ex)
        }
    }
}
