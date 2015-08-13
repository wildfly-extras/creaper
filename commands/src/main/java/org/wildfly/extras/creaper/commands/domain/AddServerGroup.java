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
 * Command which creates new server group under {@code /server-group}
 * TODO: add an offline implementation
 */
public class AddServerGroup implements OnlineCommand {
    private final String groupName;
    private final String profile;
    private final String socketBindingGroup;
    private final Integer socketBindingPortOffset;

    private AddServerGroup(Builder builder) {
        this.groupName = builder.groupName;
        this.profile = builder.profile;
        this.socketBindingGroup = builder.socketBindingGroup;
        this.socketBindingPortOffset = builder.socketBindingPortOffset;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws CommandFailedException, IOException {
        if (!ctx.client.options().isDomain) {
            throw new CommandFailedException("This command only makes sense in domain");
        }

        Operations ops = new Operations(ctx.client);
        ops.add(getAddress(), Values.empty()
                .andOptional("profile", this.profile)
                .andOptional("socket-binding-group", socketBindingGroup)
                .andOptional("socket-binding-port-offset", socketBindingPortOffset));
    }

    /**
     * @return address of newly created {@code server-group}, e.g. {@code /server-group=new-server-group}
     */
    public Address getAddress() {
        return Address.of(Constants.SERVER_GROUP, groupName);
    }

    public static final class Builder {
        private String groupName;
        private String profile;
        private String socketBindingGroup;
        private Integer socketBindingPortOffset;

        /**
         * @param groupName name of newly created {@code server-group}
         * @param profile The profile name
         * @param socketBindingGroup The default socket binding group used for servers associated with this group
         * @throws IllegalArgumentException if the {@code groupName} or {@code profile} or {@code socketBindingGroup}
         * is {@code null}
         */
        public Builder(String groupName, String profile, String socketBindingGroup) {
            if (groupName == null) {
                throw new IllegalArgumentException("Name of the group must be specified as non null value");
            }
            if (profile == null) {
                throw new IllegalArgumentException("profile must be specified as non null value");
            }
            if (socketBindingGroup == null) {
                throw new IllegalArgumentException("socketBindingGroup must be specified as non null value");
            }
            this.groupName = groupName;
            this.profile = profile;
            this.socketBindingGroup = socketBindingGroup;
        }

        /**
         * The default offset to be added to the port values given by the socket binding group.
         */
        public Builder socketBindingPortOffset(int socketBindingPortOffset) {
            this.socketBindingPortOffset = socketBindingPortOffset;
            return this;
        }

        public AddServerGroup build() {
            return new AddServerGroup(this);
        }
    }
}
