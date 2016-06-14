package org.wildfly.extras.creaper.commands.security.realms;

import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddSslServerIdentity extends AbstractAddSecurityRealmSubElement {
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

    public AddSslServerIdentity(Builder builder) {
        super(builder);
        this.keystorePassword = builder.keystorePassword;
        this.alias = builder.alias;
        this.enabledCipherSuites = builder.enabledCipherSuites;
        this.enabledProtocols = builder.enabledProtocols;
        this.keyPassword = builder.keyPassword;
        this.keystorePath = builder.keystorePath;
        this.keystoreProvider = builder.keystoreProvider;
        this.keystoreRelativeTo = builder.keystoreRelativeTo;
        this.protocol = builder.protocol;
    }

    @Override
    public final void apply(OfflineCommandContext ctx) throws Exception {
        ctx.client.apply(GroovyXmlTransform.of(AddSslServerIdentity.class)
                .subtree("management", Subtree.management())
                .parameter("realmName", securityRealmName)
                .parameter("alias", alias)
                .parameter("enabledCipherSuites", enabledCipherSuites)
                .parameter("enabledProtocols", enabledProtocols)
                .parameter("keyPassword", keyPassword)
                .parameter("keystorePassword", keystorePassword)
                .parameter("keystorePath", keystorePath)
                .parameter("keystoreProvider", keystoreProvider)
                .parameter("keystoreRelativeTo", keystoreRelativeTo)
                .parameter("protocol", protocol)
                .parameter("replaceExisting", replaceExisting)
                .build());
    }

    @Override
    public final void apply(OnlineCommandContext ctx) throws Exception {
        Address sslServerIdentitiesAddress = securityRealmAddress.and("server-identity", "ssl");

        Operations ops = new Operations(ctx.client);

        if (replaceExisting) {
            boolean secretServerIdentityExists = ops.exists(sslServerIdentitiesAddress);
            if (secretServerIdentityExists) {
                ops.remove(sslServerIdentitiesAddress);
            }
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(securityRealmAddress.and("server-identity", "ssl"), Values.empty()
                .andOptional("alias", alias)
                .andListOptional(String.class, "enabled-cipher-suites", enabledCipherSuites)
                .andListOptional(String.class, "enabled-protocols", enabledProtocols)
                .andOptional("key-password", keyPassword)
                .and("keystore-password", keystorePassword)
                .andOptional("keystore-path", keystorePath)
                .andOptional("keystore-provider", keystoreProvider)
                .andOptional("keystore-relative-to", keystoreRelativeTo)
                .andOptional("protocol", protocol));
    }


    public static final class Builder extends AbstractAddSecurityRealmSubElement.Builder<Builder> {

        private String keystorePassword;
        private String alias;
        private List<String> enabledCipherSuites;
        private List<String> enabledProtocols;
        private String keyPassword;
        private String keystorePath;
        private String keystoreProvider;
        private String keystoreRelativeTo;
        private String protocol;

        public Builder(String securityRealmName) {
            super(securityRealmName);
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


        @Override
        public AddSslServerIdentity build() {
            if (keystorePassword == null) {
                throw new IllegalArgumentException("keystorePassword is manadatory");
            }
            return new AddSslServerIdentity(this);
        }

    }
}
