package org.wildfly.extras.creaper.commands.security.realms;

import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

/**
 * Add local authentication to security realm.
 */
public class AddLocalAuthentication extends AbstractAddSecurityRealmSubElement {

    private final String allowedUsers;
    private final String defaultUser;
    private final Boolean skipGroupLoading;

    private AddLocalAuthentication(Builder builder) {
        super(builder);
        this.allowedUsers = builder.allowedUsers;
        this.defaultUser = builder.defaultUser;
        this.skipGroupLoading = builder.skipGroupLoading;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address securityRealmJaasAuthnAddress = securityRealmAddress.and("authentication", "local");
        if (replaceExisting) {
            ops.removeIfExists(securityRealmJaasAuthnAddress);
            new Administration(ctx.client).reloadIfRequired();
        }
        ops.add(securityRealmJaasAuthnAddress, Values.empty()
                .andOptional("allowed-users", allowedUsers)
                .andOptional("default-user", defaultUser)
                .andOptional("skip-group-loading", skipGroupLoading));
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        ctx.client.apply(GroovyXmlTransform.of(AddLocalAuthentication.class)
                .subtree("management", Subtree.management())
                .parameter("atrSecurityRealmName", securityRealmName)
                .parameter("atrAllowedUsers", allowedUsers)
                .parameter("atrDefaultUser", defaultUser)
                .parameter("atrSkipGroupLoading", skipGroupLoading)
                .parameter("atrReplaceExisting", replaceExisting)
                .build());
    }

    public static final class Builder extends AbstractAddSecurityRealmSubElement.Builder<Builder> {
        private String allowedUsers;
        private String defaultUser;
        private Boolean skipGroupLoading;

        public Builder(String securityRealmName) {
            super(securityRealmName);
        }

        public Builder allowedUsers(String allowedUsers) {
            this.allowedUsers = allowedUsers;
            return this;
        }

        public Builder defaultUser(String defaultUser) {
            this.defaultUser = defaultUser;
            return this;
        }

        public Builder skipGroupLoading(Boolean skipGroupLoading) {
            this.skipGroupLoading = skipGroupLoading;
            return this;
        }

        @Override
        public AddLocalAuthentication build() {
            return new AddLocalAuthentication(this);
        }
    }

}
