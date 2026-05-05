package org.wildfly.extras.creaper.commands.elytron;

import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class RemoveElytron implements OnlineCommand {

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        ops.remove(Address.subsystem("elytron"));
        ops.remove(Address.extension("org.wildfly.extension.elytron"));
        new Administration(ctx.client).reloadIfRequired();
    }

}
