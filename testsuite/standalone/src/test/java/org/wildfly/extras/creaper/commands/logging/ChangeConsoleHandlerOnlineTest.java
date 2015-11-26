package org.wildfly.extras.creaper.commands.logging;

import com.google.common.base.Charsets;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import static org.junit.Assert.assertTrue;

/**
 * @author Ivan Straka istraka@redhat.com
 */

@RunWith(Arquillian.class)
public class ChangeConsoleHandlerOnlineTest {
    private static final String TEST_HANDLER_NAME = "NEW";
    private static final Address TEST_HANDLER_ADDRESS =
            Address.subsystem("logging").and("console-handler", TEST_HANDLER_NAME);
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
            ops.removeIfExists(TEST_HANDLER_ADDRESS);
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void changeHandler() throws Exception {
        AddConsoleHandler addHandler = new AddConsoleHandler.Builder(TEST_HANDLER_NAME)
                .level(Level.FINEST)
                .filter("match(\"filter*\")")
                .setAutoFlush(false)
                .setEnabled(false)
                .patternFormatter("pattern")
                .target(Target.CONSOLE)
                .encoding(Charsets.UTF_8)
                .build();

        client.apply(addHandler);
        assertTrue("handler wasn't created", ops.exists(TEST_HANDLER_ADDRESS));


        ChangeConsoleHandler changeHandler = new ChangeConsoleHandler.Builder(TEST_HANDLER_NAME)
                .level(Level.WARN)
                .filter("match(\"new-filter*\")")
                .setAutoFlush(true)
                .setEnabled(true)
                .patternFormatter("new-pattern")
                .target(Target.STDOUT)
                .encoding(Charsets.ISO_8859_1)
                .build();

        client.apply(changeHandler);

        assertTrue("handeler wasn't created", ops.exists(TEST_HANDLER_ADDRESS));


        ModelNodeResult result = ops.readAttribute(TEST_HANDLER_ADDRESS, "level");
        result.assertSuccess();
        assertEquals("level has not been changed", Level.WARN.value(), result.stringValue());

        result = ops.readAttribute(TEST_HANDLER_ADDRESS, "filter-spec");
        result.assertSuccess();
        assertEquals("filter-spec has not been changed", "match(\"new-filter*\")", result.stringValue());

        result = ops.readAttribute(TEST_HANDLER_ADDRESS, "autoflush");
        result.assertSuccess();
        assertTrue("autoflush has not been changed", result.booleanValue());

        result = ops.readAttribute(TEST_HANDLER_ADDRESS, "enabled");
        result.assertSuccess();
        assertTrue("enabled has not been changed", result.booleanValue());

        result = ops.readAttribute(TEST_HANDLER_ADDRESS, "formatter");
        result.assertSuccess();
        assertEquals("pattern-formatter has not been changed", "new-pattern", result.stringValue());

        result = ops.readAttribute(TEST_HANDLER_ADDRESS, "encoding");
        result.assertSuccess();
        assertEquals("pattern-formatter has not been changed", Charsets.ISO_8859_1.displayName(), result.stringValue());

        result = ops.readAttribute(TEST_HANDLER_ADDRESS, "target");
        result.assertSuccess();
        assertEquals("pattern-formatter has not been changed", Target.STDOUT.value(), result.stringValue());
    }
}
