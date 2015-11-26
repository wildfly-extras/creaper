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

@RunWith(Arquillian.class)
public class ChangeConsoleLogHandlerOnlineTest {
    private static final String TEST_HANDLER_NAME = "NEW";
    private static final Address TEST_HANDLER_ADDRESS = Address.subsystem("logging")
            .and("console-handler", TEST_HANDLER_NAME);

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
    public void changeAll() throws Exception {
        AddConsoleLogHandler addHandler = Logging.handler().console().add(TEST_HANDLER_NAME)
                .level(LogLevel.FINEST)
                .filter("match(\"filter*\")")
                .autoFlush(false)
                .enabled(false)
                .patternFormatter("pattern")
                .target(ConsoleTarget.STDOUT)
                .encoding(Charsets.UTF_8)
                .build();

        client.apply(addHandler);

        assertTrue("handler should be created", ops.exists(TEST_HANDLER_ADDRESS));

        ChangeConsoleLogHandler changeHandler = Logging.handler().console().change(TEST_HANDLER_NAME)
                .level(LogLevel.WARN)
                .filter("match(\"new-filter*\")")
                .autoFlush(true)
                .enabled(true)
                .patternFormatter("new-pattern")
                .target(ConsoleTarget.STDOUT)
                .encoding(Charsets.ISO_8859_1)
                .build();

        client.apply(changeHandler);

        assertTrue("handeler should be created", ops.exists(TEST_HANDLER_ADDRESS));

        ModelNodeResult result = ops.readAttribute(TEST_HANDLER_ADDRESS, "level");
        result.assertSuccess();
        assertEquals("level should be changed", LogLevel.WARN.value(), result.stringValue());

        result = ops.readAttribute(TEST_HANDLER_ADDRESS, "filter-spec");
        result.assertSuccess();
        assertEquals("filter-spec should be changed", "match(\"new-filter*\")", result.stringValue());

        result = ops.readAttribute(TEST_HANDLER_ADDRESS, "autoflush");
        result.assertSuccess();
        assertTrue("autoflush should be changed", result.booleanValue());

        result = ops.readAttribute(TEST_HANDLER_ADDRESS, "enabled");
        result.assertSuccess();
        assertTrue("enabled should be changed", result.booleanValue());

        result = ops.readAttribute(TEST_HANDLER_ADDRESS, "formatter");
        result.assertSuccess();
        assertEquals("pattern-formatter should be changed", "new-pattern", result.stringValue());

        result = ops.readAttribute(TEST_HANDLER_ADDRESS, "encoding");
        result.assertSuccess();
        assertEquals("encoding should be changed", Charsets.ISO_8859_1.displayName(), result.stringValue());

        result = ops.readAttribute(TEST_HANDLER_ADDRESS, "target");
        result.assertSuccess();
        assertEquals("target should be changed", ConsoleTarget.STDOUT.value(), result.stringValue());
    }

    @Test
    public void changeNothing() throws Exception {
        AddConsoleLogHandler addHandler = Logging.handler().console().add(TEST_HANDLER_NAME)
                .level(LogLevel.WARN)
                .filter("match(\"new-filter*\")")
                .autoFlush(true)
                .enabled(true)
                .patternFormatter("new-pattern")
                .target(ConsoleTarget.STDOUT)
                .encoding(Charsets.ISO_8859_1)
                .build();

        client.apply(addHandler);

        assertTrue("handler should be created", ops.exists(TEST_HANDLER_ADDRESS));

        ChangeConsoleLogHandler changeHandler = Logging.handler().console().change(TEST_HANDLER_NAME)
                .build();

        client.apply(changeHandler);

        assertTrue("handeler should be created", ops.exists(TEST_HANDLER_ADDRESS));

        ModelNodeResult result = ops.readAttribute(TEST_HANDLER_ADDRESS, "level");
        result.assertSuccess();
        assertEquals("level should be changed", LogLevel.WARN.value(), result.stringValue());

        result = ops.readAttribute(TEST_HANDLER_ADDRESS, "filter-spec");
        result.assertSuccess();
        assertEquals("filter-spec should not be changed", "match(\"new-filter*\")", result.stringValue());

        result = ops.readAttribute(TEST_HANDLER_ADDRESS, "autoflush");
        result.assertSuccess();
        assertTrue("autoflush should not be changed", result.booleanValue());

        result = ops.readAttribute(TEST_HANDLER_ADDRESS, "enabled");
        result.assertSuccess();
        assertTrue("enabled should not be changed", result.booleanValue());

        result = ops.readAttribute(TEST_HANDLER_ADDRESS, "formatter");
        result.assertSuccess();
        assertEquals("pattern-formatter should not be changed", "new-pattern", result.stringValue());

        result = ops.readAttribute(TEST_HANDLER_ADDRESS, "encoding");
        result.assertSuccess();
        assertEquals("encoding should not be changed", Charsets.ISO_8859_1.displayName(), result.stringValue());

        result = ops.readAttribute(TEST_HANDLER_ADDRESS, "target");
        result.assertSuccess();
        assertEquals("target should not be changed", ConsoleTarget.STDOUT.value(), result.stringValue());
    }
}
