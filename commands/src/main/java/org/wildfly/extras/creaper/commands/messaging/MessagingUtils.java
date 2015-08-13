package org.wildfly.extras.creaper.commands.messaging;

import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;

import java.util.List;

final class MessagingUtils {
    private MessagingUtils() {}

    static final String DEFAULT_SERVER_NAME = "default";

    /**
     * Finds an address of the messaging server in the messaging subsystem of a running server.
     *
     * @throws CommandFailedException when neither ActiveMQ nor HornetQ messaging server of given name exists
     */
    static Address address(OnlineManagementClient client, String serverName) throws CommandFailedException {
        Operations ops = new Operations(client);

        Address hornetqAddress = Address.subsystem("messaging").and("hornetq-server", serverName);
        Address artemisAddress = Address.subsystem("messaging-activemq").and("server", serverName);
        Address selectedAddress = null;

        try {
            if (ops.exists(artemisAddress)) {
                selectedAddress = artemisAddress;
            }
        } catch (Exception ignored) {
            // no resource definition found
        }
        try {
            if (ops.exists(hornetqAddress)) {
                selectedAddress = hornetqAddress;
            }
        } catch (Exception ignored) {
            // no resource definition found
        }

        if (selectedAddress == null) {
            throw new CommandFailedException("The messaging server '" + serverName
                    + "' doesn't exist. Does the ActiveMQ or HornetQ messaging subsystem exist?");
        }

        return selectedAddress;
    }

    static String getStringOfEntries(List<String> jndiEntries) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < jndiEntries.size(); i++) {
            result.append(jndiEntries.get(i));
            if (i != jndiEntries.size() - 1) {
                result.append(" ");
            }
        }
        return result.toString();
    }
}
