package org.wildfly.extras.creaper.security;

import org.bouncycastle.x509.X509V3CertificateGenerator;

import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * <b>Only for tests!</b> Contains a cryptographic key pair and a certificate for it.
 */
public final class KeyPairAndCertificate {
    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final X509Certificate certificate;

    private KeyPairAndCertificate(PrivateKey privateKey, PublicKey publicKey, X509Certificate certificate) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.certificate = certificate;
    }

    /**
     * Generates a new key pair (RSA, 2048 bits) and a self-signed certificate for it (SHA1withRSA). The certificate
     * will use a distinguished name of the form {@code CN=name} and will be valid for 1 year.
     */
    public static KeyPairAndCertificate generateSelfSigned(String name) throws GeneralSecurityException, IOException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        Date from = new Date();
        Date to = new Date(from.getTime() + 365L * 24L * 60L * 60L * 1000L);
        BigInteger serialNumber = new BigInteger(64, new SecureRandom());
        X500Principal owner = new X500Principal("CN=" + name);

        X509V3CertificateGenerator certificateGenerator = new X509V3CertificateGenerator();
        certificateGenerator.setIssuerDN(owner);
        certificateGenerator.setSubjectDN(owner);
        certificateGenerator.setNotBefore(from);
        certificateGenerator.setNotAfter(to);
        certificateGenerator.setSerialNumber(serialNumber);
        certificateGenerator.setPublicKey(keyPair.getPublic());
        certificateGenerator.setSignatureAlgorithm("SHA1withRSA");
        X509Certificate certificate = certificateGenerator.generate(keyPair.getPrivate());

        return new KeyPairAndCertificate(keyPair.getPrivate(), keyPair.getPublic(), certificate);
    }

    /**
     * Creates a new keystore that will contain a single entry with given {@code entryAlias} and {@code entryPassword}.
     * The entry will contain the private key and the certificate.
     */
    public KeyStore toKeyStore(String entryAlias, String entryPassword) throws GeneralSecurityException, IOException {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null);
        keyStore.setKeyEntry(entryAlias, privateKey, entryPassword.toCharArray(), new Certificate[]{certificate});
        return keyStore;
    }
}
