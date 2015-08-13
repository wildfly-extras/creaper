package org.wildfly.extras.creaper.commands.messaging;

import org.jboss.arquillian.junit.Arquillian;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;
import org.wildfly.extras.creaper.test.AS7Tests;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(AS7Tests.class)
@RunWith(Arquillian.class)
public class RemoveTopicOnlineTest {
    private Operations ops;
    private OnlineManagementClient client;
    private Administration administration;

    private static final String TEST_TOPIC_NAME = "topicTest";
    private static final String TEST_TOPIC_NAME_JNDI = "java:/jms/topic/" + TEST_TOPIC_NAME;

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
                    .and("jms-topic", TEST_TOPIC_NAME));
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void removeTopic() throws CommandFailedException, IOException, OperationException {
        client.apply(new AddTopic.Builder(TEST_TOPIC_NAME, "default")
                .jndiEntries(Collections.singletonList(TEST_TOPIC_NAME_JNDI))
                .build());

        assertTrue("The topic should be created", ops.exists(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME).and("jms-topic", TEST_TOPIC_NAME)));

        client.apply(new RemoveTopic(TEST_TOPIC_NAME, "default"));

        assertFalse("The topic should be removed", ops.exists(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME).and("jms-topic", TEST_TOPIC_NAME)));
    }
}
