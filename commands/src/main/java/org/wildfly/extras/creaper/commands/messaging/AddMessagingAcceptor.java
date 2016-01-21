package org.wildfly.extras.creaper.commands.messaging;

/**
 * Command which creates and adds an acceptor to given server
 */
public final class AddMessagingAcceptor extends AbstractTransportConfigAddCommand {

    private AddMessagingAcceptor(InVmBuilder builder) {
        super(builder, TransportConfigItem.ACCEPTOR);
    }

    private AddMessagingAcceptor(GenericBuilder builder) {
        super(builder, TransportConfigItem.ACCEPTOR);
    }

    private AddMessagingAcceptor(RemoteBuilder builder) {
        super(builder, TransportConfigItem.ACCEPTOR);
    }

    public static final class InVmBuilder extends AbstractTransportConfigAddCommand.InVmBuilder<InVmBuilder> {

        public InVmBuilder(String name) {
            super(name);
        }

        public InVmBuilder(String name, String serverName) {
            super(name, serverName);
        }

        @Override
        public AddMessagingAcceptor build() {
            validate();
            return new AddMessagingAcceptor(this);
        }
    }

    public static final class RemoteBuilder extends AbstractTransportConfigAddCommand.RemoteBuilder<RemoteBuilder> {

        public RemoteBuilder(String name) {
            super(name);
        }

        public RemoteBuilder(String name, String serverName) {
            super(name, serverName);
        }

        @Override
        public AddMessagingAcceptor build() {
            validate();
            return new AddMessagingAcceptor(this);
        }
    }

    public static final class GenericBuilder extends AbstractTransportConfigAddCommand.GenericBuilder<GenericBuilder> {

        public GenericBuilder(String name) {
            super(name);
        }

        public GenericBuilder(String name, String serverName) {
            super(name, serverName);
        }

        @Override
        public AddMessagingAcceptor build() {
            validate();
            return new AddMessagingAcceptor(this);
        }
    }
}
