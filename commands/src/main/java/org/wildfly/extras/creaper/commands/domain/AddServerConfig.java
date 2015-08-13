package org.wildfly.extras.creaper.commands.domain;

import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.Constants;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;

/**
 * Command which creates new server config under {@code /host=some-host/server-config=server-name}
 * TODO: add an offline implementation
 */
public class AddServerConfig implements OnlineCommand {
    private final String serverName;
    private final String host;
    private final String socketBindingGroup;
    private final Integer socketBindingPortOffset;
    private final Boolean autoStart;
    private final String serverGroup;

    private AddServerConfig(Builder builder) {
        this.serverName = builder.serverName;
        this.host = builder.host;
        this.serverGroup = builder.serverGroup;
        this.socketBindingGroup = builder.socketBindingGroup;
        this.socketBindingPortOffset = builder.socketBindingPortOffset;
        this.autoStart = builder.autoStart;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws CommandFailedException, IOException {
        if (!ctx.client.options().isDomain) {
            throw new CommandFailedException("This command only makes sense in domain");
        }

        Operations ops = new Operations(ctx.client);
        ops.add(getAddress(), Values.empty()
                .andOptional("group", serverGroup)
                .andOptional("socket-binding-group", socketBindingGroup)
                .andOptional("socket-binding-port-offset", socketBindingPortOffset)
                .andOptional("auto-start", autoStart));
    }

    /**
     * @return address of newly created {@code server-config}, e.g. {@code /host=master/server-config=new-server}
     */
    public Address getAddress() {
        return Address.host(host).and(Constants.SERVER_CONFIG, serverName);
    }

    public static final class Builder {
        private String serverName;
        private String host;
        private String serverGroup;
        private String socketBindingGroup;
        private Integer socketBindingPortOffset;
        private Boolean autoStart;

        /**
         * @param serverName name of newly created {@code server-config}
         * @param host target host on which new server will be created
         * @param serverGroup server group to which new server will belong
         * @throws IllegalArgumentException if the {@code serverName} or {@code host} or {@code serverGroup}
         * is {@code null}
         */
        public Builder(String serverName, String host, String serverGroup) {
            if (serverName == null) {
                throw new IllegalArgumentException("Name of the server must be specified as non null value");
            }
            if (host == null) {
                throw new IllegalArgumentException("host must be specified as non null value");
            }
            if (serverGroup == null) {
                throw new IllegalArgumentException("serverGroup must be specified as non null value");
            }
            this.serverName = serverName;
            this.host = host;
            this.serverGroup = serverGroup;
        }

        /**
         * The socket binding group to which this server belongs.
         */
        public Builder socketBindingGroup(String socketBindingGroup) {
            this.socketBindingGroup = socketBindingGroup;
            return this;
        }

        /**
         * An offset to be added to the port values given by the socket binding group for this server.
         */
        public Builder socketBindingPortOffset(int socketBindingPortOffset) {
            this.socketBindingPortOffset = socketBindingPortOffset;
            return this;
        }

        /**
         * Whether or not this server should be started when the Host Controller starts.
         */
        public Builder autoStart(boolean autoStart) {
            this.autoStart = autoStart;
            return this;
        }

        public AddServerConfig build() {
            return new AddServerConfig(this);
        }
    }
}
