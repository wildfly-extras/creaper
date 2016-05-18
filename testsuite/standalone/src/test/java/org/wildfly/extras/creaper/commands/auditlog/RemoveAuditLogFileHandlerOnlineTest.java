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
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class RemoveAuditLogFileHandlerOnlineTest {

    private static final String TEST_HANDLER_NAME = "creaper-file-handler";
    private static final Address TEST_HANDLER_ADDRESS = Address.coreService("management")
            .and("access", "audit")
            .and("file-handler", TEST_HANDLER_NAME);

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
    public void removeFileHandler() throws Exception {
        AddAuditLogFileHandler addFileHandler = new AddAuditLogFileHandler.Builder(TEST_HANDLER_NAME)
                .formatter("json-formatter")
                .maxFailureCount(10)
                .path("/path/to/nowhere")
                .relativeTo("jboss.dir")
                .build();
        client.apply(addFileHandler);

        assertTrue("The file handler should be created", ops.exists(TEST_HANDLER_ADDRESS));

        RemoveAuditLogFileHandler removeFileHandler = new RemoveAuditLogFileHandler(TEST_HANDLER_NAME);
        client.apply(removeFileHandler);
        assertFalse("The file handler should be removed", ops.exists(TEST_HANDLER_ADDRESS));
    }

    @Test(expected = CommandFailedException.class)
    public void removeNonExistingFileHandler() throws Exception {
        RemoveAuditLogFileHandler removeFileHandler = new RemoveAuditLogFileHandler("non-existing-file-handler");
        client.apply(removeFileHandler);

        fail("Specified file handler does not exist in configuration, an exception should be thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeFileHandler_nullName() throws Exception {
        new RemoveAuditLogFileHandler(null);
        fail("Creating command with null handler name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeFileHandler_emptyName() throws Exception {
        new RemoveAuditLogFileHandler("");
        fail("Creating command with empty handler name should throw exception");
    }
}
