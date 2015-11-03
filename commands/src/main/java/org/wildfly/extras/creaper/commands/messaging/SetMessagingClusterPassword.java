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
 * Set cluster password.
 */
public final class SetMessagingClusterPassword implements OfflineCommand, OnlineCommand {

    private final String serverName;
    private final String password;

    /**
     * Set cluster password in the default messaging server.
     *
     * @param password password to be set
     */
    public SetMessagingClusterPassword(String password) {
        this(password, MessagingUtils.DEFAULT_SERVER_NAME);
    }

    /**
     * Set cluster password in the specified messaging server. <b>NOT YET
     * IMPLEMENTED FOR OFFLINE!</b>
     *
     * @param password password to be set
     * @param serverName name of the messaging server
     */
    public SetMessagingClusterPassword(String password, String serverName) {
        if (password == null) {
            throw new IllegalArgumentException("Password must be specified as non null value");
        }
        if (serverName == null) {
            throw new IllegalArgumentException("Messaging server name must be specified as non null value");
        }
        this.serverName = serverName;
        this.password = password;
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {
        GroovyXmlTransform enableSecurity = GroovyXmlTransform
                .of(SetMessagingClusterPassword.class)
                .subtree("messagingHornetq", Subtree.subsystem("messaging"))
                .subtree("messagingActivemq", Subtree.subsystem("messaging-activemq"))
                .parameter("password", password)
                .build();
        ctx.client.apply(enableSecurity);
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);

        Address address = MessagingUtils.address(ctx.client, serverName);

        ops.writeAttribute(address, "cluster-password", password);
    }
}
