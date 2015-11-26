package org.wildfly.extras.creaper.commands.logging;

import com.google.common.base.Charsets;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.online.CliException;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
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

/**
 * @author Ivan Straka istraka@redhat.com
 */
@RunWith(Arquillian.class)
public class ChangeLogCategoryOnlineTest {

    private static final String TEST_LOGGER_NAME = "creaper-logger";
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
    public void changeLogger() throws Exception {
        AddLogCategory addLogger = new AddLogCategory.Builder(TEST_LOGGER_NAME)
                .build();
        client.apply(addLogger);
        assertTrue("console handler wasn't created", ops.exists(TEST_LOGGER_ADDRESS));

        ChangeLogCategory changeLogger = new ChangeLogCategory.Builder(TEST_LOGGER_NAME)
                .filter("match(\"filter\")")
                .handlers("CONSOLE", "FILE")
                .level(Level.DEBUG)
                .setUseParentHandler(true)
                .build();
        client.apply(changeLogger);


        assertTrue("console handler wasn't created", ops.exists(TEST_LOGGER_ADDRESS));

        ModelNodeResult result = ops.readAttribute(TEST_LOGGER_ADDRESS, "level");
        result.assertSuccess();
        assertEquals("level has not been changed", Level.DEBUG.value(), result.stringValue());

        result = ops.readAttribute(TEST_LOGGER_ADDRESS, "filter-spec");
        result.assertSuccess();
        assertEquals("filter-spec has not been changed", "match(\"filter\")", result.stringValue());

        result = ops.readAttribute(TEST_LOGGER_ADDRESS, "use-parent-handlers");
        result.assertSuccess();
        assertTrue("using parent handlers has not been changed", result.booleanValue());

        result = ops.readAttribute(TEST_LOGGER_ADDRESS, "handlers");
        result.assertSuccess();
        assertEquals("enabled has not been changed", 2, result.stringListValue().size());
    }
}
