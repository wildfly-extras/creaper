package org.wildfly.extras.creaper.commands.security.realms;

import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public class AddTruststoreAuthentication extends AbstractAddSecurityRealmSubElement {
    private final String truststorePassword;
    private String truststorePath;
    private String truststoreProvider;
    private String truststoreRelativeTo;

    public AddTruststoreAuthentication(Builder builder) {
        super(builder);
        this.truststorePassword = builder.truststorePassword;
        this.truststorePath = builder.truststorePath;
        this.truststoreProvider = builder.truststoreProvider;
        this.truststoreRelativeTo = builder.truststoreRelativeTo;
    }

    @Override
    public final void apply(OfflineCommandContext ctx) throws Exception {
        if (ctx.version.greaterThanOrEqualTo(ServerVersion.VERSION_18_0_0)) {
            throw new AssertionError("Legacy security was removed in WildFly 25.");
        }

        ctx.client.apply(GroovyXmlTransform.of(AddTruststoreAuthentication.class)
                .subtree("management", Subtree.management())
                .parameter("realmName", securityRealmName)
                .parameter("truststorePassword", truststorePassword)
                .parameter("truststorePath", truststorePath)
                .parameter("truststoreProvider", truststoreProvider)
                .parameter("truststoreRelativeTo", truststoreRelativeTo)
                .parameter("replaceExisting", replaceExisting)
                .build());
    }

    @Override
    public final void apply(OnlineCommandContext ctx) throws Exception {
        if (ctx.version.greaterThanOrEqualTo(ServerVersion.VERSION_18_0_0)) {
            throw new AssertionError("Legacy security was removed in WildFly 25.");
        }

        Address truststoreAuthAddress = securityRealmAddress.and("authentication", "truststore");

        Operations ops = new Operations(ctx.client);

        if (replaceExisting) {
            boolean truststoreAuthExist = ops.exists(truststoreAuthAddress);
            if (truststoreAuthExist) {
                ops.remove(truststoreAuthAddress);
            }
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(truststoreAuthAddress, Values.empty()
                .and("keystore-password", truststorePassword)
                .andOptional("keystore-path", truststorePath)
                .andOptional("keystore-provider", truststoreProvider)
                .andOptional("keystore-relative-to", truststoreRelativeTo));
    }


    public static final class Builder extends AbstractAddSecurityRealmSubElement.Builder<Builder> {

        private String truststorePassword;
        private String truststorePath;
        private String truststoreProvider;
        private String truststoreRelativeTo;

        public Builder(String securityRealmName) {
            super(securityRealmName);
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

        @Override
        public AddTruststoreAuthentication build() {
            if (truststorePassword == null) {
                throw new IllegalArgumentException("truststorePassword is manadatory when defining the truststore");
            }
            return new AddTruststoreAuthentication(this);
        }

    }
}
