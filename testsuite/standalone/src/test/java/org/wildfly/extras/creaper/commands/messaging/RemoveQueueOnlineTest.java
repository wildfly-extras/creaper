package org.wildfly.extras.creaper.commands.messaging;

import org.jboss.arquillian.junit.Arquillian;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class RemoveQueueOnlineTest {
    private Operations ops;
    private OnlineManagementClient client;
    private Administration administration;

    private static final String TEST_QUEUE_NAME = "queueTest";
    private static final String TEST_QUEUE_NAME_JNDI = "java:/jms/queue/" + TEST_QUEUE_NAME;

    @Before
    public void connect() throws IOException {
        client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
        ops = new Operations(client);
        administration = new Administration(client);
    }

    @After
    public void cleanup() throws Exception {
        try {
            ops.removeIfExists(MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME)
                    .and("jms-queue", TEST_QUEUE_NAME));
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void removeQueue() throws CommandFailedException, IOException, OperationException {
        client.apply(new AddQueue.Builder(TEST_QUEUE_NAME)
                .jndiEntries(Collections.singletonList(TEST_QUEUE_NAME_JNDI))
                .build());

        assertTrue("The queue should be created", ops.exists(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME).and("jms-queue", TEST_QUEUE_NAME)));

        client.apply(new RemoveQueue(TEST_QUEUE_NAME, "default"));

        assertFalse("The queue should be removed", ops.exists(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME).and("jms-queue", TEST_QUEUE_NAME)));
    }
}
