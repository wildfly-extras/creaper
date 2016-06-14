package org.wildfly.extras.creaper.commands.socketbindings;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Assert;
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

import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class RemoveSocketBindingTest {

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
    public void removeSocketBinding() throws CommandFailedException, IOException, OperationException {
        AddSocketBinding addSocketBinding = new AddSocketBinding.Builder(SOCKET_BINDING_NAME)
                .build();
        client.apply(addSocketBinding);
        Assert.assertTrue(ops.exists(TEST_SOCKET_BINDING_ADDRESS));

        RemoveSocketBinding removeSocketBinding = new RemoveSocketBinding(SOCKET_BINDING_NAME);
        client.apply(removeSocketBinding);
        Assert.assertFalse(ops.exists(TEST_SOCKET_BINDING_ADDRESS));
    }

    @Test
    public void removeSocketBinding_bindingGroupSpecified() throws CommandFailedException, IOException,
            OperationException {
        AddSocketBinding addSocketBinding = new AddSocketBinding.Builder(SOCKET_BINDING_NAME)
                .build();
        client.apply(addSocketBinding);
        Assert.assertTrue(ops.exists(TEST_SOCKET_BINDING_ADDRESS));

        RemoveSocketBinding removeSocketBinding = new RemoveSocketBinding(SOCKET_BINDING_NAME, SOCKET_BINDING_GROUP);
        client.apply(removeSocketBinding);
        Assert.assertFalse(ops.exists(TEST_SOCKET_BINDING_ADDRESS));
    }

    @Test(expected = CommandFailedException.class)
    public void removeNonExistingSocketBinding() throws CommandFailedException, IOException, OperationException {
        RemoveSocketBinding removeSocketBinding = new RemoveSocketBinding("non-existing-socket-binding");
        client.apply(removeSocketBinding);

        fail("Specified socket-binding does not exist in the configuration, an exception should be thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeSocketBinding_nullName() throws Exception {
        new RemoveSocketBinding(null);
        fail("Creating command with null name should throw an exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeSocketBinding_emptyName() throws Exception {
        new RemoveSocketBinding("");
        fail("Creating command with empty name should throw an exception");
    }
}
