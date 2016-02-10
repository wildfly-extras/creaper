package org.wildfly.extras.creaper.commands.security.realms;

import java.io.IOException;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

/**
 * Add a new security realm.
 */
public final class AddSecurityRealm implements OnlineCommand, OfflineCommand {

    private final String securityRealmName;
    private final Boolean mapGroupsToRoles;
    private final boolean replaceExisting;

    private AddSecurityRealm(Builder builder) {
        this.securityRealmName = builder.securityRealmName;
        this.mapGroupsToRoles = builder.mapGroupsToRoles;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address securityRealmAddress = Address.coreService("management").and("security-realm", securityRealmName);
        if (replaceExisting) {
            try {
                ops.removeIfExists(securityRealmAddress);
                new Administration(ctx.client).reloadIfRequired();
            } catch (OperationException e) {
                throw new IOException("Failed to remove existing security realm " + securityRealmName, e);
            }
        }
        ops.add(securityRealmAddress, Values.empty()
                .andOptional("map-groups-to-roles", mapGroupsToRoles));
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        ctx.client.apply(GroovyXmlTransform.of(AddSecurityRealm.class)
                .subtree("management", Subtree.management())
                .parameter("atrSecurityRealmName", securityRealmName)
                .parameter("atrMapGroupsToRoles", mapGroupsToRoles)
                .parameter("atrReplaceExisting", replaceExisting)
                .build());
    }

    public static final class Builder {

        private String securityRealmName;
        private Boolean mapGroupsToRoles;
        private boolean replaceExisting;

        public Builder(String securityRealmName) {
            if (securityRealmName == null) {
                throw new IllegalArgumentException("Name of the security-realm must be specified as non null value");
            }
            if (securityRealmName.isEmpty()) {
                throw new IllegalArgumentException("Name of the security-realm must not be empty value");
            }
            this.securityRealmName = securityRealmName;
        }

        public Builder mapGroupsToRoles(Boolean mapGroupsToRoles) {
            this.mapGroupsToRoles = mapGroupsToRoles;
            return this;
        }

        /**
         * <b>This can cause server reload!</b>
         */
        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddSecurityRealm build() {
            return new AddSecurityRealm(this);
        }
    }
}
