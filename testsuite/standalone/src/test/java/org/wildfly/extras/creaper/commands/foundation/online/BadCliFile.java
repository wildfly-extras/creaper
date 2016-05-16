package org.wildfly.extras.creaper.commands.foundation.online;

import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;

public class BadCliFile implements OnlineCommand {
    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        ctx.client.apply(new CliFile(BadCliFile.class));
    }
}
