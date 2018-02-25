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

import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Collections
import java.util.Comparator
import java.util.SortedSet
import java.util.function.Function
import java.util.function.Predicate
import javax.validation.constraints.NotNull

/**
 * Collection of network-related algorithms. The address definitions are :
 *
 * <table>
 *     <tr>
 *         <th>IP Protocol</th>
 *         <th>Link-Local address range</th>
 *         <th>Loopback addresses</th>
 *     </tr>
 *     <tr>
 *         <td>IPv4</td>
 *         <td><tt>169.254.0.1 -- 169.254.255.254</tt></td>
 *         <td><tt>127.0.0.0/8</tt>, typically <tt>127.0.0.1</tt></td>
 *     </tr>
 *     <tr>
 *         <td>IPv6</td>
 *         <td><tt>fe80::/10</tt>, but for compliance reasons <tt>fe80::/64</tt></td>
 *         <td><tt>::1</tt></td>
 *     </tr>
 * </table>
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 * @see <a href="https://en.wikipedia.org/wiki/Link-local_address">Wikipedia: Link-local address definition</a>
 * @see <a href="https://en.wikipedia.org/wiki/Loopback">Wikipedia: Loopback address definition</a>
 */
object NetworkAlgorithms {

    /**
     * Predicate identifying non-null IPv4 InetAddresses.
     */
    @JvmStatic
    val IPV4_FILTER = Predicate<InetAddress> { candidate -> candidate is Inet4Address }

    /**
     * Predicate identifying non-null IPv6 InetAddresses.
     */
    @JvmStatic
    val IPV6_FILTER = Predicate<InetAddress> { candidate -> candidate is Inet6Address }

    /**
     * Predicate identifying non-null LoopBackAddresses.
     */
    @JvmStatic
    val LOOPBACK_FILTER = Predicate<InetAddress> { candidate -> candidate.isLoopbackAddress }

    /**
     *
     * Predicate identifying non-null IPv4 InetAddress objects that are neither LinkLocal nor Loopback addresses.
     */
    @JvmStatic
    val PUBLIC_IPV4_FILTER = Predicate<Any> { candidate ->
        candidate is Inet4Address
            && !candidate.isLoopbackAddress
            && !candidate.isLinkLocalAddress
    }

    /**
     * Comparator for InetAddress objects; failsafe in the sense that it will convert null values
     * (on either side of the comparison) to empty strings before comparing the objects.
     * It is, therefore, recommended to use this Comparator only after filtering an InetAddress
     * collection for null objects.
     */
    @JvmStatic
    val INETADDRESS_COMPARATOR = { left: InetAddress?, right: InetAddress? ->

        // If any side is null, simply replace with an empty string.
        val lSide = left?.hostAddress ?: ""
        val rSide = right?.hostAddress ?: ""

        // All Done.
        lSide.compareTo(rSide)
    }

    /**
     * Compares NetworkInterface objects by their [NetworkInterface.toString] value.
     * This Comparator is failsafe in the sense that it converts null values
     * (on either side of the comparison) to empty strings before comparing the objects.
     * It is, therefore, recommended to use this Comparator only after removing null objects
     * from the respective source Collection.
     */
    @JvmStatic
    val NETWORK_INTERFACE_COMPARATOR = kotlin.Comparator<NetworkInterface> { l, r ->

        // Be paranoid
        val left = l?.toString() ?: ""
        val right = r?.toString() ?: ""

        // All Done.
        left.compareTo(right)
    }

    /**
     * Finds all non-broadcast InetAddresses from the supplied NetworkInterface.
     */
    @JvmStatic
    fun getInetAddresses(networkInterface: NetworkInterface): SortedSet<InetAddress> {

        val toReturn = java.util.TreeSet<InetAddress>(INETADDRESS_COMPARATOR)

        networkInterface.interfaceAddresses
            .stream()
            .map { it.address }
            .forEach { toReturn.add(it) }

        // All Done.
        return toReturn
    }

    /**
     * Converts the InetAddress to a Set containing all String forms of the InetAddress, namely:
     *
     *  1. [InetAddress.getCanonicalHostName]
     *  1. [InetAddress.getHostAddress]
     *  1. [InetAddress.getHostName]
     */
    @JvmStatic
    val GET_ALL_ADRESSES = Function<InetAddress, Set<String>> { addr: InetAddress? ->

        val toReturn = java.util.TreeSet<String>()

        if (addr != null) {
            toReturn.add(addr.canonicalHostName)
            toReturn.add(addr.hostAddress)
            toReturn.add(addr.hostName)
        }

        // All Done.
        toReturn
    }

    /**
     * Convenience method to find all public (i.e. non-loopback, non-linklocal) IPv4 addresses.
     *
     * @return A sorted set holding all Inet4Address objects which are neither loopback nor link-local
     * for all network interfaces on the local computer.
     * @see [INETADDRESS_COMPARATOR]
     * @see [getInetAddresses]
     * @see [PUBLIC_IPV4_FILTER]
     */
    @JvmStatic
    val publicIPv4Addresses: SortedSet<Inet4Address>
        @NotNull get() {

            val toReturn = java.util.TreeSet<Inet4Address>(INETADDRESS_COMPARATOR)

            NetworkAlgorithms.getAllNetworkInterfaces().forEach { c ->
                c.inetAddresses.toList()
                    .filter { IPV4_FILTER.test(it) }
                    .map { it as Inet4Address }
                    .forEach { toReturn.add(it) }
            }

            // All Done.
            return toReturn
        }

    /**
     * Retrieves a SortedSet containing all [NetworkInterface]s found.
     *
     * @param comparator A Comparator ordering the NetworkInterfaces found on the executing computer.
     * @return A SortedSet containing the NetworkInterfaces on the executing computer.
     * @throws IllegalStateException if the [NetworkInterface.getNetworkInterfaces] method fails.
     */
    @JvmOverloads
    @JvmStatic
    @NotNull
    @Throws(IllegalStateException::class)
    fun getAllNetworkInterfaces(comparator: Comparator<NetworkInterface> = NETWORK_INTERFACE_COMPARATOR)
        : SortedSet<NetworkInterface> {

        // Check sanity
        val toReturn = java.util.TreeSet<NetworkInterface>(comparator)

        try {
            toReturn.addAll(Collections.list(NetworkInterface.getNetworkInterfaces()))
        } catch (e: Exception) {
            throw IllegalStateException("Could not retrieve NetworkInterfaces", e)
        }

        // All Done.
        return toReturn
    }

    /**
     * Retrieves a set of string representations of the local [NetworkInterface]s found.
     *
     * @param addressFilter A filter applied to each [NetworkInterface] found.
     * @param addressMapper A Function applied to all [InetAddress] objects filtered by the addressFilter.
     * @return A SortedSet containing all String representations of the local [NetworkInterface]s found.
     */
    @JvmOverloads
    @JvmStatic
    @NotNull
    fun getAddressesFromAllNetworkInterfaces(
        addressFilter: Predicate<InetAddress> = IPV4_FILTER,
        addressMapper: Function<InetAddress, Set<String>> = GET_ALL_ADRESSES): SortedSet<String> {

        // Check sanity
        val toReturn = java.util.TreeSet<String>()

        for(addressList in getAllNetworkInterfaces().map { it.inetAddresses.toList() }) {

            addressList.stream()
                .filter(addressFilter)
                .map(addressMapper)
                .forEach { toReturn.addAll(it) }
        }

        // All Done.
        return toReturn
    }
}
