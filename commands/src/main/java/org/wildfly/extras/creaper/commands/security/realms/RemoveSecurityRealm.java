package org.wildfly.extras.creaper.commands.security.realms;

import java.util.concurrent.TimeoutException;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;

/**
 * Remove given security realm.
 *
 * <b>Can move the server to reload-required state!</b>
 */
public final class RemoveSecurityRealm implements OnlineCommand, OfflineCommand {

    private final String securityRealmName;

    public RemoveSecurityRealm(String securityRealmName) {
        if (securityRealmName == null) {
            throw new IllegalArgumentException("Name of the security-realm must be specified as non null value");
        }
        if (securityRealmName.isEmpty()) {
            throw new IllegalArgumentException("Name of the security-realm must not be empty value");
        }
        this.securityRealmName = securityRealmName;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception,
            TimeoutException, InterruptedException {
        if (ctx.version.greaterThanOrEqualTo(ServerVersion.VERSION_18_0_0)) {
            throw new AssertionError("Legacy security was removed in WildFly 25.");
        }

        Operations ops = new Operations(ctx.client);
        ops.remove(Address.coreService("management").and("security-realm", securityRealmName));
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        if (ctx.version.greaterThanOrEqualTo(ServerVersion.VERSION_18_0_0)) {
            throw new AssertionError("Legacy security was removed in WildFly 25.");
        }

        ctx.client.apply(GroovyXmlTransform.of(RemoveSecurityRealm.class)
                .subtree("management", Subtree.management())
                .parameter("atrSecurityRealmName", securityRealmName)
                .build());
    }

}
