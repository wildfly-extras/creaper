package org.wildfly.extras.creaper.commands.undertow;

import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;

import java.io.IOException;

/**
 * Command which removes specified security realm.
 */
public final class RemoveHttpsSecurityRealm implements OnlineCommand {
    private final String realmName;

    public RemoveHttpsSecurityRealm(String realmName) {
        this.realmName = realmName;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws IOException {
        Operations ops = new Operations(ctx.client);
        ops.remove(Address.coreService("management").and("security-realm", realmName));
    }
}
