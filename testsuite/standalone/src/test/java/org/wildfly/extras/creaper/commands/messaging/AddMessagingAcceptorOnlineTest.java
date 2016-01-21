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
public class AddMessagingAcceptorOnlineTest {

    private static final String TEST_ACCEPTOR = "testAcceptor";

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
                        .and("remote-acceptor", TEST_ACCEPTOR));
        ops.removeIfExists(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME)
                        .and("acceptor", TEST_ACCEPTOR));
        ops.removeIfExists(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME)
                        .and("in-vm-acceptor", TEST_ACCEPTOR));
        client.close();
    }

    @Test
    public void addInVmAcceptor() throws CommandFailedException, IOException {
        int serverId = 145872154;

        AddMessagingAcceptor addMessagingAcceptor = new AddMessagingAcceptor.InVmBuilder(TEST_ACCEPTOR)
                .serverId(serverId)
                .build();

        client.apply(addMessagingAcceptor);

        ModelNodeResult result = ops.readAttribute(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME).and("in-vm-acceptor", TEST_ACCEPTOR),
                "server-id");
        result.assertSuccess();
    }

    @Test
    public void addGenericAcceptor() throws CommandFailedException, IOException {
        AddMessagingAcceptor addMessagingAcceptor = new AddMessagingAcceptor.GenericBuilder(TEST_ACCEPTOR)
                .socketBinding("testSocketBinding")
                .factoryClass("testClass")
                .build();

        client.apply(addMessagingAcceptor);

        ModelNodeResult result = ops.readAttribute(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME).and("acceptor", TEST_ACCEPTOR),
                "socket-binding");
        result.assertSuccess();
    }

    @Test
    public void addRemoteAcceptor() throws CommandFailedException, IOException {
        AddMessagingAcceptor addMessagingAcceptor = new AddMessagingAcceptor.RemoteBuilder(TEST_ACCEPTOR)
                .socketBinding("testSocketBinding")
                .build();

        client.apply(addMessagingAcceptor);

        ModelNodeResult result = ops.readAttribute(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME)
                        .and("remote-acceptor", TEST_ACCEPTOR),
                "socket-binding");
        result.assertSuccess();
    }
}
