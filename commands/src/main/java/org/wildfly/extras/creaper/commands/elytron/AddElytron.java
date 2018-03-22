package org.wildfly.extras.creaper.commands.elytron;

import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddElytron implements OnlineCommand {

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (ctx.version.lessThan(ServerVersion.VERSION_5_0_0)) {
            throw new AssertionError("Elytron is available since WildFly 11.");
        }
        Operations ops = new Operations(ctx.client);
        ops.add(Address.extension("org.wildfly.extension.elytron"));
        ops.add(Address.subsystem("elytron"));
        new Administration(ctx.client).reloadIfRequired();
    }

}
