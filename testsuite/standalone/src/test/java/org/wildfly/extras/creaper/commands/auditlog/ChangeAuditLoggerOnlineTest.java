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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class ChangeAuditLoggerOnlineTest {

    private static final String TEST_FILE_HANDLER_NAME = "creaper-file-handler";
    private static final String TEST_SYSLOG_HANDLER_NAME = "creaper-syslog-handler";
    private static final Address TEST_LOGGER_ADDRESS = Address.coreService("management")
            .and("access", "audit")
            .and("logger", "audit-log");
    private static final Address TEST_FILE_HANDLER_ADDRESS = Address.coreService("management")
            .and("access", "audit")
            .and("file-handler", TEST_FILE_HANDLER_NAME);
    private static final Address TEST_SYSLOG_HANDLER_ADDRESS = Address.coreService("management")
            .and("access", "audit")
            .and("syslog-handler", TEST_SYSLOG_HANDLER_NAME);
    private static final Address TEST_DESTINATION_FILE_HANDLER_ADDRESS = Address.coreService("management")
            .and("access", "audit")
            .and("logger", "audit-log")
            .and("handler", TEST_FILE_HANDLER_NAME);
    private static final Address TEST_DESTINATION_SYSLOG_HANDLER_ADDRESS = Address.coreService("management")
            .and("access", "audit")
            .and("logger", "audit-log")
            .and("handler", TEST_SYSLOG_HANDLER_NAME);

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
            ops.removeIfExists(TEST_DESTINATION_FILE_HANDLER_ADDRESS);
            ops.removeIfExists(TEST_DESTINATION_SYSLOG_HANDLER_ADDRESS);

            // return server to its default configuration
            ChangeAuditLogger changeLogger = new ChangeAuditLogger.Builder()
                    .enabled(false)
                    .logBoot(true)
                    .logReadOnly(false)
                    .build();
            client.apply(changeLogger);
            administration.reloadIfRequired();

            ops.removeIfExists(TEST_FILE_HANDLER_ADDRESS);
            ops.removeIfExists(TEST_SYSLOG_HANDLER_ADDRESS);
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void changeNothing() throws Exception {
        ChangeAuditLogger changeLogger = new ChangeAuditLogger.Builder()
                .enabled(false)
                .logBoot(true)
                .logReadOnly(false)
                .build();
        client.apply(changeLogger);
        assertTrue("Logger should not be removed", ops.exists(TEST_LOGGER_ADDRESS));

        checkAttribute(TEST_LOGGER_ADDRESS, "enabled", "false");
        checkAttribute(TEST_LOGGER_ADDRESS, "log-boot", "true");
        checkAttribute(TEST_LOGGER_ADDRESS, "log-read-only", "false");
    }

    @Test
    public void changeEverything() throws Exception {
        ChangeAuditLogger changeLogger = new ChangeAuditLogger.Builder()
                .enabled(true)
                .logBoot(false)
                .logReadOnly(true)
                .build();
        client.apply(changeLogger);
        assertTrue("Logger should not be removed", ops.exists(TEST_LOGGER_ADDRESS));

        checkAttribute(TEST_LOGGER_ADDRESS, "enabled", "true");
        checkAttribute(TEST_LOGGER_ADDRESS, "log-boot", "false");
        checkAttribute(TEST_LOGGER_ADDRESS, "log-read-only", "true");
    }

    @Test
    public void addFileHandler() throws Exception {
        createFileHandler();

        ChangeAuditLogger change = new ChangeAuditLogger.Builder()
                .enabled(true)
                .logBoot(true)
                .logReadOnly(true)
                .addHandler(TEST_FILE_HANDLER_NAME)
                .build();
        client.apply(change);
        assertTrue("Logger shouldn't be removed", ops.exists(TEST_LOGGER_ADDRESS));

        assertTrue("File handler should be created", ops.exists(TEST_DESTINATION_FILE_HANDLER_ADDRESS));
    }

    @Test
    public void addSyslogHandler() throws Exception {
        createSyslogHandler();

        ChangeAuditLogger change = new ChangeAuditLogger.Builder()
                .enabled(true)
                .logBoot(true)
                .logReadOnly(true)
                .addHandler(TEST_SYSLOG_HANDLER_NAME)
                .build();
        client.apply(change);
        assertTrue("Logger shouldn't be removed", ops.exists(TEST_LOGGER_ADDRESS));

        assertTrue("Syslog handler should be created", ops.exists(TEST_DESTINATION_SYSLOG_HANDLER_ADDRESS));
    }

    @Test
    public void addTwoHandlers() throws Exception {
        createSyslogHandler();
        createFileHandler();

        ChangeAuditLogger change = new ChangeAuditLogger.Builder()
                .enabled(true)
                .logBoot(true)
                .logReadOnly(true)
                .addHandlers(TEST_SYSLOG_HANDLER_NAME, TEST_FILE_HANDLER_NAME)
                .build();
        client.apply(change);
        assertTrue("Logger shouldn't be removed", ops.exists(TEST_LOGGER_ADDRESS));

        assertTrue("File handler should be created", ops.exists(TEST_DESTINATION_FILE_HANDLER_ADDRESS));
        assertTrue("Syslog handler should be created", ops.exists(TEST_DESTINATION_SYSLOG_HANDLER_ADDRESS));
    }

    @Test
    public void removeHandler() throws Exception {
        createSyslogHandler();
        createFileHandler();

        ChangeAuditLogger change = new ChangeAuditLogger.Builder()
                .addHandlers(TEST_SYSLOG_HANDLER_NAME, TEST_FILE_HANDLER_NAME)
                .build();
        client.apply(change);
        assertTrue("Logger shouldn't be removed", ops.exists(TEST_LOGGER_ADDRESS));

        assertTrue("File handler should be created", ops.exists(TEST_DESTINATION_FILE_HANDLER_ADDRESS));
        assertTrue("Syslog handler should be created", ops.exists(TEST_DESTINATION_SYSLOG_HANDLER_ADDRESS));

        change = new ChangeAuditLogger.Builder()
                .removeHandler(TEST_FILE_HANDLER_NAME)
                .build();
        client.apply(change);
        assertTrue("Logger shouldn't be removed", ops.exists(TEST_LOGGER_ADDRESS));

        assertFalse("File handler should be removed", ops.exists(TEST_DESTINATION_FILE_HANDLER_ADDRESS));
        assertTrue("Syslog handler shouldn't be removed", ops.exists(TEST_DESTINATION_SYSLOG_HANDLER_ADDRESS));
    }

    @Test
    public void removeTwoHandlers() throws Exception {
        createSyslogHandler();
        createFileHandler();

        ChangeAuditLogger change = new ChangeAuditLogger.Builder()
                .addHandlers(TEST_SYSLOG_HANDLER_NAME, TEST_FILE_HANDLER_NAME)
                .build();
        client.apply(change);
        assertTrue("Logger shouldn't be removed", ops.exists(TEST_LOGGER_ADDRESS));

        assertTrue("File handler should be created", ops.exists(TEST_DESTINATION_FILE_HANDLER_ADDRESS));
        assertTrue("Syslog handler should be created", ops.exists(TEST_DESTINATION_SYSLOG_HANDLER_ADDRESS));

        change = new ChangeAuditLogger.Builder()
                .removeHandlers(TEST_SYSLOG_HANDLER_NAME, TEST_FILE_HANDLER_NAME)
                .build();
        client.apply(change);
        assertTrue("Logger shouldn't be removed", ops.exists(TEST_LOGGER_ADDRESS));

        assertFalse("File handler should be removed", ops.exists(TEST_DESTINATION_FILE_HANDLER_ADDRESS));
        assertFalse("Syslog handler should be removed", ops.exists(TEST_DESTINATION_SYSLOG_HANDLER_ADDRESS));
    }

    @Test(expected = CommandFailedException.class)
    public void addNonExistingHandler() throws Exception {
        ChangeAuditLogger change = new ChangeAuditLogger.Builder()
                .addHandler("non-existing-destination-handler")
                .build();
        client.apply(change);
    }

    @Test(expected = CommandFailedException.class)
    public void removeNonExistingHandler() throws Exception {
        ChangeAuditLogger change = new ChangeAuditLogger.Builder()
                .removeHandler("non-existing-destination-handler")
                .build();
        client.apply(change);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAndRemoveSameHandler() throws Exception {
        ChangeAuditLogger change = new ChangeAuditLogger.Builder()
                .removeHandler("handler")
                .addHandler("handler")
                .build();
        client.apply(change);
    }

    private void checkAttribute(Address address, String attribute, String expectedValue) throws IOException {
        ModelNodeResult readAttribute = ops.readAttribute(address, attribute);
        readAttribute.assertSuccess("Read operation for " + attribute + " failed");
        assertEquals("Read operation for " + attribute + " return wrong value", expectedValue,
                readAttribute.stringValue());
    }

    private void createFileHandler() throws CommandFailedException, IOException, OperationException {
        AddAuditLogFileHandler addFileHandler = new AddAuditLogFileHandler.Builder(TEST_FILE_HANDLER_NAME)
                .formatter("json-formatter")
                .path("/tmp/audit-log.log")
                .build();
        client.apply(addFileHandler);
        assertTrue("The file handler should be created", ops.exists(TEST_FILE_HANDLER_ADDRESS));
    }

    private void createSyslogHandler() throws OperationException, CommandFailedException, IOException {
        AddAuditLogSyslogHandler addUdpSyslogHandler = new AddAuditLogSyslogHandler.UdpBuilder(TEST_SYSLOG_HANDLER_NAME)
                .formatter("json-formatter")
                .build();
        client.apply(addUdpSyslogHandler);
        assertTrue("The syslog handler should be created", ops.exists(TEST_SYSLOG_HANDLER_ADDRESS));
    }
}
