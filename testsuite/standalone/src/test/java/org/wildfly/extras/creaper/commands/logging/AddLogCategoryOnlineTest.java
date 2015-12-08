package org.wildfly.extras.creaper.commands.logging;

import com.google.common.base.Charsets;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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

import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class AddLogCategoryOnlineTest {

    private static final String TEST_LOGGER_NAME = "creaper-logger";
    private static final String TEST_HANDLER_NAME_1 = "creaper-handler1";
    private static final String TEST_HANDLER_NAME_2 = "creaper-handler2";
    private static final Address TEST_LOGGER_ADDRESS =
            Address.subsystem("logging").and("logger", TEST_LOGGER_NAME);
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
            ops.removeIfExists(TEST_LOGGER_ADDRESS);
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void addLogger() throws Exception {

        AddConsoleHandler addConsoleHandler = new AddConsoleHandler.Builder(TEST_HANDLER_NAME_1)
                .setAutoFlush(true)
                .setEnabled(true)
                .level(Level.OFF)
                .filter("match(\"filter\")")
                .encoding(Charsets.UTF_8)
                .target(Target.CONSOLE)
                .patternFormatter("aaa")
                .build();

        client.apply(addConsoleHandler);

        addConsoleHandler = new AddConsoleHandler.Builder(TEST_HANDLER_NAME_2)
                .setAutoFlush(true)
                .setEnabled(true)
                .level(Level.OFF)
                .filter("match(\"filter\")")
                .encoding(Charsets.UTF_8)
                .target(Target.CONSOLE)
                .patternFormatter("bbb")
                .build();

        client.apply(addConsoleHandler);

        AddLogCategory addLogCategory = new AddLogCategory.Builder(TEST_LOGGER_NAME)
                .level(Level.OFF)
                .filter("match(\"filter\")")
                .handlers(TEST_HANDLER_NAME_1, TEST_HANDLER_NAME_2)
                .setUseParentHandler(true)
                .build();

        client.apply(addLogCategory);

        assertTrue("logger should be created", ops.exists(TEST_LOGGER_ADDRESS));
    }
}
