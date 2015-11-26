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
public class AddPeriodicHandlerOnlineTest {
    private static final String TEST_HANDLER_NAME = "creaper-handler_1";
    private static final Address TEST_HANDLER_ADDRESS =
            Address.subsystem("logging").and("periodic-rotating-file-handler", TEST_HANDLER_NAME);
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
        AddPeriodicHandler addPeriodicHandler = new AddPeriodicHandler.Builder(TEST_HANDLER_NAME, "server.log", ".yyyy")
                .level(Level.WARN)
                .filter("match(\"filter*\")")
                .setAutoFlush(true)
                .setEnabled(true)
                .patternFormatter("pattern")
                .setAppend(false)
                .fileRelativeTo("jboss.server.log.dir")
                .encoding(Charsets.ISO_8859_1)
                .build();

        client.apply(addPeriodicHandler);

        assertTrue("periodic handler wasn't created", ops.exists(TEST_HANDLER_ADDRESS));


        ModelNodeResult result = ops.readAttribute(TEST_HANDLER_ADDRESS, "level");
        result.assertSuccess();
        assertEquals("level has not been changed", Level.WARN.value(), result.stringValue());

        result = ops.readAttribute(TEST_HANDLER_ADDRESS, "filter-spec");
        result.assertSuccess();
        assertEquals("filter-spec has not been changed", "match(\"filter*\")", result.stringValue());

        result = ops.readAttribute(TEST_HANDLER_ADDRESS, "autoflush");
        result.assertSuccess();
        assertTrue("autoflush has not been changed", result.booleanValue());

        result = ops.readAttribute(TEST_HANDLER_ADDRESS, "enabled");
        result.assertSuccess();
        assertTrue("enabled has not been changed", result.booleanValue());

        result = ops.readAttribute(TEST_HANDLER_ADDRESS, "append");
        result.assertSuccess();
        assertFalse("append has not been changed", result.booleanValue());

        result = ops.readAttribute(TEST_HANDLER_ADDRESS, "formatter");
        result.assertSuccess();
        assertEquals("pattern-formatter has not been changed", "pattern", result.stringValue());

        result = ops.readAttribute(TEST_HANDLER_ADDRESS, "file");
        result.assertSuccess();
        assertEquals("log file has not been changed", "server.log", result.get("result").get("path").asString());
        assertEquals("relative-to attr has not been changed", "jboss.server.log.dir", result.get("result").get("relative-to").asString());

        result = ops.readAttribute(TEST_HANDLER_ADDRESS, "encoding");
        result.assertSuccess();
        assertEquals("encoding has not been changed", Charsets.ISO_8859_1.displayName(), result.stringValue());
    }

    @Test
    public void replaceHandler() throws Exception {
        AddPeriodicHandler addPeriodicHandler = new AddPeriodicHandler.Builder(TEST_HANDLER_NAME, "server.log", ".yyyy")
                .level(Level.FINEST)
                .filter("match(\"filter*\")")
                .setAutoFlush(false)
                .setEnabled(false)
                .patternFormatter("pattern")
                .setAppend(true)
                .fileRelativeTo("jboss.server.log.dir")
                .encoding(Charsets.UTF_8)
                .build();

        client.apply(addPeriodicHandler);

        assertTrue("periodic file handler wasn't created", ops.exists(TEST_HANDLER_ADDRESS));

        addPeriodicHandler = new AddPeriodicHandler.Builder(TEST_HANDLER_NAME, "server.log", ".yyyy")
                .level(Level.FINEST)
                .filter("match(\"new-filter*\")")
                .setAutoFlush(false)
                .setEnabled(false)
                .patternFormatter("pattern")
                .setAppend(true)
                .fileRelativeTo("jboss.server.log.dir")
                .encoding(Charsets.UTF_8)
                .setReplaceExisting(true)
                .build();

        client.apply(addPeriodicHandler);

        assertTrue("periodic fila handler wasn't created", ops.exists(TEST_HANDLER_ADDRESS));
        ops.readAttribute(TEST_HANDLER_ADDRESS, "filter-spec").assertSuccess();
        assertEquals("filter-spec is not changed", "match(\"new-filter*\")",
                ops.readAttribute(TEST_HANDLER_ADDRESS, "filter-spec").stringValue());
    }

    @Test(expected = CommandFailedException.class)
    public void replaceHandler2() throws Exception {
        AddPeriodicHandler addPeriodicHandler = new AddPeriodicHandler.Builder(TEST_HANDLER_NAME, "server.log", ".yyyy")
                .level(Level.FINEST)
                .filter("match(\"filter*\")")
                .setAutoFlush(false)
                .setEnabled(false)
                .patternFormatter("pattern")
                .setAppend(true)
                .fileRelativeTo("jboss.server.log.dir")
                .encoding(Charsets.UTF_8)
                .build();

        client.apply(addPeriodicHandler);

        assertTrue("periodic file handler wasn't created", ops.exists(TEST_HANDLER_ADDRESS));

        addPeriodicHandler = new AddPeriodicHandler.Builder(TEST_HANDLER_NAME, "server.log", ".yyyy")
                .level(Level.FINEST)
                .filter("match(\"new-filter*\")")
                .setAutoFlush(false)
                .setEnabled(false)
                .patternFormatter("pattern")
                .setAppend(true)
                .fileRelativeTo("jboss.server.log.dir")
                .encoding(Charsets.UTF_8)
                .setReplaceExisting(false)
                .build();

        client.apply(addPeriodicHandler);
    }
}
