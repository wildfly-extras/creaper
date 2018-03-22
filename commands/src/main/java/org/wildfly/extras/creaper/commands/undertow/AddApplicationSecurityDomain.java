package org.wildfly.extras.creaper.commands.undertow;

import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

/**
 * Command which adds application security domain to Undertow subsystem.
 */
public final class AddApplicationSecurityDomain implements OnlineCommand, OfflineCommand {

    private final String name;
    private final String httpAuthenticationFactory;
    private final Boolean overrideDeploymentConfig;
    private final boolean replaceExisting;

    private AddApplicationSecurityDomain(Builder builder) {
        this.name = builder.name;
        this.httpAuthenticationFactory = builder.httpAuthenticationFactory;
        this.overrideDeploymentConfig = builder.overrideDeploymentConfig;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        // Undertow is available since WildFly 8, but some options are only available since WildFly 11;
        // for now, restricting to WildFly 11 and above
        ctx.version.assertAtLeast(ServerVersion.VERSION_5_0_0);

        Operations ops = new Operations(ctx.client);
        Address address = Address.subsystem("undertow").and("application-security-domain", name);
        if (replaceExisting) {
            ops.removeIfExists(address);
            new Administration(ctx.client).reloadIfRequired();
        }
        Values params = Values.empty()
                .and("http-authentication-factory", httpAuthenticationFactory)
                .andOptional("override-deployment-config", overrideDeploymentConfig);

        ops.add(address, params);
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        // Undertow is available since WildFly 8, but some options are only available since WildFly 11;
        // for now, restricting to WildFly 11 and above
        ctx.version.assertAtLeast(ServerVersion.VERSION_5_0_0);

        ctx.client.apply(GroovyXmlTransform.of(AddApplicationSecurityDomain.class)
                .subtree("undertowSubsystem", Subtree.subsystem("undertow"))
                .parameter("atrName", name)
                .parameter("atrHttpAuthenticationFactory", httpAuthenticationFactory)
                .parameter("atrOverrideDeploymentConfig", overrideDeploymentConfig)
                .parameter("atrReplaceExisting", replaceExisting)
                .build());
    }

    public static final class Builder {

        private final String name;
        private String httpAuthenticationFactory;
        private Boolean overrideDeploymentConfig;
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Name of the application-security-domain must be specified as non empty value");
            }
            this.name = name;
        }

        /**
         * Reference to the HttpAuthenticationFactory that should be used.
         */
        public Builder httpAuthenticationFactory(String httpAuthenticationFactory) {
            this.httpAuthenticationFactory = httpAuthenticationFactory;
            return this;
        }

        /**
         * The references HttpServerAuthenticationMechanismFactory contains it's own policy configuration
         * to control the authentication mechanisms it supports, if this attribute is set to 'true'
         * that policy will override the methods specified within the deployment.
         */
        public Builder overrideDeploymentConfig(boolean overrideDeploymentConfig) {
            this.overrideDeploymentConfig = overrideDeploymentConfig;
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddApplicationSecurityDomain build() {
            if (httpAuthenticationFactory == null || httpAuthenticationFactory.isEmpty()) {
                throw new IllegalArgumentException("httpAuthenticationFactory is manadatory");
            }
            return new AddApplicationSecurityDomain(this);
        }
    }
}
