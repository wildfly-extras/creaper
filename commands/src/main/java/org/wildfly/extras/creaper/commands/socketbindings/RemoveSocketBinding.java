package org.wildfly.extras.creaper.commands.socketbindings;

import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;

/**
 * A command for removing socket binding from default socket binding group if not specified else.
 */
public final class RemoveSocketBinding implements OnlineCommand {

    private final String socketBindingName;
    private final String socketBindingGroup;

    public RemoveSocketBinding(String socketBindingName) {
        this(socketBindingName, null);
    }

    public RemoveSocketBinding(String socketBindingName, String socketBindingGroup) {
        if (socketBindingName == null) {
            throw new IllegalArgumentException("Name of the socket-binding must be specified as non null value");
        }
        if (socketBindingName.isEmpty()) {
            throw new IllegalArgumentException("Name of the socket-binding must not be empty value");
        }

        this.socketBindingName = socketBindingName;
        this.socketBindingGroup = socketBindingGroup;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);

        String bindingGroup = this.socketBindingGroup;
        if (bindingGroup == null) {
            bindingGroup = ctx.client.options().isDomain ? "full-sockets" : "standard-sockets";
        }

        Address socketBindingAddress = Address.of("socket-binding-group", bindingGroup)
                .and("socket-binding", socketBindingName);

        ops.remove(socketBindingAddress);
    }

}
