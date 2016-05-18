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
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class AddAuditLoggerOnlineTest {

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
            ChangeAuditLogger changeLogger = new ChangeAuditLogger.Builder()
                    .enabled(false)
                    .logBoot(true)
                    .logReadOnly(false)
                    .build();
            client.apply(changeLogger);
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void addDefaultLogger() throws Exception {
        AddAuditLogger addLogger = new AddAuditLogger.Builder()
                .replaceExisting()
                .build();
        client.apply(addLogger);

        assertTrue("Logger should be created", ops.exists(TEST_LOGGER_ADDRESS));
    }

    @Test
    public void addLogger() throws Exception {
        AddAuditLogger addLogger = new AddAuditLogger.Builder()
                .enabled(true)
                .logBoot(true)
                .logReadOnly(true)
                .replaceExisting()
                .build();
        client.apply(addLogger);

        assertTrue("Logger should be created", ops.exists(TEST_LOGGER_ADDRESS));

        checkAttribute(TEST_LOGGER_ADDRESS, "enabled", "true");
        checkAttribute(TEST_LOGGER_ADDRESS, "log-boot", "true");
        checkAttribute(TEST_LOGGER_ADDRESS, "log-read-only", "true");
    }

    @Test
    public void overrideExistingLogger() throws Exception {
        AddAuditLogger addLogger = new AddAuditLogger.Builder()
                .enabled(true)
                .logBoot(true)
                .logReadOnly(true)
                .replaceExisting()
                .build();
        client.apply(addLogger);
        assertTrue("Logger should be created", ops.exists(TEST_LOGGER_ADDRESS));

        AddAuditLogger addLogger2 = new AddAuditLogger.Builder()
                .enabled(false)
                .logBoot(false)
                .logReadOnly(false)
                .replaceExisting()
                .build();

        client.apply(addLogger2);

        assertTrue("Logger should be created", ops.exists(TEST_LOGGER_ADDRESS));

        checkAttribute(TEST_LOGGER_ADDRESS, "enabled", "false");
        checkAttribute(TEST_LOGGER_ADDRESS, "log-boot", "false");
        checkAttribute(TEST_LOGGER_ADDRESS, "log-read-only", "false");
    }

    @Test(expected = CommandFailedException.class)
    public void overrideExistingLogger_notAllowed() throws Exception {
        AddAuditLogger addLogger = new AddAuditLogger.Builder()
                .replaceExisting()
                .build();
        client.apply(addLogger);
        assertTrue("Logger should be created", ops.exists(TEST_LOGGER_ADDRESS));

        AddAuditLogger addLogger2 = new AddAuditLogger.Builder()
                .enabled(false)
                .logBoot(true)
                .logReadOnly(false)
                .build();

        client.apply(addLogger2);
        fail("Logger already exists, an exception should be thrown");
    }

    private void checkAttribute(Address address, String attribute, String expectedValue) throws IOException {
        ModelNodeResult readAttribute = ops.readAttribute(address, attribute);
        readAttribute.assertSuccess("Read operation for " + attribute + " failed");
        assertEquals("Read operation for " + attribute + " return wrong value", expectedValue,
                readAttribute.stringValue());
    }
}
