package org.wildfly.extras.creaper.commands.logging;

import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;

public class RemoveHandler implements OnlineCommand, OfflineCommand {

    private final HandlerType type;
    private final String name;

    public RemoveHandler(final HandlerType type, final String name) {
        if (type == null || name == null) {
            throw new IllegalArgumentException("Handler type and name can not be null.");
        }
        this.type = type;
        this.name = name;
    }

    @Override
    public final void apply(OfflineCommandContext ctx) throws Exception {
        GroovyXmlTransform transform = GroovyXmlTransform.of(RemoveHandler.class)
                .subtree("logging", Subtree.subsystem("logging"))

                .parameter("name", name)
                .parameter("type", type.value())

                .build();

        ctx.client.apply(transform);
    }

    @Override
    public final void apply(final OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);

        Address handlerAddress = Address.subsystem("logging").and(type.value(), name);

        try {
            ops.removeIfExists(handlerAddress);
        } catch (OperationException e) {
            throw new CommandFailedException(String.format("Failed to remove existing %s %s", type.value(), name), e);
        }

    }
}
