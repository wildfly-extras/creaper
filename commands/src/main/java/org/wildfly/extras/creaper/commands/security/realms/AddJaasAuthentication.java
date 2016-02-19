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
 * Add jaas authentication to security realm.
 */
public final class AddJaasAuthentication extends AbstractAddSecurityRealmSubElement {

    private final String name;
    private final Boolean assignGroups;

    private AddJaasAuthentication(Builder builder) {
        super(builder);
        this.name = builder.name;
        this.assignGroups = builder.assignGroups;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (assignGroups != null) {
            if (ctx.version.lessThan(ServerVersion.VERSION_1_7_0)
                    || ctx.version.inRange(ServerVersion.VERSION_2_0_0, ServerVersion.VERSION_2_2_0)) {
                throw new AssertionError("Option assign-groups is available since WildFly 9 or in EAP 6.4.x.");
            }
        }

        Operations ops = new Operations(ctx.client);
        Address securityRealmJaasAuthnAddress = securityRealmAddress.and("authentication", "jaas");
        if (replaceExisting) {
            ops.removeIfExists(securityRealmJaasAuthnAddress);
            new Administration(ctx.client).reloadIfRequired();
        }
        ops.add(securityRealmJaasAuthnAddress, Values.empty()
                .andOptional("name", name)
                .andOptional("assign-groups", assignGroups));
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        if (assignGroups != null) {
            if (ctx.version.lessThan(ServerVersion.VERSION_1_7_0)
                    || ctx.version.inRange(ServerVersion.VERSION_2_0_0, ServerVersion.VERSION_2_2_0)) {
                throw new AssertionError("Option assign-groups is available since WildFly 9 or in EAP 6.4.x.");
            }
        }

        ctx.client.apply(GroovyXmlTransform.of(AddJaasAuthentication.class)
                .subtree("management", Subtree.management())
                .parameter("atrSecurityRealmName", securityRealmName)
                .parameter("atrName", name)
                .parameter("atrAssignGroups", assignGroups)
                .parameter("atrReplaceExisting", replaceExisting)
                .build());
    }

    public static final class Builder extends AbstractAddSecurityRealmSubElement.Builder<Builder> {

        private String name;
        private Boolean assignGroups;

        public Builder(String securityRealmName) {
            super(securityRealmName);
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * <b>This option is available since WildFly 9 or in EAP 6.4.x!</b>
         */
        public Builder assignGroups(Boolean assignGroups) {
            this.assignGroups = assignGroups;
            return this;
        }

        @Override
        public AddJaasAuthentication build() {
            if (name == null) {
                throw new IllegalArgumentException("Name of the security domain must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the security domain must not be empty value");
            }
            return new AddJaasAuthentication(this);
        }
    }

}
