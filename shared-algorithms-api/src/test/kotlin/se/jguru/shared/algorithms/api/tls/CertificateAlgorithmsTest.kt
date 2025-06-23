package se.jguru.shared.algorithms.api.tls

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.cert.X509Certificate
import java.util.Objects
import java.util.function.Predicate
import javax.naming.ldap.Rdn

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class CertificateAlgorithmsTest {

    @Test
    fun validateCreatingJksKeystore() {

        // Assemble
        val passwd = "foobats"

        // Act
        val result = CertificateAlgorithms.createKeyStore(passwd)

        // Assert
        assertThat(result).isNotNull
        assertThat(result.type).isEqualTo(JKS_KEYSTORE_TYPE)
        assertThat(result.size()).isEqualTo(0)
    }

    @Test
    fun validateExceptionOnCreatingUnknownKeystoreType() {

        // Act & Assert
        assertThatExceptionOfType(KeyStoreException::class.java)
            .isThrownBy {
                CertificateAlgorithms.createKeyStore("foobats", "unknownType")
            }
    }

    @Test
    fun validateLoadingStandardKeystore() {

        // Assemble

        // Act
        val result1 = CertificateAlgorithms.loadKeyStore()
        val result2 = CertificateAlgorithms.getStandardJKS()

        // Assert
        assertThat(result1).isNotNull
        assertThat(result2).isNotNull

        assertThat(result1.type).isEqualTo(JKS_KEYSTORE_TYPE)
        assertThat(result2.type).isEqualTo(JKS_KEYSTORE_TYPE)

        assertThat(result1.size()).isGreaterThan(0)
        assertThat(result1.size()).isEqualTo(result2.size())
    }

    @Test
    fun validateGettingEntryMap() {

        // Assemble
        val standardJKS = CertificateAlgorithms.loadKeyStore()

        // Act
        val result = CertificateAlgorithms.getEntryMapFrom(standardJKS)

        // Assert
        assertThat(standardJKS).isNotNull
        assertThat(result).isNotNull

        result
            .filter { CertificateAlgorithms.IS_X509_CERTIFICATE.test(it.value) }
            .forEach { (key, value) ->

                val certEntry = (value as KeyStore.TrustedCertificateEntry).trustedCertificate as X509Certificate
                val certificateClassName = certEntry::class.java.simpleName

                val desc = StringBuffer()

                desc.append("\n Issuer Name: " +
                    CertificateAlgorithms.getRelativeDistinguishedNamesFor(certEntry, false)
                        .map { "[${it.type}]: ${it.value}" }
                        .reduce { sum, c -> "$sum, $c" })
                desc.append("\n Subject Name: " +
                    CertificateAlgorithms.getRelativeDistinguishedNamesFor(certEntry, true)
                        .map { "[${it.type}]: ${it.value}" }
                        .reduce { sum, c -> "$sum, $c" })
                desc.append("\n Algorithm : " + certEntry.sigAlgName)


                val attributes = when (value.attributes.size) {
                    0 -> ""
                    else -> "[" + value.attributes.stream()
                        .filter(Objects::nonNull)
                        .map { "${it.name}: ${it.value}" }.reduce { l: String?, r: String? -> "$l,$r" } + "]"
                }

                // Uncomment to print out a blurb on each Certificate.
                // Something like:
                //
                // addtrustexternalca [jdk]: X509CertImpl ()
                //      Issuer Name: [C]: SE, [O]: AddTrust AB, [OU]: AddTrust External TTP Network, [CN]: AddTrust External CA Root
                //      Subject Name: [C]: SE, [O]: AddTrust AB, [OU]: AddTrust External TTP Network, [CN]: AddTrust External CA Root
                //      Algorithm : SHA1withRSA
                //
                println("$key: $certificateClassName ($attributes) $desc")
            }
    }

    @Test
    fun validateGettingEntryMapFromSelfSignedJKS() {

        // Assemble
        val alias = "mr testo"
        val passwd = "secret"
        val resourcePath = "testdata/keystores/selfsigned_keystore.jks"
        val resourceURL = Thread.currentThread().contextClassLoader.getResource(resourcePath)

        val ks = CertificateAlgorithms.loadKeyStore(passwd, JKS_KEYSTORE_TYPE, resourceURL)

        // Act
        val result = CertificateAlgorithms.getEntryMapFrom(ks, mapOf(Pair(alias, passwd)))

        // Assert
        assertThat(result).isNotNull
        assertThat(result.size).isEqualTo(1)

        val privateKey = result[alias] as KeyStore.PrivateKeyEntry
        val relativeDNs = CertificateAlgorithms.getRelativeDistinguishedNamesFor(
            privateKey.certificate as X509Certificate)

        assertThat(relativeDNs).isNotNull
        assertThat(relativeDNs.size).isEqualTo(7)

        val keyCN: Rdn = relativeDNs.first { it.type == "CN" }
        assertThat(keyCN.value).isEqualTo("Mr Testo")
    }

    @Test
    fun validateMergingKeyStores() {

        // Assemble
        val alias = "mr testo"
        val passwd = "secret"
        val resourcePath = "testdata/keystores/selfsigned_keystore.jks"
        val alias2PasswordMap = mapOf(Pair(alias, passwd))
        val resourceURL = Thread.currentThread().contextClassLoader.getResource(resourcePath)

        val toMerge = CertificateAlgorithms.loadKeyStore(passwd, JKS_KEYSTORE_TYPE, resourceURL)
        val defaultKeystore = CertificateAlgorithms.loadKeyStore()

        // Act
        val merged = CertificateAlgorithms.mergeKeyStores(
            defaultKeystore,
            emptyMap(),
            toMerge,
            alias2PasswordMap)

        val mergedEntryMap = CertificateAlgorithms.getEntryMapFrom(merged, alias2PasswordMap)

        // Assert
        assertThat(merged).isNotNull
        assertThat(merged.size()).isEqualTo(defaultKeystore.size() + 1)

        assertThat(mergedEntryMap).isNotNull
        assertThat(mergedEntryMap.size).isEqualTo(defaultKeystore.size() + 1)

        val allPrivateKeyEntries = mergedEntryMap.filter { e -> e.value is KeyStore.PrivateKeyEntry }
        val allTrustedCertificateEntries = mergedEntryMap.filter { e -> e.value is KeyStore.TrustedCertificateEntry }

        assertThat(allPrivateKeyEntries.size).isEqualTo(1)
        assertThat(allTrustedCertificateEntries.size).isEqualTo(defaultKeystore.size())
    }

    @Test
    fun validateMergingSelfSignedCertAsTrustedCA() {

        // Assemble
        val alias = "mr testo"
        val passwd = "secret"
        val resourcePath = "testdata/keystores/selfsigned_keystore.jks"
        val alias2PasswordMap = mapOf(Pair(alias, passwd))
        val resourceURL = Thread.currentThread().contextClassLoader.getResource(resourcePath)

        val toMerge = CertificateAlgorithms.loadKeyStore(passwd, JKS_KEYSTORE_TYPE, resourceURL)
        val defaultKeystore = CertificateAlgorithms.loadKeyStore()

        // Act
        val merged = CertificateAlgorithms.mergeKeyStores(
            defaultKeystore,
            emptyMap(),
            toMerge,
            alias2PasswordMap,
            Predicate { true },
            { anEntry ->
                when (anEntry) {
                    is KeyStore.PrivateKeyEntry -> KeyStore.TrustedCertificateEntry(anEntry.certificate)
                    else -> anEntry
                }
            })

        val mergedEntryMap = CertificateAlgorithms.getEntryMapFrom(merged, alias2PasswordMap)

        // Assert
        assertThat(merged).isNotNull
        assertThat(merged.size()).isEqualTo(defaultKeystore.size() + 1)

        assertThat(mergedEntryMap).isNotNull
        assertThat(mergedEntryMap.size).isEqualTo(defaultKeystore.size() + 1)

        val allPrivateKeyEntries = mergedEntryMap.filter { e -> e.value is KeyStore.PrivateKeyEntry }
        val allTrustedCertificateEntries = mergedEntryMap.filter { e -> e.value is KeyStore.TrustedCertificateEntry }

        assertThat(allPrivateKeyEntries).isEmpty()
        assertThat(allTrustedCertificateEntries.size).isEqualTo(defaultKeystore.size() + 1)
    }

    @Test
    fun validateMergingSelfSignedCertAsTrustedCAWithConvenience() {
        
        // Assemble
        val alias = "mr testo"
        val passwd = "secret"
        val resourcePath = "testdata/keystores/selfsigned_keystore.jks"
        val alias2PasswordMap = mapOf(Pair(alias, passwd))
        val resourceURL = Thread.currentThread().contextClassLoader.getResource(resourcePath)

        val toMerge = CertificateAlgorithms.loadKeyStore(passwd, JKS_KEYSTORE_TYPE, resourceURL)
        val defaultKeystore = CertificateAlgorithms.loadKeyStore()

        // Act
        val merged = CertificateAlgorithms.convertSelfSignedCertificatesToTrustedAndMergeWithCaCerts(
            toMerge,
            alias2PasswordMap)

        val mergedEntryMap = CertificateAlgorithms.getEntryMapFrom(merged, alias2PasswordMap)

        // Assert
        assertThat(merged).isNotNull
        assertThat(merged.size()).isEqualTo(defaultKeystore.size() + 1)

        assertThat(mergedEntryMap).isNotNull
        assertThat(mergedEntryMap.size).isEqualTo(defaultKeystore.size() + 1)

        val allPrivateKeyEntries = mergedEntryMap.filter { e -> e.value is KeyStore.PrivateKeyEntry }
        val allTrustedCertificateEntries = mergedEntryMap.filter { e -> e.value is KeyStore.TrustedCertificateEntry }

        assertThat(allPrivateKeyEntries).isEmpty()
        assertThat(allTrustedCertificateEntries.size).isEqualTo(defaultKeystore.size() + 1)

    }
}