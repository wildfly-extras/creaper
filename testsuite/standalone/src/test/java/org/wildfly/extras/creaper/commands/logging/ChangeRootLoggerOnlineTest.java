package org.wildfly.extras.creaper.commands.logging;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class ChangeRootLoggerOnlineTest {
    private static final String ROOT_NAME = "ROOT";
    private static final Address TEST_ROOT_LOGGER_ADDRESS =
            Address.subsystem("logging").and("root-logger", ROOT_NAME);
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
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void changeEverything() throws Exception {
        ChangeRootLogger changeRootLogger = new ChangeRootLogger.Builder()
                .changeFilter("match(\"new-filter\")")
                .changeHandlers(new String[]{})
                .changeLevel(Level.OFF)
                .build();

        client.apply(changeRootLogger);

        assertTrue("root logger does not exists", ops.exists(TEST_ROOT_LOGGER_ADDRESS));

        ops.readAttribute(TEST_ROOT_LOGGER_ADDRESS, "handlers").assertSuccess();
        assertFalse("Handlers for root logger should not be defined.",
                ops.readAttribute(TEST_ROOT_LOGGER_ADDRESS, "handlers").hasDefinedValue());

        ops.readAttribute(TEST_ROOT_LOGGER_ADDRESS, "level").assertSuccess();
        assertEquals("level should be changed", Level.OFF.value(),
                ops.readAttribute(TEST_ROOT_LOGGER_ADDRESS, "level").stringValue());

        ops.readAttribute(TEST_ROOT_LOGGER_ADDRESS, "filter-spec").assertSuccess();
        assertEquals("filter-spec should be changed", "match(\"new-filter\")",
                ops.readAttribute(TEST_ROOT_LOGGER_ADDRESS, "filter-spec").stringValue());


        changeRootLogger = new ChangeRootLogger.Builder()
                .changeHandlers("CONSOLE")
                .build();
        client.apply(changeRootLogger);


        ops.readAttribute(TEST_ROOT_LOGGER_ADDRESS, "handlers").assertSuccess();
        assertEquals("size of handlers list",
                1, ops.readAttribute(TEST_ROOT_LOGGER_ADDRESS, "handlers").stringListValue().size());
    }

}
