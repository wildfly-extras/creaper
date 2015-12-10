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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class RemoveHandlerOnlineTest {
    private static final String TEST_HANDLER_NAME = "creaper-handler";
    private static final Address TEST_CONSOLE_HANDLER_ADDRESS =
            Address.subsystem("logging").and("console-handler", TEST_HANDLER_NAME);
    private static final Address TEST_PERIODIC_HANDLER_ADDRESS =
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
            ops.removeIfExists(TEST_CONSOLE_HANDLER_ADDRESS);
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void removeConsole() throws Exception {
        LogHandlerCommand addConsoleHandler = LogHandlerCommand.console().add(TEST_HANDLER_NAME)
                .level(Level.WARN)
                .filter("match(\"new-filter*\")")
                .setAutoFlush(true)
                .setEnabled(true)
                .patternFormatter("new-pattern")
                .target(Target.STDOUT)
                .encoding(Charsets.ISO_8859_1)
                .build();

        client.apply(addConsoleHandler);

        assertTrue("console handler should be created", ops.exists(TEST_CONSOLE_HANDLER_ADDRESS));

        LogHandlerCommand removeHandler = LogHandlerCommand.console().remove(TEST_HANDLER_NAME);
        client.apply(removeHandler);
        assertFalse("console handler should be deleted", ops.exists(TEST_CONSOLE_HANDLER_ADDRESS));
    }

    @Test
    public void removePeriodic() throws Exception {
        LogHandlerCommand addPeriodicRotatingFileHandler =
                LogHandlerCommand.periodicRotatingFile().add(TEST_HANDLER_NAME, "server.log", ".yyyy")
                        .level(Level.FINEST)
                        .filter("match(\"filter*\")")
                        .setAutoFlush(false)
                        .setEnabled(false)
                        .patternFormatter("pattern")
                        .setAppend(true)
                        .fileRelativeTo("jboss.server.log.dir")
                        .encoding(Charsets.UTF_8)
                        .build();

        client.apply(addPeriodicRotatingFileHandler);

        assertTrue("console handler should be created", ops.exists(TEST_PERIODIC_HANDLER_ADDRESS));

        LogHandlerCommand removeHandler = LogHandlerCommand.periodicRotatingFile().remove(TEST_HANDLER_NAME);
        client.apply(removeHandler);
        assertFalse("periodic handler should be deleted", ops.exists(TEST_PERIODIC_HANDLER_ADDRESS));
    }
}
