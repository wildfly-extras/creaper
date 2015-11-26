package org.wildfly.extras.creaper.commands.logging;

import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;

public final class RemoveLogHandler implements OnlineCommand, OfflineCommand {
    private final LogHandlerType type;
    private final String name;

    public RemoveLogHandler(LogHandlerType type, String name) {
        if (type == null) {
            throw new IllegalArgumentException("Handler type can not be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Handler name can not be null.");
        }

        this.type = type;
        this.name = name;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);

        ops.removeIfExists(Address.subsystem("logging").and(type.value(), name));
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        GroovyXmlTransform transform = GroovyXmlTransform.of(RemoveLogHandler.class)
                .subtree("logging", Subtree.subsystem("logging"))
                .parameter("name", name)
                .parameter("type", type.value())
                .build();

        ctx.client.apply(transform);
    }

    @Override
    public String toString() {
        return "RemoveLogHandler " + name;
    }
}
