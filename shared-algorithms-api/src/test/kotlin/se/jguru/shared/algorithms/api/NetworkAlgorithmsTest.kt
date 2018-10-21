package se.jguru.shared.algorithms.api

import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.SortedSet
import java.util.function.Consumer

/**
 *
 * @author [Lennart Jörelid](mailto:lj@jguru.se), jGuru Europe AB
 */
@Ignore("The network tests are really slow on Mac OS X")
class NetworkAlgorithmsTest {

    // Shared state
    private var allLocalInterfaces: SortedSet<NetworkInterface>? = null
    private var allLocalIPv4Addresses: SortedSet<InetAddress>? = null
    private var allLocalIPv6Addresses: SortedSet<InetAddress>? = null
    private var foundLocalNetworkInterfaces: Boolean = false
    private var foundLocalIPv4Addresses: Boolean = false
    private var foundPublicIPv4Addresses: Boolean = false
    private var foundLocalIPv6Addresses: Boolean = false

    @Before
    fun setupSharedState() {

        allLocalInterfaces = NetworkAlgorithms.getAllNetworkInterfaces()
        allLocalIPv4Addresses = java.util.TreeSet<InetAddress>(NetworkAlgorithms.INETADDRESS_COMPARATOR)
        allLocalIPv6Addresses = java.util.TreeSet<InetAddress>(NetworkAlgorithms.INETADDRESS_COMPARATOR)
        foundLocalNetworkInterfaces = !allLocalInterfaces!!.isEmpty()

        if (foundLocalNetworkInterfaces) {

            // Harvest all IPv4 InetAddresses
            allLocalInterfaces!!.stream()
                .map { NetworkAlgorithms.getInetAddresses(it) }
                .forEach { c ->
                    c.stream()
                        .filter(NetworkAlgorithms.IPV4_FILTER)
                        .forEach { allLocalIPv4Addresses!!.add(it) }
                }

            // Harvest all IPv6 InetAddresses
            allLocalInterfaces!!.stream()
                .map { NetworkAlgorithms.getInetAddresses(it) }
                .forEach { c ->
                    c.stream()
                        .filter(NetworkAlgorithms.IPV6_FILTER)
                        .forEach { allLocalIPv6Addresses!!.add(it) }
                }
        }

        foundLocalIPv4Addresses = allLocalIPv4Addresses!!.isNotEmpty()
        foundLocalIPv6Addresses = allLocalIPv6Addresses!!.isNotEmpty()
        foundPublicIPv4Addresses = allLocalIPv4Addresses!!.filter { !it.isLoopbackAddress }.any()
    }

    @Test
    fun validatePublicIpV4Filter() {

        if (foundPublicIPv4Addresses) {

            // Assemble
            val networkIFs = NetworkAlgorithms
                .getAllNetworkInterfaces(NetworkAlgorithms.NETWORK_INTERFACE_COMPARATOR)

            // Act
            val publicIpV4Addresses = java.util.TreeSet<Inet4Address>(NetworkAlgorithms.INETADDRESS_COMPARATOR)
            networkIFs.stream()
                .map { NetworkAlgorithms.getInetAddresses(it) }
                .forEach { ifs ->
                    ifs.stream()
                        .filter(NetworkAlgorithms.PUBLIC_IPV4_FILTER)
                        .map { ip -> ip as Inet4Address }
                        .forEach { publicIpV4Addresses.add(it) }
                }

            val sortedIFs = java.util.TreeSet<String>()
            publicIpV4Addresses.stream()
                .map(NetworkAlgorithms.GET_ALL_ADRESSES)
                .forEach { sortedIFs.addAll(it) }

            // Assert
            Assert.assertFalse(sortedIFs.isEmpty())
            validateAdresses(publicIpV4Addresses, false, false)
        }
    }

    @Test
    fun validateLoopbackIpV4Filter() {

        if (foundLocalIPv4Addresses) {

            // Assemble
            val networkIFs = NetworkAlgorithms
                .getAllNetworkInterfaces(NetworkAlgorithms.NETWORK_INTERFACE_COMPARATOR)

            // Act
            val ipV4Addresses = java.util.TreeSet<Inet4Address>(NetworkAlgorithms.INETADDRESS_COMPARATOR)
            networkIFs.stream()
                .map { NetworkAlgorithms.getInetAddresses(it) }
                .forEach { ifs ->
                    ifs.stream()
                        .filter(NetworkAlgorithms.LOOPBACK_FILTER)
                        .filter { candidate -> !candidate.isLinkLocalAddress }
                        .filter(NetworkAlgorithms.IPV4_FILTER)
                        .map { ip -> ip as Inet4Address }
                        .forEach { ipV4Addresses.add(it) }
                }

            val sortedIFs = java.util.TreeSet<String>()
            ipV4Addresses.stream()
                .map(NetworkAlgorithms.GET_ALL_ADRESSES)
                .forEach { sortedIFs.addAll(it) }

            // Assert
            Assert.assertFalse(sortedIFs.isEmpty())
            validateAdresses(ipV4Addresses, true, false)
        }
    }

    @Test
    fun validateNonLoopbackIpV6Filter() {

        if (foundLocalIPv6Addresses) {

            // Assemble
            val networkIFs = NetworkAlgorithms.getAllNetworkInterfaces()

            // Act
            val ipV6Addresses = java.util.TreeSet<Inet6Address>(NetworkAlgorithms.INETADDRESS_COMPARATOR)

            networkIFs.stream()
                .map { NetworkAlgorithms.getInetAddresses(it) }
                .forEach { ifs ->
                    ifs.stream()
                        .filter(NetworkAlgorithms.IPV6_FILTER)
                        .filter { candidate -> candidate != null && !candidate.isLoopbackAddress }
                        .map { ip -> ip as Inet6Address }
                        .forEach { ipV6Addresses.add(it) }
                }

            val sortedIFs = java.util.TreeSet<String>()
            ipV6Addresses.stream()
                .map(NetworkAlgorithms.GET_ALL_ADRESSES)
                .forEach(Consumer<Set<String>> { sortedIFs.addAll(it) })

            // Assert
            Assert.assertFalse(sortedIFs.isEmpty())
            validateAdresses(ipV6Addresses, false, true)

            // System.out.println("Got sorted IPv6 addresses: " + ipV6Addresses);
        }
    }

    //
    // Private helpers
    //

    private fun validateAdresses(inetAddresses: SortedSet<out InetAddress>,
                                 shouldBeLoopback: Boolean,
                                 shouldContainLinkLocalAddress: Boolean) {

        Assert.assertFalse(inetAddresses.isEmpty())
        Assert.assertTrue(inetAddresses.size <= allLocalInterfaces!!.size)

        inetAddresses.forEach { inetAddress ->

            if (shouldBeLoopback) {
                Assert.assertTrue(inetAddress.isLoopbackAddress)
            } else {
                Assert.assertFalse(inetAddress.isLoopbackAddress)
            }
        }
        inetAddresses.forEach { inetAddress -> Assert.assertFalse(inetAddress.isAnyLocalAddress) }
        inetAddresses.forEach { inetAddress -> Assert.assertFalse(inetAddress.isMulticastAddress) }

        // Link-local unicast in IPv4 (169.254.0.0/16)
        if (!shouldContainLinkLocalAddress) {
            inetAddresses.forEach { inetAddress ->
                Assert.assertFalse("Address [$inetAddress] is LinkLocal",
                    inetAddress.isLinkLocalAddress)
            }
        } else {
            Assert.assertTrue(inetAddresses.stream().anyMatch { it.isLinkLocalAddress })
        }

        // System.out.println("Got: " + sortedIFs);
    }
}