package se.jguru.shared.algorithms.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import se.jguru.shared.algorithms.api.jmx.FooBarImpl
import se.jguru.shared.algorithms.api.jmx.FooBarMXBean
import java.lang.management.MemoryMXBean
import java.util.TreeMap
import javax.management.MBeanServer
import javax.management.ObjectInstance
import javax.management.ObjectName

/**
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
class JmxAlgorithmsTest {

    // Shared state
    lateinit var platformServer: MBeanServer

    @BeforeEach
    fun setupSharedState() {

        platformServer = JmxAlgorithms.getPlatformServer()

        assertThat(platformServer).isNotNull
    }

    @AfterEach
    fun removeBoundMXBeans() {

        val jmxDomain = FooBarMXBean::class.java.`package`.name

        val namesInDomain = JmxAlgorithms.getNamesInDomain(jmxDomain)
        if (namesInDomain.isEmpty()) {

            println("No JMX names found in domain [$jmxDomain]. Not unbinding.")

        } else {

            for (current in namesInDomain) {

                print("Unbinding [$current] ... ")
                try {
                    JmxAlgorithms.getPlatformServer().unregisterMBean(current)
                    println("Done!")
                } catch (e: Exception) {
                    println("Failed! Caused by:\n$e")
                }
            }
        }
    }


    @Test
    fun validateGettingDomains() {

        /*
java.util.logging:type=Logging
java.lang:type=OperatingSystem
java.lang:type=MemoryManager,name=Metaspace Manager
java.lang:type=MemoryPool,name=Metaspace
java.lang:type=MemoryPool,name=PS Old Gen
java.lang:type=ClassLoading
java.lang:type=Runtime
java.lang:type=GarbageCollector,name=PS Scavenge
java.lang:type=Threading
java.lang:type=MemoryManager,name=CodeCacheManager
java.lang:type=MemoryPool,name=PS Eden Space
java.lang:type=MemoryPool,name=Code Cache
java.lang:type=MemoryPool,name=Compressed Class Space
java.lang:type=MemoryPool,name=PS Survivor Space
java.lang:type=GarbageCollector,name=PS MarkSweep
java.lang:type=Memory
java.lang:type=Compilation
         */

        // Assemble
        val memoryMBeanObjects: Set<ObjectInstance> = JmxAlgorithms.getPlatformServer()
            .queryMBeans(ObjectName("*:type=Memory"), null)

        assertThat(memoryMBeanObjects).isNotNull
        assertThat(memoryMBeanObjects.size).isEqualTo(1)
        val memoryMxBean = memoryMBeanObjects.first()

        // Act
        val mBeanInterfaceName = JmxAlgorithms.getMBeanInterfaceName(memoryMxBean.objectName)
        val mxBeanProxy = JmxAlgorithms.getMXBeanProxy(MemoryMXBean::class.java, memoryMxBean.objectName)

        // Assert
        assertThat(mBeanInterfaceName).isEqualTo(MemoryMXBean::class.java.name)
        assertThat(MemoryMXBean::class.java).isAssignableFrom(mxBeanProxy::class.java)

        /*
        for (aDomain in someDomains) {
            JmxAlgorithms.getMBeansInDomain(aDomain).map { it.objectName }.forEach { println(it) }
        }
        */
    }

    @Test
    fun validateSynthesizingObjectNames() {

        // Assemble
        val interfaceType = FooBarMXBean::class.java
        val expectedDomain = interfaceType.`package`.name
        val extraProps = TreeMap<String, String>()
        extraProps["foo"] = "bar"

        // Act
        val result = JmxAlgorithms.getNaturalObjectNameFor(interfaceType, extraProps)
        // println("Got result: " + result);

        // Assert
        assertThat(result.domain).isEqualTo(expectedDomain)
        assertThat(result.toString()).isEqualTo("se.jguru.shared.algorithms.api.jmx:jmxInterfaceType=FooBarMXBean,foo=bar")
        assertThat(result.getKeyProperty(JmxAlgorithms.JMX_INTERFACE_TYPE)).isEqualTo(interfaceType.simpleName)
    }

    @Test
    fun validateGettingBoundMXBean() {

        // Assemble
        val newBar = "newBar"
        val theImpl = FooBarImpl()
        theImpl.setBar(newBar)

        // Act
        JmxAlgorithms.registerMXBean(FooBarMXBean::class.java, theImpl)

        val mxBean = JmxAlgorithms.getMXBeanProxy(
            FooBarMXBean::class.java,
            JmxAlgorithms.getNaturalObjectNameFor(FooBarMXBean::class.java))

        // Assert
        assertThat(mxBean).isNotNull
        assertThat(theImpl).isNotSameAs(mxBean)
        assertThat(mxBean.getBar()).isEqualTo("newBar")
        assertThat(mxBean.getFoo()).isEqualTo("foo!")
    }

    @Test
    fun validateDifferentiatingBetweenBoundMXBeans() {

        // Assemble
        val jmxDomain = FooBarMXBean::class.java.`package`.name
        val qualifierKey = "qualifier"
        val impl1 = FooBarImpl()
        impl1.setBar("impl1")
        val impl1Properties = getSingleEntryMap(qualifierKey, "impl1")

        val impl2 = FooBarImpl()
        impl2.setBar("impl2")
        val impl2Properties = getSingleEntryMap(qualifierKey, "impl2")

        JmxAlgorithms.registerMXBean(FooBarMXBean::class.java, impl1, impl1Properties)
        JmxAlgorithms.registerMXBean(FooBarMXBean::class.java, impl2, impl2Properties)

        // Act
        val namesInDomain = JmxAlgorithms.getNamesInDomain(jmxDomain)
        val mBeansInDomain = JmxAlgorithms.getMBeansInDomain(jmxDomain)
        val name2ObjectName = TreeMap<String, ObjectName>()
        val objectName2Bar = TreeMap<ObjectName, String>()

        namesInDomain.stream()
            .filter { it != null }
            .forEach { current ->

                // Dig out the JMX Proxy
                val key = current.toString()
                val currentProxy = JmxAlgorithms.getMXBeanProxy(FooBarMXBean::class.java, current)

                // Filter out the corresponding ObjectInstance, and validate its ClassName.
                val directObjectInstance = mBeansInDomain.stream()
                    .filter({ obj -> obj.objectName == current })
                    .findFirst()
                    .orElse(null)
                assertThat(directObjectInstance.className).isEqualTo(FooBarImpl::class.java.name)

                // Call a method within the Proxy, and stash the results
                name2ObjectName[key] = current
                objectName2Bar[current] = currentProxy.getBar()
            }

        // Assert
        assertThat(name2ObjectName.size.toLong()).isEqualTo(2)
        assertThat(objectName2Bar.size.toLong()).isEqualTo(2)

        objectName2Bar.forEach { (k, v) ->

            val qualifier = k.keyPropertyList[qualifierKey]
            assertThat(qualifier).isNotNull
            assertThat(v).isEqualTo(qualifier)

            println("Domain: " + k.domain
                + ", Canonical Name: " + k.canonicalName
                + ", Key Property List: " + k.keyPropertyList)
        }
    }


    //
    // Private helpers
    //

    private fun getSingleEntryMap(key: String, value: String): Map<String, String> {

        val toReturn = TreeMap<String, String>()
        toReturn[key] = value
        return toReturn
    }

}