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
public class AddTopicOnlineTest {
    private static final String TEST_TOPIC_NAME = "queueTest";
    private static final List<String> JNDI_ENTREIS = Collections.singletonList("jms/topic/" + TEST_TOPIC_NAME);

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
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME).and("jms-topic", TEST_TOPIC_NAME));
        client.close();
    }

    @Test
    public void addSimpleTopic_commandSucceeds() throws CommandFailedException, IOException {
        client.apply(new AddTopic.Builder(TEST_TOPIC_NAME)
                .jndiEntries(JNDI_ENTREIS)
                .build());

        ModelNodeResult result = ops.readAttribute(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME).and("jms-topic", TEST_TOPIC_NAME),
                "entries");
        result.assertSuccess();
    }

    @Test
    public void addDuplicatedTopic_commandSucceeds() throws CommandFailedException, IOException {
        client.apply(new AddTopic.Builder(TEST_TOPIC_NAME)
                .jndiEntries(JNDI_ENTREIS)
                .build());

        ModelNodeResult result = ops.readAttribute(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME).and("jms-topic", TEST_TOPIC_NAME),
                "entries");
        result.assertSuccess();

        client.apply(new AddTopic.Builder(TEST_TOPIC_NAME)
                .jndiEntries(JNDI_ENTREIS)
                .replaceExisting()
                .build());

        result = ops.readAttribute(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME).and("jms-topic", TEST_TOPIC_NAME),
                "entries");
        result.assertSuccess();
    }
}
