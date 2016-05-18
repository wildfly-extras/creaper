package org.wildfly.extras.creaper.commands.auditlog;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.online.CliException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class RemoveAuditLoggerOnlineTest {

    private static final Address TEST_LOGGER_ADDRESS = Address.coreService("management")
            .and("access", "audit")
            .and("logger", "audit-log");

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
    public void cleanup() throws IOException, CliException, OperationException, TimeoutException,
            InterruptedException, CommandFailedException {
        try {
            // return server to its default configuration
            AddAuditLogger addLogger = new AddAuditLogger.Builder()
                    .enabled(false)
                    .logBoot(true)
                    .logReadOnly(false)
                    .replaceExisting()
                    .build();
            client.apply(addLogger);
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void removeLogger() throws Exception {
        assertTrue("Logger should be created by default", ops.exists(TEST_LOGGER_ADDRESS));

        RemoveAuditLogger removeLogger = new RemoveAuditLogger();
        client.apply(removeLogger);
        assertFalse("Logger should be removed", ops.exists(TEST_LOGGER_ADDRESS));
    }

}
