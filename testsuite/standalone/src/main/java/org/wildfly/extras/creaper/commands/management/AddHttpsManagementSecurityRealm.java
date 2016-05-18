package org.wildfly.extras.creaper.commands.management;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Batch;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

/**
 * <b>Only for tests!</b>
 */
public final class AddHttpsManagementSecurityRealm implements OnlineCommand, OfflineCommand {
    private final String securityRealmName;

    // server identity
    private final String protocol;
    private final List<String> enabledProtocols;
    private final List<String> enabledCipherSuites;

    // server identity keystore
    private final String keystorePassword;
    private final String keystorePath;
    private final String keystoreRelativeTo;
    private final String keystoreProvider;
    private final String keyAlias;
    private final String keyPassword;

    // authentication truststore
    private final String truststorePassword;
    private final String truststorePath;
    private final String truststoreRelativeTo;
    private final String truststoreProvider;

    private final boolean replaceExisting;


    private AddHttpsManagementSecurityRealm(Builder builder) {
        this.securityRealmName = builder.securityRealmName;

        this.protocol = builder.protocol;
        this.enabledProtocols = builder.enabledProtocols;
        this.enabledCipherSuites = builder.enabledCipherSuites;

        this.keystorePassword = builder.keystorePassword;
        this.keystorePath = builder.keystorePath;
        this.keystoreRelativeTo = builder.keystoreRelativeTo;
        this.keystoreProvider = builder.keystoreProvider;
        this.keyAlias = builder.keyAlias;
        this.keyPassword = builder.keyPassword;

        this.truststorePassword = builder.truststorePassword;
        this.truststorePath = builder.truststorePath;
        this.truststoreRelativeTo = builder.truststoreRelativeTo;
        this.truststoreProvider = builder.truststoreProvider;

        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws IOException {
        Address securityRealmAddress = Address.coreService("management").and("security-realm", this.securityRealmName);

        Operations ops = new Operations(ctx.client);
        Batch batch = new Batch();
        batch.add(securityRealmAddress);
        batch.add(securityRealmAddress.and("server-identity", "ssl"), Values.empty()
                        .andOptional("alias", this.keyAlias)
                        .andListOptional(String.class, "enabled-cipher-suites", this.enabledCipherSuites)
                        .andListOptional(String.class, "enabled-protocols", this.enabledProtocols)
                        .andOptional("key-password", this.keyPassword)
                        .and("keystore-password", this.keystorePassword)
                        .andOptional("keystore-path", this.keystorePath)
                        .andOptional("keystore-provider", this.keystoreProvider)
                        .andOptional("keystore-relative-to", this.keystoreRelativeTo)
                        .andOptional("protocol", this.protocol)
        );
        if (this.truststorePath != null) {
            batch.add(securityRealmAddress.and("authentication", "truststore"), Values.empty()
                    .and("keystore-password", this.truststorePassword)
                    .andOptional("keystore-path", this.truststorePath)
                    .andOptional("keystore-provider", this.truststoreProvider)
                    .andOptional("keystore-relative-to", this.truststoreRelativeTo)
            );
        }

        ops.batch(batch);
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        ctx.client.apply(GroovyXmlTransform.of(AddHttpsManagementSecurityRealm.class)
                .subtree("management", Subtree.management())
                .parameter("atrSecurityRealmName", this.securityRealmName)
                .parameter("atrProtocol", this.protocol)
                .parameter("atrEnabledProtocols", this.enabledProtocols)
                .parameter("atrEnabledCipherSuites", this.enabledCipherSuites)
                .parameter("atrKeystorePassword", this.keystorePassword)
                .parameter("atrKeystorePath", this.keystorePath)
                .parameter("atrKeystoreRelativeTo", this.keystoreRelativeTo)
                .parameter("atrKeystoreProvider", this.keystoreProvider)
                .parameter("atrKeyAlias", this.keyAlias)
                .parameter("atrKeyPassword", this.keyPassword)
                .parameter("atrTruststorePassword", this.truststorePassword)
                .parameter("atrTruststorePath", this.truststorePath)
                .parameter("atrTruststoreRelativeTo", this.truststoreRelativeTo)
                .parameter("atrTruststoreProvider", this.truststoreProvider)
                .parameter("atrReplaceExisting", this.replaceExisting)
                .build());
    }

    public static final class Builder {
        private final String securityRealmName;

        // keystore
        private String keystorePassword;
        private String keyAlias;
        private List<String> enabledCipherSuites;
        private List<String> enabledProtocols;
        private String keyPassword;
        private String keystorePath;
        private String keystoreProvider;
        private String keystoreRelativeTo;
        private String protocol;

        // truststore
        private String truststorePassword;
        private String truststorePath;
        private String truststoreProvider;
        private String truststoreRelativeTo;

        private boolean replaceExisting;


        public Builder(String securityRealmName) {
            this.securityRealmName = securityRealmName;
        }

        /**
         * The alias of the entry to use from the keystore.
         */
        public Builder keyAlias(String keyAlias) {
            this.keyAlias = keyAlias;
            return this;
        }

        /**
         * Adds ciphers which should be set as enabled on the underlying SSLEngine.
         */
        public Builder cipherSuitesToEnable(String... cipherSuitesToEnable) {
            if (this.enabledCipherSuites == null && cipherSuitesToEnable != null) {
                this.enabledCipherSuites = new ArrayList<String>();
            }
            if (cipherSuitesToEnable != null) {
                this.enabledCipherSuites.addAll(Arrays.asList(cipherSuitesToEnable));
            }
            return this;
        }

        /**
         * Adds protocols which should be set as enabled on the underlying SSLEngine.
         */
        public Builder protocolsToEnable(String... protocolsToEnable) {
            if (this.enabledProtocols == null && protocolsToEnable != null) {
                this.enabledProtocols = new ArrayList<String>();
            }
            if (protocolsToEnable != null) {
                this.enabledProtocols.addAll(Arrays.asList(protocolsToEnable));
            }
            return this;
        }

        /**
         * Defines the password to open the keystore. This is mandatory parameter which needs to be set.
         */
        public Builder keystorePassword(String keystorePassword) {
            this.keystorePassword = keystorePassword;
            return this;
        }

        /**
         * Defines the password to obtain the key from the keystore.
         */
        public Builder keyPassword(String keyPassword) {
            this.keyPassword = keyPassword;
            return this;
        }

        /**
         * Defines the path of the keystore, will be ignored if the keystore provider is anything other than JKS.
         */
        public Builder keystorePath(String keystorePath) {
            this.keystorePath = keystorePath;
            return this;
        }

        /**
         * The provider for loading the keystore, defaults to JKS.
         */
        public Builder keystoreProvider(String keystoreProvider) {
            this.keystoreProvider = keystoreProvider;
            return this;
        }

        /**
         * Defines name of another previously named path, or of one of the standard paths provided by the system.
         * If 'relative-to' is provided, the value of the 'path' attribute is treated as relative to the path specified
         * by this attribute.
         */
        public Builder keystoreRelativeTo(String keystoreRelativeTo) {
            this.keystoreRelativeTo = keystoreRelativeTo;
            return this;
        }

        /**
         * Defines the protocol to use when creating the SSLContext.
         */
        public Builder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        /**
         * Defines the password to open the truststore. It is mandatory parameter when also defining truststore
         */
        public Builder truststorePassword(String truststorePassword) {
            this.truststorePassword = truststorePassword;
            return this;
        }

        /**
         * Defines the path of the trustore, will be ignored if the truststore provider is anything other than JKS.
         * If not defined, truststore is not defined for the server.
         */
        public Builder truststorePath(String truststorePath) {
            this.truststorePath = truststorePath;
            return this;
        }

        /**
         * Defines the provider for loading the truststore, defaults to JKS.
         */
        public Builder truststoreProvider(String truststoreProvider) {
            this.truststoreProvider = truststoreProvider;
            return this;
        }

        /**
         * Define name of another previously named path, or of one of the standard paths provided by the system.
         * If 'relative-to' is provided, the value of the 'path' attribute is treated as relative to the path
         * specified by this attribute.
         */
        public Builder truststoreRelativeTo(String truststoreRelativeTo) {
            this.truststoreRelativeTo = truststoreRelativeTo;
            return this;
        }

        /**
         * <b>This can cause server reload!</b>
         */
        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddHttpsManagementSecurityRealm build() {
            if (this.keystorePassword == null) {
                throw new IllegalArgumentException("keystorePassword is manadatory");
            }
            if (this.truststorePath != null && this.truststorePassword == null) {
                throw new IllegalArgumentException("truststorePassword is manadatory when defining also truststore");
            }
            return new AddHttpsManagementSecurityRealm(this);
        }
    }
}
