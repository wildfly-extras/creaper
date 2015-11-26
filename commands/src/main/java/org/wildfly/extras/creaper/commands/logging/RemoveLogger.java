package org.wildfly.extras.creaper.commands.logging;

import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;

public final class RemoveLogger implements OnlineCommand, OfflineCommand {
    private final String category;

    public RemoveLogger(String category) {
        if (category == null) {
            throw new IllegalArgumentException("Category can not be null.");
        }

        this.category = category;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);

        ops.removeIfExists(Address.subsystem("logging").and("logger", category));
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        GroovyXmlTransform transform = GroovyXmlTransform.of(RemoveLogger.class)
                .subtree("logging", Subtree.subsystem("logging"))
                .parameter("category", category)
                .build();

        ctx.client.apply(transform);
    }

    @Override
    public String toString() {
        return "RemoveLogger " + category;
    }
}
