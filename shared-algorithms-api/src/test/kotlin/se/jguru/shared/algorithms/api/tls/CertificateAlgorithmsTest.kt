package se.jguru.shared.algorithms.api.tls

import org.junit.Assert
import org.junit.Test
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.cert.X509Certificate
import java.util.Objects

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
        Assert.assertNotNull(result)
        Assert.assertEquals(JKS_KEYSTORE_TYPE, result.type)
        Assert.assertEquals(0, result.size())
    }

    @Test(expected = KeyStoreException::class)
    fun validateExceptionOnCreatingUnknownKeystoreType() {

        // Act & Assert
        CertificateAlgorithms.createKeyStore("foobats", "unknownType")
    }

    @Test
    fun validateLoadingStandardKeystore() {

        // Assemble

        // Act
        val result1 = CertificateAlgorithms.loadKeyStore()
        val result2 = CertificateAlgorithms.getStandardJKS()

        // Assert
        Assert.assertNotNull(result1)
        Assert.assertNotNull(result2)

        Assert.assertEquals(JKS_KEYSTORE_TYPE, result1.type)
        Assert.assertEquals(JKS_KEYSTORE_TYPE, result2.type)

        Assert.assertTrue(result1.size() > 0)
        Assert.assertTrue(result1.size() == result2.size())
    }

    @Test
    fun validateGettingEntryMap() {

        // Assemble
        val standardJKS = CertificateAlgorithms.loadKeyStore()

        // Act
        val result = CertificateAlgorithms.getEntryMapFrom(standardJKS)

        // Assert
        Assert.assertNotNull(standardJKS)
        Assert.assertNotNull(result)

        result
            .filter { CertificateAlgorithms.IS_X509_CERTIFICATE.test(it.value) }
            .forEach { key, value ->

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
                println("$key: $certificateClassName ($attributes) $desc")
            }
    }
}