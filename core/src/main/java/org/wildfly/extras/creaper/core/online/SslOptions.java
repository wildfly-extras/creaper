package org.wildfly.extras.creaper.core.online;

import com.google.common.io.ByteSource;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * SSL/TLS connection options. Use {@code SslOptions.Builder} to create a new instance.
 */
public final class SslOptions {

    private static final SslProtocol DEFAULT_PROTOCOL = SslProtocol.TLS;

    final SslProtocol protocol;

    final ByteSource keyStoreSource;
    final String keyStorePassword;
    final KeyStoreType keyStoreType;
    final String keyAlias;
    final String keyPassword;

    final ByteSource trustStoreSource;
    final String trustStorePassword;
    final KeyStoreType trustStoreType;
    final boolean hostnameVerification;


    private SslOptions(Builder builder) {
        this.protocol = builder.protocol != null ? builder.protocol : DEFAULT_PROTOCOL;

        this.keyStoreSource = builder.keyStoreSource;
        this.keyStorePassword = builder.keyStorePassword;
        this.keyStoreType = builder.keyStoreType != null ? builder.keyStoreType : KeyStoreType.DEFAULT_TYPE;
        this.keyAlias = builder.keyAlias;
        this.keyPassword = builder.keyPassword;

        this.trustStoreSource = builder.trustStoreSource;
        this.trustStorePassword = builder.trustStorePassword;
        this.trustStoreType = builder.trustStoreType != null ? builder.trustStoreType : KeyStoreType.DEFAULT_TYPE;
        this.hostnameVerification = builder.hostnameVerification;
    }


    public static final class Builder {
        private SslProtocol protocol;

        private ByteSource keyStoreSource;
        private String keyStorePassword;
        private KeyStoreType keyStoreType;
        private String keyAlias;
        private String keyPassword;

        private ByteSource trustStoreSource;
        private String trustStorePassword;
        private KeyStoreType trustStoreType;
        private boolean hostnameVerification = true;

        /** Changes SSL/TLS protocol used for connection. Optional. {@link SslProtocol#TLS TLS} is used by default. */
        public Builder protocol(SslProtocol protocol) {
            this.protocol = protocol;
            return this;
        }

        /** Keystore with certificate for authentication, from the filesystem. Optional. */
        public Builder keyStore(File file) {
            if (file == null || !file.exists()) {
                throw new IllegalArgumentException("A file with the keystore must be provided.");
            }

            this.keyStoreSource = Files.asByteSource(file);
            return this;
        }

        /**
         * Keystore with certificate for authentication, from the classpath. Optional.
         * The resource will be loaded by the {@code resourceLoader} class ({@link Class#getResourceAsStream(String)}).
         *
         * @param resourceLoader class that will be used to load the keystore from classpath
         * @param path path to the keystore on classpath (absolute or relative to {@code resourceLoader})
         * @throws IllegalArgumentException if the {@code resourceLoader} or {@code path} is {@code null}
         */
        public Builder keyStore(Class resourceLoader, String path) {
            if (resourceLoader == null) {
                throw new IllegalArgumentException("A class for loading the keystore must be provided.");
            }
            if (path == null) {
                throw new IllegalArgumentException("A path to the keystore must be provided.");
            }

            URL url = Resources.getResource(resourceLoader, path);
            this.keyStoreSource = Resources.asByteSource(url);
            return this;
        }

        /** Keystore password. Optional. */
        public Builder keyStorePassword(String keyStorePassword) {
            this.keyStorePassword = keyStorePassword;
            return this;
        }

        /** Key from keystore used for authentication. Optional. */
        public Builder key(String keyAlias, String keyPassword) {
            this.keyAlias = keyAlias;
            this.keyPassword = keyPassword;
            return this;
        }

        /**
         * Changes expected type of keystore. Optional.
         * {@link KeyStoreType#DEFAULT_TYPE JDK default} is expected by default.
         */
        public Builder keyStoreType(KeyStoreType keyStoreType) {
            this.keyStoreType = keyStoreType;
            return this;
        }

        /** Truststore with trusted certificates, from the filesystem. Optional. */
        public Builder trustStore(File file) {
            if (file == null || !file.exists()) {
                throw new IllegalArgumentException("A file with the truststore must be provided.");
            }

            this.trustStoreSource = Files.asByteSource(file);
            return this;
        }

        /**
         * Truststore with certificate for authentication, from the classpath. Optional.
         * The resource will be loaded by the {@code resourceLoader} class ({@link Class#getResourceAsStream(String)}).
         *
         * @param resourceLoader class that will be used to load the truststore from classpath
         * @param path path to the truststore on classpath (absolute or relative to {@code resourceLoader})
         * @throws IllegalArgumentException if the {@code resourceLoader} or {@code path} is {@code null}
         */
        public Builder trustStore(Class resourceLoader, String path) {
            if (resourceLoader == null) {
                throw new IllegalArgumentException("A class for loading the truststore must be provided.");
            }
            if (path == null) {
                throw new IllegalArgumentException("A path to the truststore must be provided.");
            }

            URL url = Resources.getResource(resourceLoader, path);
            this.trustStoreSource = Resources.asByteSource(url);
            return this;
        }

        /** Truststore password. Optional. */
        public Builder trustStorePassword(String trustStorePassword) {
            this.trustStorePassword = trustStorePassword;
            return this;
        }

        /**
         * Changes expected type of truststore. Optional.
         * {@link KeyStoreType#DEFAULT_TYPE JDK default} is expected by default.
         */
        public Builder trustStoreType(KeyStoreType trustStoreType) {
            this.trustStoreType = trustStoreType;
            return this;
        }

        /**
         * Errors during hostname verification will be ignored.
         * Hostname verification is an HTTPS concept, so this method only makes sense when using
         * {@link ManagementProtocol#HTTPS}. There's no such thing as hostname verification with the native management
         * protocols.
         */
        public Builder turnOffHostnameVerification() {
            this.hostnameVerification = false;
            return this;
        }

        /** Builds the final {@code SslOptions}. */
        public SslOptions build() {
            return new SslOptions(this);
        }
    }


    /**
     * System properties like {@literal javax.net.ssl.keyStore} are not taken into account.
     * See <a href="http://docs.oracle.com/javase/6/docs/technotes/guides/security/jsse/JSSERefGuide.html#Customization">http://docs.oracle.com/javase/6/docs/technotes/guides/security/jsse/JSSERefGuide.html#Customization</a>.
     */
    SSLContext createSslContext() {
        final KeyManager[] keyManagers = this.getKeyManagers();
        final TrustManager[] trustManagers = this.getTrustManagers();

        try {
            final SSLContext sslContext = SSLContext.getInstance(this.protocol.protocolName());
            sslContext.init(keyManagers, trustManagers, null);

            return sslContext;
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Failed to create SSLContext.", ex);
        }
    }

    private KeyManager[] getKeyManagers() {
        // PKCS#11 provider provides the keys, source for the keystore should not be set
        if (this.keyStoreSource == null && this.keyStoreType != KeyStoreType.PKCS11) {
            return null;
        } else if (this.keyStoreSource != null && this.keyStoreType == KeyStoreType.PKCS11) {
            throw new IllegalArgumentException("The keystore type is PKCS#11, the keystore should not be set.");
        }

        final KeyManagerFactory keyManagerFactory = createKeyManagerFactory();
        KeyStore keyStore = this.createKeyStore();
        char[] password = toCharArray(this.keyPassword != null ? this.keyPassword : this.keyStorePassword);

        if (this.keyAlias != null) {
            // We cannot pass key alias to KeyManagerFactory to define which key should be used. Furthermore, other
            // keys in the keystore may have different passwords, which could cause failures while retrieving the key.
            keyStore = this.createRepacementKeyStore(keyStore, password);
        }

        try {
            keyManagerFactory.init(keyStore, password);
        } catch (GeneralSecurityException ex) {
            throw new IllegalArgumentException("Failed to init KeyManagerFactory for " + this.keyStoreSource, ex);
        }

        return keyManagerFactory.getKeyManagers();
    }

    private TrustManager[] getTrustManagers() {
        // PKCS#11 provider provides the keys, source for the truststore should not be set
        if (this.trustStoreSource == null && this.trustStoreType != KeyStoreType.PKCS11) {
            return null;
        } else if (this.trustStoreSource != null && this.trustStoreType == KeyStoreType.PKCS11) {
            throw new IllegalArgumentException("The truststore type is PKCS#11, the truststore should not be set.");
        }

        final TrustManagerFactory trustManagerFactory = createTrustManagerFactory();
        final KeyStore trustStore = this.createTrustStore();

        try {
            trustManagerFactory.init(trustStore);
        } catch (KeyStoreException ex) {
            throw new IllegalArgumentException("Failed to init TrustManagerFactory for " + this.trustStoreSource, ex);
        }
        return trustManagerFactory.getTrustManagers();
    }

    @SuppressFBWarnings("NP_NULL_PARAM_DEREF") // spotbugs doesn't know guava's @Nullable
    private KeyStore createKeyStore() {
        final KeyStore keyStore = createStore(this.keyStoreType);
        InputStream keyStoreStream = null;

        if (this.keyStoreSource != null) {
            keyStoreStream = createStoreStream(this.keyStoreSource);
        }

        try {
            keyStore.load(keyStoreStream, toCharArray(this.keyStorePassword));
        } catch (IOException ex) {
            if (ex.getCause() instanceof UnrecoverableKeyException) {
                throw new IllegalArgumentException("Failed to load keystore. Maybe the password is not correct.", ex);
            }
            throw new IllegalStateException("Failed to load keystore. Maybe the keystore type is not correct.", ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Failed to load keystore.", ex);
        } catch (CertificateException ex) {
            throw new IllegalStateException("Failed to load keystore.", ex);
        } finally {
            Closeables.closeQuietly(keyStoreStream);
        }

        return keyStore;
    }

    /**
     * Creates a new keystore of {@link KeyStoreType#DEFAULT_TYPE default type} with single entry
     * (according to the key alias) accessible under given password.
     *
     * <p>We cannot pass key alias to {@link KeyManagerFactory#init(KeyStore, char[])} to define which key should be
     * used. Furthermore, other keys in the given keystore may have different passwords, which could cause failures
     * while retrieving the key (for example, in the {@link sun.security.ssl.SunX509KeyManagerImpl}, there is used
     * {@link KeyStore#getKey(String, char[])} for each key alias in the keystore (applying given password),
     * therefore {@link UnrecoverableKeyException} could be thrown).</p>
     */
    private KeyStore createRepacementKeyStore(KeyStore keyStore, char[] password) {
        final KeyStore replacementKeyStore = createEmptyStore(KeyStoreType.DEFAULT_TYPE);
        final KeyStore.ProtectionParameter protection = new KeyStore.PasswordProtection(password);
        final KeyStore.Entry keyEntry;

        try {
            keyEntry = keyStore.getEntry(this.keyAlias, protection);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Failed to read certificate for alias " + this.keyAlias, ex);
        } catch (UnrecoverableEntryException ex) {
            throw new IllegalStateException("Failed to read certificate for alias '" + this.keyAlias
                    + "'. Maybe the password is not correct.", ex);
        } catch (KeyStoreException ex) {
            throw new IllegalStateException("Failed to read certificate for alias " + this.keyAlias, ex);
        }

        if (keyEntry == null) {
            throw new IllegalStateException("Failed to read certificate for alias '" + this.keyAlias
                    + "'. Maybe wrong alias was provided.");
        }

        try {
            replacementKeyStore.setEntry(this.keyAlias, keyEntry, protection);
        } catch (KeyStoreException ex) {
            throw new IllegalStateException("Failed to write certificate for alias " + this.keyAlias, ex);
        }

        return replacementKeyStore;
    }

    @SuppressFBWarnings("NP_NULL_PARAM_DEREF") // spotbugs doesn't know guava's @Nullable
    private KeyStore createTrustStore() {
        final KeyStore trustStore = createStore(this.trustStoreType);
        InputStream trustStoreStream = null;

        if (this.trustStoreSource != null) {
            trustStoreStream = createStoreStream(this.trustStoreSource);
        }

        try {
            trustStore.load(trustStoreStream, toCharArray(this.trustStorePassword));
        } catch (IOException ex) {
            if (ex.getCause() instanceof UnrecoverableKeyException) {
                throw new IllegalArgumentException("Failed to load truststore. Maybe the password is not correct.", ex);
            }
            throw new IllegalStateException("Failed to load truststore. Maybe the truststore type is not correct.", ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Failed to load truststore.", ex);
        } catch (CertificateException ex) {
            throw new IllegalStateException("Failed to load truststore.", ex);
        } finally {
            Closeables.closeQuietly(trustStoreStream);
        }

        return trustStore;
    }

    private static KeyManagerFactory createKeyManagerFactory() {
        try {
            return KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("KeyManagerFactory cannot be instantiated with default algorithm.", ex);
        }
    }

    private static TrustManagerFactory createTrustManagerFactory() {
        try {
            return TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("TrustManagerFactory cannot be instantiated with default algorithm.", ex);
        }
    }

    private static KeyStore createStore(KeyStoreType storeType) {
        try {
            return KeyStore.getInstance(storeType.typeName());
        } catch (KeyStoreException ex) {
            throw new IllegalArgumentException("KeyStore of type '" + storeType + "' cannot be instantiated.", ex);
        }
    }

    private static KeyStore createEmptyStore(KeyStoreType storeType) {
        KeyStore keyStore = createStore(storeType);

        try {
            keyStore.load(null);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load empty keystore.", ex);
        }

        return keyStore;
    }

    private static InputStream createStoreStream(ByteSource store) {
        try {
            return store.openStream();
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to open store file " + store, ex);
        }
    }

    private static char[] toCharArray(String password) {
        if (password != null && !password.isEmpty()) {
            return password.toCharArray();
        }

        return null;
    }
}
