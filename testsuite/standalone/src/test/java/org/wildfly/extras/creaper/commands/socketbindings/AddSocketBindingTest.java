package org.wildfly.extras.creaper.commands.socketbindings;

import org.junit.Assert;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.online.CliException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
public class AddSocketBindingTest {
    private static final String SOCKET_BINDING_NAME = "creaper-sb";
    private static final String SOCKET_BINDING_GROUP = "standard-sockets";
    private static final Address TEST_SOCKET_BINDING_ADDRESS = Address.of("socket-binding-group", SOCKET_BINDING_GROUP)
            .and("socket-binding", SOCKET_BINDING_NAME);
    private OnlineManagementClient client;
    private Operations ops;
    private Administration administration;

    @Before
    public void connect() throws IOException {
        client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
        ops = new Operations(client);
        administration = new Administration(client);
    }

    @After
    public void cleanup() throws IOException, CliException, OperationException, TimeoutException, InterruptedException {
        try {
            ops.removeIfExists(TEST_SOCKET_BINDING_ADDRESS);
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void addSocketBinding() throws CommandFailedException, IOException, OperationException {
        AddSocketBinding addSocketBinding = new AddSocketBinding.Builder(SOCKET_BINDING_NAME)
                .build();
        client.apply(addSocketBinding);
        Assert.assertTrue(ops.exists(TEST_SOCKET_BINDING_ADDRESS));
    }
}
