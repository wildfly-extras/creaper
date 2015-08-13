package org.wildfly.extras.creaper.commands.messaging;

import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Operations;

import java.io.IOException;

/**
 * Removes an existing messaging topic.
 */
public final class RemoveTopic implements OnlineCommand, OfflineCommand {
    private final String topicName;
    private final String serverName;

    /**
     * Removes a topic from the default messaging server.
     */
    public RemoveTopic(String topicName) {
        this(topicName, MessagingUtils.DEFAULT_SERVER_NAME);
    }

    /**
     * Removes a topic from the specified messaging server. <b>NOT YET IMPLEMENTED FOR OFFLINE!</b>
     */
    public RemoveTopic(String topicName, String serverName) {
        if (topicName == null) {
            throw new IllegalArgumentException("Name of the topic must be specified as non null value");
        }
        if (serverName == null) {
            throw new IllegalArgumentException("Server name must be specified as non null value");
        }
        this.topicName = topicName;
        this.serverName = serverName;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws IOException, CommandFailedException {
        Operations ops = new Operations(ctx.client);
        ops.remove(MessagingUtils.address(ctx.client, serverName).and("jms-topic", topicName));
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException {
        if (!MessagingUtils.DEFAULT_SERVER_NAME.equals(serverName)) {
            throw new CommandFailedException("Non-default messaging server name not yet implemented in offline mode");
        }

        GroovyXmlTransform transform = GroovyXmlTransform.of(RemoveTopic.class)
                .subtree("messagingHornetq", Subtree.subsystem("messaging"))
                .subtree("messagingActivemq", Subtree.subsystem("messaging-activemq"))
                .parameter("topicName", topicName)
                .build();
        ctx.client.apply(transform);
    }

    @Override
    public String toString() {
        return "RemoveTopic " + topicName;
    }
}
