package org.wildfly.extras.creaper.commands.socketbindings;

import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>A command for creating socket binding under default socket binding group if not specified else.</p>
 * <p>This command <b>does not check</b> whether the port or multicast port are available!</p>
 */
public final class AddSocketBinding implements OnlineCommand {

    private final String socketBindingName;
    private final String socketBindingGroup;

    private final List<String> clientMappings;
    private final Boolean fixedPort;
    private final String interfaceName;
    private final String multicastAddress;
    private final Integer multicastPort;
    private final Integer port;
    private final boolean replaceExisting;

    private AddSocketBinding(Builder builder) {
        this.socketBindingName = builder.socketBindingName;
        this.socketBindingGroup = builder.socketBindingGroup;
        this.clientMappings = builder.clientMappings;
        this.fixedPort = builder.fixedPort;
        this.interfaceName = builder.interfaceName;
        this.multicastAddress = builder.multicastAddress;
        this.multicastPort = builder.multicastPort;
        this.port = builder.port;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws CommandFailedException, IOException {
        Operations ops = new Operations(ctx.client);

        String socketBindingGroup = this.socketBindingGroup;
        if (socketBindingGroup == null) {
            socketBindingGroup = ctx.client.options().isDomain ? "full-sockets" : "standard-sockets";
        }

        Address socketBindingAddress = Address.of("socket-binding-group", socketBindingGroup)
                .and("socket-binding", socketBindingName);

        if (replaceExisting) {
            try {
                ops.removeIfExists(socketBindingAddress);
            } catch (OperationException e) {
                throw new CommandFailedException("Failed to remove existing socket binding " + socketBindingName, e);
            }
        }

        ops.add(socketBindingAddress, Values.empty()
                .andListOptional(String.class, "client-mappings", clientMappings)
                .andOptional("fixed-port", fixedPort)
                .andOptional("interface", interfaceName)
                .andOptional("multicast-address", multicastAddress)
                .andOptional("multicast-port", multicastPort)
                .andOptional("port", port));
    }

    public static final class Builder {

        private final String socketBindingName;
        private final String socketBindingGroup;

        private List<String> clientMappings;
        private Boolean fixedPort;
        private String interfaceName;
        private String multicastAddress;
        private Integer multicastPort;
        private Integer port;
        private boolean replaceExisting;

        /**
         * When socket binding group is not selected, default will be set. Default group for standalone mode is
         * <code>standard-sockets</code> and default group for domain mode is <code>full-sockets</code>.
         * <code>full-sockets</code> is <b>default socket group only for all started servers</b> in default server
         * configuration (<code>server-one</code> and <code>server-two</code>).
         */
        public Builder(String socketBindingName) {
            this(socketBindingName, null);
        }

        public Builder(String socketBindingName, String socketBindingGroup) {
            this.socketBindingName = socketBindingName;
            this.socketBindingGroup = socketBindingGroup;
        }


        /**
         * Specifies zero or more client mappings for this socket binding. A client connecting to this socket should use
         * the destination address specified in the mapping that matches its desired outbound interface. This allows for
         * advanced network topologies that use either network address translation, or have bindings on multiple network
         * interfaces to function. Each mapping should be evaluated in declared order, with the first successful match
         * used to determine the destination.
         */
        public Builder clientMappings(String... clientMappingsToAdd) {
            if (this.clientMappings == null && clientMappingsToAdd != null) {
                this.clientMappings = new ArrayList<String>();
            }
            if (clientMappingsToAdd != null) {
                this.clientMappings.addAll(Arrays.asList(clientMappingsToAdd));
            }
            return this;
        }

        /**
         * Whether the port value should remain fixed even if numeric offsets are applied to the other sockets in the
         * socket group.
         */
        public Builder fixedPort(boolean isPortFixed) {
            this.fixedPort = isPortFixed;
            return this;
        }

        /**
         * Name of the interface to which the socket should be bound, or, for multicast sockets, the interface on which
         * it should listen. This should be one of the declared interfaces.
         */
        public Builder interfaceName(String interfaceName) {
            this.interfaceName = interfaceName;
            return this;
        }

        /**
         * Multicast address on which the socket should receive multicast traffic. If unspecified, the socket will not
         * be configured to receive multicast.
         */
        public Builder multicastAddress(String multicastAddress) {
            this.multicastAddress = multicastAddress;
            return this;
        }

        /**
         * Port on which the socket should receive multicast traffic. Must be configured if 'multicast-address' is
         * configured.
         */
        public Builder multicastPort(Integer multicastPort) {
            this.multicastPort = multicastPort;
            return this;
        }

        /**
         * Number of the port to which the socket should be bound.
         */
        public Builder port(Integer port) {
            this.port = port;
            return this;
        }

        /**
         * Specify whether to replace the existing socket binding based on its name.
         */
        public Builder replaceExisting(boolean replaceExisting) {
            this.replaceExisting = replaceExisting;
            return this;
        }

        public AddSocketBinding build() {
            return new AddSocketBinding(this);
        }
    }
}
