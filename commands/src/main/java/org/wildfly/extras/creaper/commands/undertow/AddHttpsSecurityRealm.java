package org.wildfly.extras.creaper.commands.undertow;

import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Batch;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Command which creates security realm for usage mainly by HTTPS listener.
 */
public final class AddHttpsSecurityRealm implements OnlineCommand {
    private final String securityRealmName;

    // keystore
    private final String keystorePassword;
    private final String alias;
    private final List<String> enabledCipherSuites;
    private final List<String> enabledProtocols;
    private final String keyPassword;
    private final String keystorePath;
    private final String keystoreProvider;
    private final String keystoreRelativeTo;
    private final String protocol;

    // truststore
    private final String truststorePassword;
    private final String truststorePath;
    private final String truststoreProvider;
    private final String truststoreRelativeTo;

    private AddHttpsSecurityRealm(Builder builder) {
        this.securityRealmName = builder.securityRealmName;
        this.keystorePassword = builder.keystorePassword;
        this.alias = builder.alias;
        this.enabledCipherSuites = builder.enabledCipherSuites;
        this.enabledProtocols = builder.enabledProtocols;
        this.keyPassword = builder.keyPassword;
        this.keystorePath = builder.keystorePath;
        this.keystoreProvider = builder.keystoreProvider;
        this.keystoreRelativeTo = builder.keystoreRelativeTo;
        this.protocol = builder.protocol;
        this.truststorePassword = builder.truststorePassword;
        this.truststorePath = builder.truststorePath;
        this.truststoreProvider = builder.truststoreProvider;
        this.truststoreRelativeTo = builder.truststoreRelativeTo;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws IOException {
        Address securityRealmAddress = Address.coreService("management").and("security-realm", securityRealmName);

        Operations ops = new Operations(ctx.client);
        Batch batch = new Batch();
        batch.add(securityRealmAddress);
        batch.add(securityRealmAddress.and("server-identity", "ssl"), Values.empty()
                        .andOptional("alias", alias)
                        .andListOptional(String.class, "enabled-cipher-suites", enabledCipherSuites)
                        .andListOptional(String.class, "enabled-protocols", enabledProtocols)
                        .andOptional("key-password", keyPassword)
                        .and("keystore-password", keystorePassword)
                        .andOptional("keystore-path", keystorePath)
                        .andOptional("keystore-provider", keystoreProvider)
                        .andOptional("keystore-relative-to", keystoreRelativeTo)
                        .andOptional("protocol", protocol)
        );
        batch.add(securityRealmAddress.and("authentication", "truststore"), Values.empty()
                        .and("keystore-password", truststorePassword)
                        .andOptional("keystore-path", truststorePath)
                        .andOptional("keystore-provider", truststoreProvider)
                        .andOptional("keystore-relative-to", truststoreRelativeTo)
        );

        ops.batch(batch);
    }

    public static final class Builder {
        private final String securityRealmName;

        // keystore
        private String keystorePassword;
        private String alias;
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

        public Builder(String securityRealmName) {
            this.securityRealmName = securityRealmName;
        }

        /**
         * The alias of the entry to use from the keystore.
         */
        public Builder alias(String alias) {
            this.alias = alias;
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
         * Defines the password to open the keystore.
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
         * Defines the password to open the truststore.
         */
        public Builder truststorePassword(String truststorePassword) {
            this.truststorePassword = truststorePassword;
            return this;
        }

        /**
         * Defines the path of the trustore, will be ignored if the truststore provider is anything other than JKS.
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

        public AddHttpsSecurityRealm build() {
            return new AddHttpsSecurityRealm(this);
        }
    }
}
