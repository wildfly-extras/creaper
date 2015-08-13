package org.wildfly.extras.creaper.commands.web;

import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;

import java.io.IOException;

/**
 * Command for removing specified connector
 */
public final class RemoveConnector implements OnlineCommand, OfflineCommand {
    private final String connectorName;

    public RemoveConnector(final String connectorName) {
        if (connectorName == null) {
            throw new IllegalArgumentException("Connector name must be specified");
        }
        this.connectorName = connectorName;
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException {
        ctx.client.apply(GroovyXmlTransform.of(RemoveConnector.class)
                .subtree("web", Subtree.subsystem("web"))
                .parameter("connectorName", connectorName)
                .build());
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws IOException {
        Operations ops = new Operations(ctx.client);
        ops.remove(Address.subsystem("web").and("connector", connectorName));
    }
}
