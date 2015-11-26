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
public class AddConsoleHandlerOnlineTest {
    private static final String TEST_HANDLER_NAME = "creaper-handler";
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
    public void addHandler() throws Exception {
        AddConsoleHandler addConsoleHandler = new AddConsoleHandler.Builder(TEST_HANDLER_NAME)
                .level(Level.WARN)
                .filter("match(\"new-filter*\")")
                .setAutoFlush(true)
                .setEnabled(true)
                .patternFormatter("new-pattern")
                .target(Target.STDOUT)
                .encoding(Charsets.ISO_8859_1)
                .build();

        client.apply(addConsoleHandler);

        assertTrue("console handler wasn't created", ops.exists(TEST_HANDLER_ADDRESS));

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

    @Test
    public void replaceExisting() throws Exception {
        AddConsoleHandler addConsoleHandler = new AddConsoleHandler.Builder(TEST_HANDLER_NAME)
                .setAutoFlush(true)
                .setEnabled(true)
                .level(Level.OFF)
                .filter("match(\"filter\")")
                .encoding(Charsets.UTF_8)
                .target(Target.CONSOLE)
                .patternFormatter("aaa")
                .build();

        client.apply(addConsoleHandler);

        assertTrue("console handler wasn't created", ops.exists(TEST_HANDLER_ADDRESS));

        addConsoleHandler = new AddConsoleHandler.Builder(TEST_HANDLER_NAME)
                .setAutoFlush(false)
                .setEnabled(false)
                .level(Level.ALL)
                .filter("match(\"new-filter\")")
                .encoding(Charsets.UTF_8)
                .target(Target.CONSOLE)
                .patternFormatter("bbb")
                .setReplaceExisting(true)
                .build();

        client.apply(addConsoleHandler);

        assertTrue("console handler wasn't created", ops.exists(TEST_HANDLER_ADDRESS));

        ModelNodeResult result = ops.readAttribute(TEST_HANDLER_ADDRESS, "level");
        result.assertSuccess();
        assertEquals("level has not been changed", Level.ALL.value(), result.stringValue());

        result = ops.readAttribute(TEST_HANDLER_ADDRESS, "filter-spec");
        result.assertSuccess();
        assertEquals("filter-spec has not been changed", "match(\"new-filter\")", result.stringValue());

        result = ops.readAttribute(TEST_HANDLER_ADDRESS, "autoflush");
        result.assertSuccess();
        assertFalse("autoflush has not been changed", result.booleanValue());

        result = ops.readAttribute(TEST_HANDLER_ADDRESS, "enabled");
        result.assertSuccess();
        assertFalse("enabled has not been changed", result.booleanValue());

        result = ops.readAttribute(TEST_HANDLER_ADDRESS, "formatter");
        result.assertSuccess();
        assertEquals("pattern-formatter has not been changed", "bbb", result.stringValue());

        result = ops.readAttribute(TEST_HANDLER_ADDRESS, "encoding");
        result.assertSuccess();
        assertEquals("pattern-formatter has been changed", Charsets.UTF_8.displayName(), result.stringValue());

        result = ops.readAttribute(TEST_HANDLER_ADDRESS, "target");
        result.assertSuccess();
        assertEquals("pattern-formatter has been changed", Target.CONSOLE.value(), result.stringValue());
    }

    @Test(expected = CommandFailedException.class)
    public void replaceExisting2() throws Exception {
        AddConsoleHandler addConsoleHandler = new AddConsoleHandler.Builder(TEST_HANDLER_NAME)
                .setAutoFlush(true)
                .setEnabled(true)
                .level(Level.OFF)
                .filter("match(\"filter\")")
                .encoding(Charsets.UTF_8)
                .target(Target.CONSOLE)
                .patternFormatter("aaa")
                .setReplaceExisting(true)
                .build();

        client.apply(addConsoleHandler);

        addConsoleHandler = new AddConsoleHandler.Builder(TEST_HANDLER_NAME)
                .setAutoFlush(false)
                .setEnabled(false)
                .level(Level.ALL)
                .filter("match(\"new-filter\")")
                .encoding(Charsets.UTF_8)
                .target(Target.CONSOLE)
                .patternFormatter("bbb")
                .setReplaceExisting(false)
                .build();

        client.apply(addConsoleHandler);
    }

}
