package org.wildfly.extras.creaper.commands.datasources;

import java.io.IOException;

import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;

/**
 * Command which removes an existing datasource.
 */
public final class RemoveDataSource implements OnlineCommand, OfflineCommand {
    private final String name;

    public RemoveDataSource(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name of the data-source must be specified as non null value");
        }
        this.name = name;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws IOException {
        Operations ops = new Operations(ctx.client);
        ops.remove(Address.subsystem("datasources").and("data-source", name));
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException {
        GroovyXmlTransform transform = GroovyXmlTransform.of(RemoveDataSource.class)
                .subtree("datasources", Subtree.subsystem("datasources"))
                .parameter("name", name)
                .build();
        ctx.client.apply(transform);
    }

    @Override
    public String toString() {
        return "RemoveDataSource " + name;
    }
}
