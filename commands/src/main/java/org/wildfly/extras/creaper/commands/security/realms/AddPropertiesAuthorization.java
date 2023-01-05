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

/**
 * Add properties authorization to security realm.
 */
public final class AddPropertiesAuthorization extends AbstractAddSecurityRealmSubElement {

    private final String path;
    private final String relativeTo;

    private AddPropertiesAuthorization(Builder builder) {
        super(builder);
        this.path = builder.path;
        this.relativeTo = builder.relativeTo;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (ctx.version.greaterThanOrEqualTo(ServerVersion.VERSION_18_0_0)) {
            throw new AssertionError("Legacy security was removed in WildFly 25.");
        }

        Operations ops = new Operations(ctx.client);
        Address securityRealmPropertiesAuthzAddress = securityRealmAddress.and("authorization", "properties");
        if (replaceExisting) {
            ops.removeIfExists(securityRealmPropertiesAuthzAddress);
            new Administration(ctx.client).reloadIfRequired();
        }
        ops.add(securityRealmPropertiesAuthzAddress, Values.empty()
                .andOptional("path", path)
                .andOptional("relative-to", relativeTo));
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        if (ctx.version.greaterThanOrEqualTo(ServerVersion.VERSION_18_0_0)) {
            throw new AssertionError("Legacy security was removed in WildFly 25.");
        }

        ctx.client.apply(GroovyXmlTransform.of(AddPropertiesAuthorization.class)
                .subtree("management", Subtree.management())
                .parameter("atrSecurityRealmName", securityRealmName)
                .parameter("atrPath", path)
                .parameter("atrRelativeTo", relativeTo)
                .parameter("atrReplaceExisting", replaceExisting)
                .build());
    }

    public static final class Builder extends AbstractAddSecurityRealmSubElement.Builder<Builder> {

        private String path;
        private String relativeTo;

        public Builder(String securityRealmName) {
            super(securityRealmName);
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder relativeTo(String relativeTo) {
            this.relativeTo = relativeTo;
            return this;
        }

        @Override
        public AddPropertiesAuthorization build() {
            if (path == null) {
                throw new IllegalArgumentException("Path of the property file must be specified as non null value");
            }
            if (path.isEmpty()) {
                throw new IllegalArgumentException("Path of the property file must not be empty value");
            }
            return new AddPropertiesAuthorization(this);
        }
    }
}
