package se.jguru.shared.algorithms.api

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
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

    @Before
    fun setupSharedState() {

        platformServer = JmxAlgorithms.getPlatformServer()

        Assert.assertNotNull(platformServer)
    }

    @After
    fun removeBoundMXBeans() {

        val jmxDomain = FooBarMXBean::class.java.`package`.name

        val namesInDomain = JmxAlgorithms.getNamesInDomain(jmxDomain)
        if (namesInDomain.isEmpty()) {

            System.out.println("No JMX names found in domain [$jmxDomain]. Not unbinding.")

        } else {

            for (current in namesInDomain) {

                print("Unbinding [$current] ... ")
                try {
                    JmxAlgorithms.getPlatformServer().unregisterMBean(current)
                    println("Done!")
                } catch (e: Exception) {
                    println("Failed! Caused by:\n" + e)
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

        Assert.assertNotNull(memoryMBeanObjects)
        Assert.assertEquals(1, memoryMBeanObjects.size)
        val memoryMxBean = memoryMBeanObjects.first()

        // Act
        val mBeanInterfaceName = JmxAlgorithms.getMBeanInterfaceName(memoryMxBean.objectName)
        val mxBeanProxy = JmxAlgorithms.getMXBeanProxy(MemoryMXBean::class.java, memoryMxBean.objectName)

        // Assert
        Assert.assertEquals(MemoryMXBean::class.java.name, mBeanInterfaceName)
        Assert.assertTrue(MemoryMXBean::class.java.isAssignableFrom(mxBeanProxy::class.java))

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
        Assert.assertEquals(expectedDomain, result.getDomain())
        Assert.assertEquals("se.jguru.shared.algorithms.api.jmx:jmxInterfaceType=FooBarMXBean,foo=bar",
            result.toString())
        Assert.assertEquals(interfaceType.simpleName, result.getKeyProperty(JmxAlgorithms.JMX_INTERFACE_TYPE))
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
        Assert.assertNotNull(mxBean)
        Assert.assertNotSame(mxBean, theImpl)
        Assert.assertEquals("newBar", mxBean.getBar())
        Assert.assertEquals("foo!", mxBean.getFoo())
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
                Assert.assertEquals(FooBarImpl::class.java.name, directObjectInstance.className)

                // Call a method within the Proxy, and stash the results
                name2ObjectName[key] = current
                objectName2Bar[current] = currentProxy.getBar()
            }

        // Assert
        Assert.assertEquals(2, name2ObjectName.size.toLong())
        Assert.assertEquals(2, objectName2Bar.size.toLong())

        objectName2Bar.forEach { k, v ->

            val qualifier = k.keyPropertyList[qualifierKey]
            Assert.assertNotNull(qualifier)
            Assert.assertEquals(qualifier, v)

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