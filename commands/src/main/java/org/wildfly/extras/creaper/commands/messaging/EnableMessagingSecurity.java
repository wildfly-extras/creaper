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
 * Enable security in messaging subsystem.
 */
public final class EnableMessagingSecurity implements OfflineCommand, OnlineCommand {

    private final String serverName;

    /**
     * Enable security in the default messaging server.
     */
    public EnableMessagingSecurity() {
        this(MessagingUtils.DEFAULT_SERVER_NAME);
    }

    /**
     * Enable security in the specified messaging server. <b>NOT YET IMPLEMENTED
     * FOR OFFLINE!</b>
     *
     * @param serverName name of the messaging server
     */
    public EnableMessagingSecurity(String serverName) {
        if (serverName == null) {
            throw new IllegalArgumentException("Messaging server name must be specified as non null value");
        }
        this.serverName = serverName;
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {
        GroovyXmlTransform enableSecurity = GroovyXmlTransform
                .of(EnableMessagingSecurity.class)
                .subtree("messagingHornetq", Subtree.subsystem("messaging"))
                .subtree("messagingActivemq", Subtree.subsystem("messaging-activemq"))
                .build();
        ctx.client.apply(enableSecurity);
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);

        Address address = MessagingUtils.address(ctx.client, serverName);

        ops.writeAttribute(address, "security-enabled", true);
    }
}
