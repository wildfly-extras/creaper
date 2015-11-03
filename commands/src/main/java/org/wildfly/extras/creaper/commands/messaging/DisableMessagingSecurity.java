package org.wildfly.extras.creaper.commands.messaging;

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
 * Disable security in messaging subsystem.
 */
public final class DisableMessagingSecurity implements OfflineCommand, OnlineCommand {

    private final String serverName;

    /**
     * Disable security in the default messaging server.
     */
    public DisableMessagingSecurity() {
        this(MessagingUtils.DEFAULT_SERVER_NAME);
    }

    /**
     * Disable security in the specified messaging server. <b>NOT YET
     * IMPLEMENTED FOR OFFLINE!</b>
     *
     * @param serverName name of the messaging server
     */
    public DisableMessagingSecurity(String serverName) {
        if (serverName == null) {
            throw new IllegalArgumentException("Messaging server name must be specified as non null value");
        }
        this.serverName = serverName;
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {
        GroovyXmlTransform disableSecurity = GroovyXmlTransform
                .of(DisableMessagingSecurity.class)
                .subtree("messagingHornetq", Subtree.subsystem("messaging"))
                .subtree("messagingActivemq", Subtree.subsystem("messaging-activemq"))
                .build();
        ctx.client.apply(disableSecurity);
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);

        Address address = MessagingUtils.address(ctx.client, serverName);

        ops.writeAttribute(address, "security-enabled", false);
    }
}
