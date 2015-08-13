package org.wildfly.extras.creaper.commands.messaging;

import org.jboss.arquillian.junit.Arquillian;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.online.CliException;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RunWith(Arquillian.class)
public class AddQueueOnlineTest {
    private static final String TEST_QUEUE_NAME = "queueTest";

    private static final List<String> JNDI_ENTRIES = Collections.singletonList("jms/queue/" + TEST_QUEUE_NAME);

    private OnlineManagementClient client;
    private Operations ops;

    @Before
    public void connect() throws IOException {
        client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
        ops = new Operations(client);
    }

    @After
    public void close() throws IOException, CliException, OperationException, CommandFailedException {
        ops.removeIfExists(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME).and("jms-queue", TEST_QUEUE_NAME));
        client.close();
    }

    @Test
    public void addSimpleQueue_commandSucceeds() throws CommandFailedException, IOException {
        client.apply(new AddQueue.Builder(TEST_QUEUE_NAME)
                .jndiEntries(JNDI_ENTRIES)
                .build());

        ModelNodeResult result = ops.readAttribute(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME).and("jms-queue", TEST_QUEUE_NAME),
                "durable");
        result.assertSuccess();
    }

    @Test
    public void addQueueWithAllParameters_commandSucceeds() throws CommandFailedException, IOException {
        client.apply(new AddQueue.Builder(TEST_QUEUE_NAME)
                .jndiEntries(JNDI_ENTRIES)
                .durable(false)
                .selector("xxx")
                .build());

        ModelNodeResult result = ops.readAttribute(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME).and("jms-queue", TEST_QUEUE_NAME),
                "durable");
        result.assertSuccess();
    }

    @Test
    public void addDuplicatedQueue_commandSucceeds() throws CommandFailedException, IOException {
        client.apply(new AddQueue.Builder(TEST_QUEUE_NAME)
                .jndiEntries(JNDI_ENTRIES)
                .durable(true)
                .build());

        ModelNodeResult result = ops.readAttribute(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME).and("jms-queue", TEST_QUEUE_NAME),
                "durable");
        result.assertSuccess();

        client.apply(new AddQueue.Builder(TEST_QUEUE_NAME, "default")
                .jndiEntries(JNDI_ENTRIES)
                .replaceExisting()
                .build());

        result = ops.readAttribute(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME).and("jms-queue", TEST_QUEUE_NAME),
                "durable");
        result.assertSuccess();
    }

}
