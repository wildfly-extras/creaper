package org.wildfly.extras.creaper.commands.messaging;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;

import java.io.IOException;

@RunWith(Arquillian.class)
public class AddMessagingConnectorOnlineTest {

    private static final String TEST_CONNECTOR = "testConnector";

    private OnlineManagementClient client;
    private Operations ops;

    @Before
    public void connect() throws IOException {
        client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
        ops = new Operations(client);
    }

    @After
    public void after() throws CommandFailedException, IOException, OperationException {
        ops.removeIfExists(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME)
                        .and("remote-connector", TEST_CONNECTOR));
        ops.removeIfExists(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME)
                        .and("connector", TEST_CONNECTOR));
        ops.removeIfExists(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME)
                        .and("in-vm-connector", TEST_CONNECTOR));
        client.close();
    }

    @Test
    public void addInVmConnector() throws CommandFailedException, IOException {
        int serverId = 145872154;

        AddMessagingConnector addMessagingConnector = new AddMessagingConnector.InVmBuilder(TEST_CONNECTOR)
                .serverId(serverId)
                .build();

        client.apply(addMessagingConnector);

        ModelNodeResult result = ops.readAttribute(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME)
                        .and("in-vm-connector", TEST_CONNECTOR),
                "server-id");
        result.assertSuccess();
    }

    @Test
    public void addGenericConnector() throws CommandFailedException, IOException {
        AddMessagingConnector addMessagingConnector = new AddMessagingConnector.GenericBuilder(TEST_CONNECTOR)
                .socketBinding("testSocketBinding")
                .factoryClass("testClass")
                .build();

        client.apply(addMessagingConnector);

        ModelNodeResult result = ops.readAttribute(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME)
                        .and("connector", TEST_CONNECTOR),
                "socket-binding");
        result.assertSuccess();
    }

    @Test
    public void addRemoteConnector() throws CommandFailedException, IOException {
        AddMessagingConnector addMessagingConnector = new AddMessagingConnector.RemoteBuilder(TEST_CONNECTOR)
                .socketBinding("testSocketBinding")
                .build();

        client.apply(addMessagingConnector);

        ModelNodeResult result = ops.readAttribute(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME)
                        .and("remote-connector", TEST_CONNECTOR),
                "socket-binding");
        result.assertSuccess();
    }
}
