package org.wildfly.extras.creaper.commands.messaging;

import java.io.IOException;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.CliException;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class DisableEnableSecurityOnlineTest {

    private OnlineManagementClient client;
    private Operations ops;

    @Before
    public void connect() throws IOException {
        client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
        ops = new Operations(client);
    }

    @After
    public void close() throws IOException, CliException, OperationException,
            CommandFailedException {
        client.close();
    }

    @Test
    public void disableTest() throws Exception {
        disable();
    }

    @Test
    public void enableTest() throws Exception {
        enable();
    }

    @Test
    public void disableEnableTest() throws Exception {
        disable();
        enable();
    }

    @Test
    public void enableDisableTest() throws Exception {
        enable();
        disable();
    }

    public void disable() throws Exception {
        client.apply(new DisableMessagingSecurity());
        ModelNodeResult result = ops.readAttribute(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME),
                "security-enabled");
        result.assertSuccess();
        assertFalse(result.booleanValue());
    }

    public void enable() throws Exception {
        client.apply(new EnableMessagingSecurity());
        ModelNodeResult result = ops.readAttribute(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME),
                "security-enabled");
        result.assertSuccess();
        assertTrue(result.booleanValue());
    }
}
