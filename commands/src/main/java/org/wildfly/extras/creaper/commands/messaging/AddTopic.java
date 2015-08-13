package org.wildfly.extras.creaper.commands.messaging;

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
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.List;

/**
 * Creates new messaging topic.
 */
public final class AddTopic implements OnlineCommand, OfflineCommand {
    private final String name;
    private final String serverName;
    private List<String> jndiEntries;
    private final boolean replaceExisting;

    private AddTopic(Builder builder) {
        this.name = builder.name;
        this.serverName = builder.serverName;
        this.jndiEntries = builder.jndiEntries;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws IOException, CommandFailedException {
        Operations ops = new Operations(ctx.client);

        Address address = MessagingUtils.address(ctx.client, serverName).and("jms-topic", name);

        if (replaceExisting) {
            try {
                ops.removeIfExists(address);
            } catch (OperationException e) {
                throw new CommandFailedException("Failed to remove existing topic " + name, e);
            }
        }

        ops.add(address, Values.empty()
                .andList(String.class, "entries", jndiEntries));
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {
        if (!MessagingUtils.DEFAULT_SERVER_NAME.equals(serverName)) {
            throw new CommandFailedException("Non-default messaging server name not yet implemented in offline mode");
        }

        GroovyXmlTransform transform = GroovyXmlTransform.of(AddTopic.class)
                .subtree("messagingHornetq", Subtree.subsystem("messaging"))
                .subtree("messagingActivemq", Subtree.subsystem("messaging-activemq"))
                .parameter("name", name)
                .parameter("entries", jndiEntries)
                .parameter("entriesString", MessagingUtils.getStringOfEntries(jndiEntries))
                .parameter("replaceExisting", replaceExisting)
                .build();

        ctx.client.apply(transform);
    }

    @Override
    public String toString() {
        return "AddTopic " + name;
    }

    public static final class Builder {
        private final String name;
        private final String serverName;
        private List<String> jndiEntries;
        private boolean replaceExisting;

        /**
         * Adds a topic to the default messaging server.
         * @param name name of the topic
         */
        public Builder(String name) {
            this(name, MessagingUtils.DEFAULT_SERVER_NAME);
        }

        /**
         * Adds a topic to the specified messaging server. <b>NOT YET IMPLEMENTED FOR OFFLINE!</b>
         * @param name name of the topic
         * @param serverName name of the messaging server
         */
        public Builder(String name, String serverName) {
            if (name == null) {
                throw new IllegalArgumentException("Topic name must be specified as non null value");
            }
            if (serverName == null) {
                throw new IllegalArgumentException("Messaging server name must be specified as non null value");
            }

            this.name = name;
            this.serverName = serverName;
        }

        /**
         * Defines the list of jndi entries to which this topic is bound to.
         */
        public Builder jndiEntries(List<String> jndiEntries) {
            this.jndiEntries = jndiEntries;
            return this;
        }

        /**
         * Specify whether to replace the existing topic based on its name. By
         * default existing topic is not replaced and exception is thrown.
         */
        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddTopic build() {
            check();
            return new AddTopic(this);
        }

        private void check() {
            if (jndiEntries == null || jndiEntries.isEmpty()) {
                throw new IllegalArgumentException("At least one jndi entry needs to be specified for topic");
            }
        }
    }
}
