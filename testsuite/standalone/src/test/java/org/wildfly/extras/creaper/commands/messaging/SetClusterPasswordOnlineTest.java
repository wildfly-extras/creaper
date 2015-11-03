package org.wildfly.extras.creaper.commands.messaging;

import java.io.IOException;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Assert;
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

@RunWith(Arquillian.class)
public class SetClusterPasswordOnlineTest {

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
    public void changeTest() throws Exception {
        change("pswd1");
    }

    @Test
    public void changeTwoTimesTest() throws Exception {
        change("heslo1");
        change("heslo2");
    }

    public void change(String pswd) throws Exception {
        client.apply(new SetMessagingClusterPassword(pswd));
        ModelNodeResult result = ops.readAttribute(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME),
                "cluster-password");
        result.assertSuccess();
        Assert.assertEquals(result.stringValue(), pswd);
    }
}
