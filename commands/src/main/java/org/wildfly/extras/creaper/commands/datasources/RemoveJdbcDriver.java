package org.wildfly.extras.creaper.commands.datasources;

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
 * Command which removes an existing JDBC driver from the {@code datasources} subsystem.
 */
public final class RemoveJdbcDriver implements OnlineCommand, OfflineCommand {
    private final String driverName;

    public RemoveJdbcDriver(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name of the driver must be specified as non null value");
        }
        this.driverName = name;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws IOException {
        Operations ops = new Operations(ctx.client);
        ops.remove(Address.subsystem("datasources").and("jdbc-driver", driverName));
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException {
        GroovyXmlTransform transform = GroovyXmlTransform.of(RemoveJdbcDriver.class)
                .subtree("datasources", Subtree.subsystem("datasources"))
                .parameter("driverName", driverName)
                .build();
        ctx.client.apply(transform);
    }

    @Override
    public String toString() {
        return "RemoveJdbcDriver " + driverName;
    }
}
