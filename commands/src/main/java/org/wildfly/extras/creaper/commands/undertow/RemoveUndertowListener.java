package org.wildfly.extras.creaper.commands.undertow;

import org.wildfly.extras.creaper.core.ManagementVersion;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;

import java.io.IOException;

/**
 * Command which removes specified Undertow listener.
 */
public final class RemoveUndertowListener implements OnlineCommand {
    private final UndertowListenerType listenerType;
    private final String listenerName;
    private final String serverName;

    private RemoveUndertowListener(Builder builder) {
        this.listenerType = builder.listenerType;
        this.listenerName = builder.listenerName;
        this.serverName = builder.serverName;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws IOException {
        // Undertow is available since WildFly 8
        ctx.serverVersion.assertAtLeast(ManagementVersion.VERSION_2_0_0);

        Operations ops = new Operations(ctx.client);
        Address listenerAddress = Address.subsystem("undertow").and("server", serverName)
                .and(listenerType.listenerTypeName(), listenerName);
        ops.remove(listenerAddress);
    }

    @Override
    public String toString() {
        return "RemoveUndertowListener " + listenerName + " of type " + listenerType.listenerTypeName()
                + " for server " + serverName;
    }

    public static final class Builder {
        private final UndertowListenerType listenerType;
        private final String listenerName;
        private String serverName = UndertowConstants.DEFAULT_SERVER_NAME;

        public Builder(UndertowListenerType listenerType, String listenerName) {
            if (listenerName == null) {
                throw new IllegalArgumentException("Name of the listener must be specified as non null value");
            }
            if (listenerType == null) {
                throw new IllegalArgumentException("Listener type must be specified as non null value");
            }
            this.listenerType = listenerType;
            this.listenerName = listenerName;
        }

        /**
         * Creates the {@code RemoveListener} command for specified Undertow server.
         */
        public RemoveUndertowListener forServer(String serverName) {
            this.serverName = serverName;
            return new RemoveUndertowListener(this);
        }

        /**
         * Creates the {@code RemoveListener} command for the default Undertow server.
         */
        public RemoveUndertowListener forDefaultServer() {
            return new RemoveUndertowListener(this);
        }
    }
}
