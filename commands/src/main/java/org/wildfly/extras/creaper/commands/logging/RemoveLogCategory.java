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

/**
 * @author Ivan Straka istraka@redhat.com
 */

public class RemoveLogCategory implements OfflineCommand, OnlineCommand{

    private String category;

    public RemoveLogCategory (String category) {
        this.category = category;
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        GroovyXmlTransform transform = GroovyXmlTransform.of(RemoveLogCategory.class)
                .subtree("logging", Subtree.subsystem("logging"))

                .parameter("category", category)

                .build();

        ctx.client.apply(transform);
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);

        Address handlerAddress = Address.subsystem("logging").and("logger", category);

        try {
            ops.removeIfExists(handlerAddress);
        } catch (OperationException e) {
            throw new CommandFailedException("Failed to remove existing periodic-rotating-file-handler " + category, e);
        }
    }
}
