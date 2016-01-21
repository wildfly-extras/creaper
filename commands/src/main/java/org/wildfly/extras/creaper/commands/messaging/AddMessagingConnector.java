package org.wildfly.extras.creaper.commands.messaging;

/**
 * Command which creates and adds a connector to given server
 */
public final class AddMessagingConnector extends AbstractTransportConfigAddCommand {

    private AddMessagingConnector(InVmBuilder builder) {
        super(builder, TransportConfigItem.CONNECTOR);
    }

    private AddMessagingConnector(GenericBuilder builder) {
        super(builder, TransportConfigItem.CONNECTOR);
    }

    private AddMessagingConnector(RemoteBuilder builder) {
        super(builder, TransportConfigItem.CONNECTOR);
    }

    public static final class InVmBuilder extends AbstractTransportConfigAddCommand.InVmBuilder<InVmBuilder> {

        public InVmBuilder(String name) {
            super(name);
        }

        public InVmBuilder(String name, String serverName) {
            super(name, serverName);
        }

        @Override
        public AddMessagingConnector build() {
            validate();
            return new AddMessagingConnector(this);
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
        public AddMessagingConnector build() {
            validate();
            return new AddMessagingConnector(this);
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
        public AddMessagingConnector build() {
            validate();
            return new AddMessagingConnector(this);
        }
    }
}
