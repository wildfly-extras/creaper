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
public class AddAuditLogFileHandlerOnlineTest {

    private static final String TEST_FILE_HANDLER_NAME = "creaper-file-handler";
    private static final Address TEST_FILE_HANDLER_ADDRESS = Address.coreService("management")
            .and("access", "audit")
            .and("file-handler", TEST_FILE_HANDLER_NAME);

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
            ops.removeIfExists(TEST_FILE_HANDLER_ADDRESS);
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void addFileHandler() throws Exception {
        AddAuditLogFileHandler addFileHandler = new AddAuditLogFileHandler.Builder(TEST_FILE_HANDLER_NAME)
                .formatter("json-formatter")
                .maxFailureCount(10)
                .path("audit-log.log")
                .relativeTo("jboss.dir")
                .build();

        client.apply(addFileHandler);

        assertTrue("The file handler should be created", ops.exists(TEST_FILE_HANDLER_ADDRESS));

        checkAttribute(TEST_FILE_HANDLER_ADDRESS, "formatter", "json-formatter");
        checkAttribute(TEST_FILE_HANDLER_ADDRESS, "max-failure-count", "10");
        checkAttribute(TEST_FILE_HANDLER_ADDRESS, "path", "audit-log.log");
        checkAttribute(TEST_FILE_HANDLER_ADDRESS, "relative-to", "jboss.dir");
    }

    @Test
    public void overrideExistingFileHandler() throws Exception {
        AddAuditLogFileHandler addFileHandler = new AddAuditLogFileHandler.Builder(TEST_FILE_HANDLER_NAME)
                .formatter("json-formatter")
                .path("/tmp/audit-log.log")
                .build();
        AddAuditLogFileHandler addFileHandler2 = new AddAuditLogFileHandler.Builder(TEST_FILE_HANDLER_NAME)
                .formatter("json-formatter")
                .path("different-audit-log.log")
                .maxFailureCount(20)
                .relativeTo("jboss.dir")
                .replaceExisting()
                .build();

        client.apply(addFileHandler);
        assertTrue("The file handler should be created", ops.exists(TEST_FILE_HANDLER_ADDRESS));
        client.apply(addFileHandler2);
        assertTrue("The file handler should be created", ops.exists(TEST_FILE_HANDLER_ADDRESS));

        checkAttribute(TEST_FILE_HANDLER_ADDRESS, "formatter", "json-formatter");
        checkAttribute(TEST_FILE_HANDLER_ADDRESS, "max-failure-count", "20");
        checkAttribute(TEST_FILE_HANDLER_ADDRESS, "path", "different-audit-log.log");
        checkAttribute(TEST_FILE_HANDLER_ADDRESS, "relative-to", "jboss.dir");
    }

    @Test(expected = CommandFailedException.class)
    public void overrideExistingFileHandler_notAllowed() throws Exception {
        AddAuditLogFileHandler addFileHandler = new AddAuditLogFileHandler.Builder(TEST_FILE_HANDLER_NAME)
                .formatter("json-formatter")
                .path("/tmp/audit-log.log")
                .build();
        AddAuditLogFileHandler addFileHandler2 = new AddAuditLogFileHandler.Builder(TEST_FILE_HANDLER_NAME)
                .formatter("json-formatter")
                .path("different-audit-log.log")
                .maxFailureCount(20)
                .relativeTo("jboss.dir")
                .build();

        client.apply(addFileHandler);
        assertTrue("The file handler should be created", ops.exists(TEST_FILE_HANDLER_ADDRESS));
        client.apply(addFileHandler2);

        fail("File handler creaper-file-handler already exists, an exception should be thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addFileHandler_nullPath() throws Exception {
        new AddAuditLogFileHandler.Builder(TEST_FILE_HANDLER_NAME)
                .formatter("json-formatter")
                .path(null)
                .build();
        fail("Creating command with null path should throw an exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addFileHandler_emptyPath() throws Exception {
        new AddAuditLogFileHandler.Builder(TEST_FILE_HANDLER_NAME)
                .formatter("json-formatter")
                .path("")
                .build();
        fail("Creating command with empty path should throw an exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addFileHandler_nullFormatter() throws Exception {
        new AddAuditLogFileHandler.Builder(TEST_FILE_HANDLER_NAME)
                .formatter(null)
                .path("/tmp/audit-log.log")
                .build();
        fail("Creating command with null formatter should throw an exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addFileHandler_emptyFormatter() throws Exception {
        new AddAuditLogFileHandler.Builder(TEST_FILE_HANDLER_NAME)
                .formatter("")
                .path("/tmp/audit-log.log")
                .build();
        fail("Creating command with empty formatter should throw an exception");
    }

    private void checkAttribute(Address address, String attribute, String expectedValue) throws IOException {
        ModelNodeResult readAttribute = ops.readAttribute(address, attribute);
        readAttribute.assertSuccess("Read operation for " + attribute + " failed");
        assertEquals("Read operation for " + attribute + " return wrong value", expectedValue,
                readAttribute.stringValue());
    }
}
