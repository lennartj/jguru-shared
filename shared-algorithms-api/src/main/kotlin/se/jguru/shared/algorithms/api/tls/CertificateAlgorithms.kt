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
package se.jguru.shared.algorithms.api.tls

import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.cert.X509Certificate
import java.util.SortedMap
import java.util.TreeMap
import java.util.function.Predicate
import javax.naming.ldap.LdapName
import javax.naming.ldap.Rdn

/**
 * Default type for Java [KeyStore]s.
 */
const val JKS_KEYSTORE_TYPE = "JKS"

/**
 * The standard system property to define the location of the truststore ("cacerts") location.
 */
const val PROPERTY_SSL_TRUSTSTORE_PATH = "javax.net.ssl.trustStore"

/**
 * The standard system property to define the password of the truststore ("cacerts").
 */
const val PROPERTY_SSL_TRUSTSTORE_PASSWORD = "javax.net.ssl.trustStorePassword"

private val SEP = File.separator

// Our Logger
private val log = LoggerFactory.getLogger(CertificateAlgorithms::class.java)

/**
 * Specification regarding how to handle overwriting existing files.
 */
enum class OverwriteStrategy {

    OVERWRITE_EXISTING,

    NO_OVERWRITE_IF_EXISTS,

    EXCEPTION_IF_EXISTS
}

/**
 * Collection of algorithms that simplify working with SSL Certificates for Java usage.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
object CertificateAlgorithms {

    /**
     * The File to the standard JKS used by this JVM.
     */
    @JvmStatic
    val standardJavaKeyStore: File
        get() = when (System.getProperty(PROPERTY_SSL_TRUSTSTORE_PATH)) {
            null -> {

                // Define standard search locations
                fun getStandardLocation(injectJreDirectory: Boolean) = System.getProperty("java.home") +
                    when (injectJreDirectory) {
                        true -> "${SEP}jre"
                        false -> ""
                    } + "${SEP}lib${SEP}security${SEP}cacerts"

                // Wrap in a File, and check which ones exist.
                val jreLocation = File(getStandardLocation(false))
                val jdkLocation = File(getStandardLocation(true))

                // All Done. Return the first existing File location, or die trying.
                when {
                    jdkLocation.exists() -> jdkLocation
                    jreLocation.exists() -> jreLocation
                    else -> throw IllegalStateException("Standard Java KeyStore not found at either " +
                        "[${jdkLocation.path}] or [${jreLocation.path}]. Check sanity.")
                }
            }
            else -> File(System.getProperty(PROPERTY_SSL_TRUSTSTORE_PATH))
        }

    /**
     * The normal password of the standard JKS used by this JVM.
     */
    @JvmStatic
    val standardJavaKeyStorePassword: String
        get() = when (System.getProperty(PROPERTY_SSL_TRUSTSTORE_PASSWORD)) {
            null -> "changeit"
            else -> System.getProperty(PROPERTY_SSL_TRUSTSTORE_PASSWORD)
        }

    /**
     * Predicate accepting [KeyStore.TrustedCertificateEntry] entries which contains [X509Certificate]s.
     */
    @JvmStatic
    val IS_X509_CERTIFICATE = Predicate<KeyStore.Entry> {
        it is KeyStore.TrustedCertificateEntry && it.trustedCertificate is X509Certificate
    }

    /**
     * Predicate accepting [KeyStore.PrivateKeyEntry]s.
     */
    @JvmStatic
    val IS_PRIVATE_KEY = Predicate<KeyStore.Entry> {
        it is KeyStore.PrivateKeyEntry
    }

    /**
     * Predicate accepting [KeyStore.SecretKeyEntry]s.
     */
    @JvmStatic
    val IS_SECRET_KEY = Predicate<KeyStore.Entry> {
        it is KeyStore.SecretKeyEntry
    }

    /**
     * Creates an empty, in-memory KeyStore.
     *
     * @param storePassword The KeyStore's password.
     * @param storeType The type of KeyStore to be created.
     */
    @Throws(KeyStoreException::class)
    @JvmOverloads
    @JvmStatic
    fun createKeyStore(storePassword: String = standardJavaKeyStorePassword,
                       storeType: String = JKS_KEYSTORE_TYPE): KeyStore {

        val toReturn = KeyStore.getInstance(storeType)

        // Assign the KeyStore's password.
        toReturn.load(null, storePassword.toCharArray())

        // All Done.
        return toReturn
    }

    /**
     * Loads the [KeyStore] from the supplied URL using the given storePassword and type.
     *
     * @param storePassword The password of the JKS store. Defaults to [standardJavaKeyStorePassword].
     * @param storeType The [KeyStore] type. Defaults to [JKS_KEYSTORE_TYPE].
     * @param keystoreURL The URL where the [KeyStore] can be found. Defaults to the URL of [standardJavaKeyStore].
     * @return the fully loaded [KeyStore].
     */
    @JvmOverloads
    @JvmStatic
    fun loadKeyStore(storePassword: String = standardJavaKeyStorePassword,
                     storeType: String = JKS_KEYSTORE_TYPE,
                     keystoreURL: URL = standardJavaKeyStore.toURI().toURL()): KeyStore {

        val toReturn = createKeyStore(storePassword, storeType)
        toReturn.load(keystoreURL.openStream(), storePassword.toCharArray())

        // All Done.
        return toReturn
    }

    /**
     * Retrieves the KeyStore connected to the JDK's (standard) Java [KeyStore], found at [standardJavaKeyStore] File.
     *
     * @return The standard Java KeyStore.
     */
    @JvmStatic
    fun getStandardJKS(): KeyStore = loadKeyStore()

    /**
     * Persists the supplied KeyStore to the given File.
     *
     * @param store The KeyStore to persist
     * @param file The file where to persist the KeyStore
     * @param storePassword The KeyStore's password.
     * @param overwrite The definition of how to handle an already existing KeyStore at the given File path.
     */
    @JvmOverloads
    @JvmStatic
    fun persistKeyStore(store: KeyStore,
                        file: File,
                        storePassword: String = standardJavaKeyStorePassword,
                        overwrite: OverwriteStrategy = OverwriteStrategy.EXCEPTION_IF_EXISTS) {

        // #1) Check sanity. Does the keystore file exist?
        //
        if (file.exists()) {

            when (overwrite) {
                OverwriteStrategy.EXCEPTION_IF_EXISTS -> throw IllegalArgumentException(
                    "Refusing to overwrite existing KeyStore [${file.path}], as instructed.")
                OverwriteStrategy.NO_OVERWRITE_IF_EXISTS -> {

                    if (log.isDebugEnabled) {
                        log.debug("Not overwriting existing KeyStore [${file.path}], as instructed.")
                    }

                    return
                }
                OverwriteStrategy.OVERWRITE_EXISTING -> {
                    if (log.isDebugEnabled) {
                        log.debug("Overwriting existing KeyStore [${file.path}], as instructed.")
                    }
                }
            }
        }

        // #2) Write the KeyStore file.
        //
        store.store(FileOutputStream(file), storePassword.toCharArray());
    }

    /**
     * Creates a Map relating entry alias to [KeyStore.Entry] for all entries within the supplied
     * store, where the [entryFilter] function yields true.
     *
     * @param store The [KeyStore] from which the entries should be extracted.
     * @param entryAlias2PasswordMap A Map relating aliases to passwords. The values are optional, and if a
     * password is given for a specific entry, it is applied when extracting the Entry from the KeyStore.
     * @param entryFilter A Predicate which includes [KeyStore.Entry]s within the result if yielding true.
     *
     * @return a Map relating entry alias to [KeyStore.Entry] for accepted entries.
     */
    @JvmOverloads
    @JvmStatic
    fun getEntryMapFrom(store: KeyStore = getStandardJKS(),
                        entryAlias2PasswordMap: Map<String, String> = emptyMap(),
                        entryFilter: Predicate<KeyStore.Entry> = Predicate { true })
        : SortedMap<String, KeyStore.Entry> {

        val toReturn = TreeMap<String, KeyStore.Entry>()

        store.aliases()
            .toList()
            .forEach { alias ->

                // Explicit password given for this alias?
                val currentPassword = entryAlias2PasswordMap[alias]

                // If so, create a PasswordProtection object.
                val currentProtectionParameter = when (currentPassword) {
                    null -> null
                    else -> KeyStore.PasswordProtection(currentPassword.toCharArray())
                }

                // Extract the entry
                val currentEntry = store.getEntry(alias, currentProtectionParameter)

                // If we want the entry, map it in the return value
                if (entryFilter.test(currentEntry)) {
                    toReturn[alias] = currentEntry
                }
            }

        // All Done
        return toReturn
    }

    /**
     * Parses either of the the subject DN or issuer DN into a List of [Rdn] (which are Key - Value holders).
     *
     * @param cert The X509Certificate from which to extract certificate details.
     * @param useSubject if true, use the Subject x500 Principal within the certificate to extract the RDNs from.
     * Otherwise use the Issuer x500 Principal.
     * @return A List containing all RDNs from the given area of the X509Certificate.
     */
    @JvmStatic
    fun getRelativeDistinguishedNamesFor(cert: X509Certificate, useSubject: Boolean = true): MutableList<Rdn> {

        val x509Principal = when {
            useSubject -> cert.subjectX500Principal
            else -> cert.issuerX500Principal
        }

        // All Done.
        return LdapName(x509Principal.name).rdns
    }
}
