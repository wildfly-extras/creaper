package org.wildfly.extras.creaper.commands.web;

import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;

/**
 * Command which adds ssl configuration to the specified connector
 */
public final class AddConnectorSslConfig implements OnlineCommand, OfflineCommand {
    private final String connectorName;

    private final String caCertificateFile;
    private final String caCertificatePassword;
    private final String caRevocationUrl;
    private final String certificateFile;
    private final String certificateKeyFile;
    private final String cipherSuite;
    private final String keyAlias;
    private final String keystoreType;
    private final String password;
    private final String protocol;
    private final Integer sessionCacheSize;
    private final Integer sessionTimeout;
    private final String sslProtocol;
    private final String truststoreType;
    private final String verifyClient;
    private final Integer verifyDepth;

    private AddConnectorSslConfig(Builder builder) {
        this.connectorName = builder.connectorName;
        this.caCertificateFile = builder.caCertificateFile;
        this.caCertificatePassword = builder.caCertificatePassword;
        this.caRevocationUrl = builder.caRevocationUrl;
        this.certificateFile = builder.certificateFile;
        this.certificateKeyFile = builder.certificateKeyFile;
        this.cipherSuite = builder.cipherSuite;
        this.keyAlias = builder.keyAlias;
        this.keystoreType = builder.keystoreType;
        this.password = builder.password;
        this.protocol = builder.protocol;
        this.sessionCacheSize = builder.sessionCacheSize;
        this.sessionTimeout = builder.sessionTimeout;
        this.sslProtocol = builder.sslProtocol;
        this.truststoreType = builder.truststoreType;
        this.verifyClient = builder.verifyClient;
        this.verifyDepth = builder.verifyDepth;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws IOException {
        Operations ops = new Operations(ctx.client);
        ops.add(Address.subsystem("web").and("connector", connectorName).and("configuration", "ssl"), Values.empty()
                .andOptional("ca-certificate-file", caCertificateFile)
                .andOptional("ca-certificate-password", caCertificatePassword)
                .andOptional("ca-revocation-url", caRevocationUrl)
                .andOptional("certificate-file", certificateFile)
                .andOptional("certificate-key-file", certificateKeyFile)
                .andOptional("cipher-suite", cipherSuite)
                .andOptional("key-alias", keyAlias)
                .andOptional("keystore-type", keystoreType)
                .andOptional("password", password)
                .andOptional("protocol", protocol)
                .andOptional("session-cache-size", sessionCacheSize)
                .andOptional("session-timeout", sessionTimeout)
                .andOptional("ssl-protocol", sslProtocol)
                .andOptional("truststore-type", truststoreType)
                .andOptional("verify-client", verifyClient)
                .andOptional("verify-depth", verifyDepth));
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {
        GroovyXmlTransform sslConf = GroovyXmlTransform
                .of(AddConnectorSslConfig.class)
                .subtree("web", Subtree.subsystem("web"))
                .parameter("connectorName", connectorName)
                .parameter("caCertificateFile", caCertificateFile)
                .parameter("caCertificatePassword", caCertificatePassword)
                .parameter("caRevocationUrl", caRevocationUrl)
                .parameter("certificateFile", certificateFile)
                .parameter("certificateKeyFile", certificateKeyFile)
                .parameter("cipherSuite", cipherSuite)
                .parameter("keyAlias", keyAlias)
                .parameter("keystoreType", keystoreType)
                .parameter("password", password)
                .parameter("protocol", protocol)
                .parameter("sessionCacheSize", sessionCacheSize)
                .parameter("sessionTimeout", sessionTimeout)
                .parameter("sslProtocol", sslProtocol)
                .parameter("truststoreType", truststoreType)
                .parameter("verifyClient", verifyClient)
                .parameter("verifyDepth", verifyDepth)
                .build();
        ctx.client.apply(sslConf);
    }

    @Override
    public String toString() {
        return "AddConnectorSslConfig " + connectorName;
    }

    /**
     * Some details about the attributes can be found at http://wildscribe.github.io/JBoss%20EAP/6.2.0/subsystem/web/connector/configuration/ssl/index.html
     */
    public static final class Builder {
        private final String connectorName;

        private String caCertificateFile;
        private String caCertificatePassword;
        private String caRevocationUrl;
        private String certificateFile;
        private String certificateKeyFile;
        private String cipherSuite;
        private String keyAlias;
        private String keystoreType;
        private String password;
        private String protocol;
        private Integer sessionCacheSize;
        private Integer sessionTimeout;
        private String sslProtocol;
        private String truststoreType;
        private String verifyClient;
        private Integer verifyDepth;

        public Builder(String connectorName) {
            if (connectorName == null) {
                throw new IllegalArgumentException("Name of the connector must be specified as non null value");
            }
            this.connectorName = connectorName;
        }

        /**
         * Sets certificate authority
         */
        public Builder caCertificateFile(String caCertificateFile) {
            this.caCertificateFile = caCertificateFile;
            return this;
        }

        /**
         * Sets certificate password
         */
        public Builder caCertificatePassword(String caCertificatePassword) {
            this.caCertificatePassword = caCertificatePassword;
            return this;
        }

        /**
         * Sets certificate authority revocation list
         */
        public Builder caRevocationUrl(String caRevocationUrl) {
            this.caRevocationUrl = caRevocationUrl;
            return this;
        }

        /**
         * Sets server certificate file.
         */
        public Builder certificateFile(String certificateFile) {
            this.certificateFile = certificateFile;
            return this;
        }

        /**
         * Sets key file for the certificate.
         */
        public Builder certificateKeyFile(String certificateKeyFile) {
            this.certificateKeyFile = certificateKeyFile;
            return this;
        }

        /**
         * Sets the allowed cipher suite.
         */
        public Builder cipherSuite(String cipherSuite) {
            this.cipherSuite = cipherSuite;
            return this;
        }

        /**
         * Sets the key alias.
         */
        public Builder keyAlias(String keyAlias) {
            this.keyAlias = keyAlias;
            return this;
        }

        /**
         * Sets the keystore type.
         */
        public Builder keystoreType(String keystoreType) {
            this.keystoreType = keystoreType;
            return this;
        }

        /**
         * Sets the password
         */
        public Builder password(String password) {
            this.password = password;
            return this;
        }

        /**
         * Sets the SSL protocols that are enabled.
         */
        public Builder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        /**
         * Sets size of the SSL session cache.
         */
        public Builder sessionCacheSize(Integer sessionCacheSize) {
            this.sessionCacheSize = sessionCacheSize;
            return this;
        }

        /**
         * Sets the SSL session cache timeout.
         */
        public Builder sessionTimeout(Integer sessionTimeout) {
            this.sessionTimeout = sessionTimeout;
            return this;
        }

        /**
         * Sets name of custom ssl protocol provider
         */
        public Builder sslProtocol(String sslProtocol) {
            this.sslProtocol = sslProtocol;
            return this;
        }

        /**
         * Sets type of the truststore,
         */
        public Builder truststoreType(String truststoreType) {
            this.truststoreType = truststoreType;
            return this;
        }

        /**
         * Enables client certificate verification.
         */
        public Builder verifyClient(String verifyClient) {
            this.verifyClient = verifyClient;
            return this;
        }

        /**
         * Sets limit of certificate nesting.
         */
        public Builder verifyDepth(Integer verifyDepth) {
            this.verifyDepth = verifyDepth;
            return this;
        }

        public AddConnectorSslConfig build() {
            return new AddConnectorSslConfig(this);
        }
    }
}
