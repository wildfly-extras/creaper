package org.wildfly.extras.creaper.commands.elytron.audit;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class AddFileAuditLogOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_FILE_AUDIT_LOG_NAME = "CreaperTestFileAuditLog";
    private static final Address TEST_FILE_AUDIT_LOG_ADDRESS = SUBSYSTEM_ADDRESS
            .and("file-audit-log", TEST_FILE_AUDIT_LOG_NAME);
    private static final String TEST_FILE_AUDIT_LOG_NAME2 = "CreaperTestFileAuditLog2";
    private static final Address TEST_FILE_AUDIT_LOG_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("file-audit-log", TEST_FILE_AUDIT_LOG_NAME2);

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_FILE_AUDIT_LOG_ADDRESS);
        ops.removeIfExists(TEST_FILE_AUDIT_LOG_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleFileAuditLog() throws Exception {
        AddFileAuditLog addFileAuditLog = new AddFileAuditLog.Builder(TEST_FILE_AUDIT_LOG_NAME)
                .path("audit.log")
                .build();

        client.apply(addFileAuditLog);

        assertTrue("File audit log should be created", ops.exists(TEST_FILE_AUDIT_LOG_ADDRESS));
    }

    @Test
    public void addTwoFileAuditLogs() throws Exception {
        AddFileAuditLog addFileAuditLog = new AddFileAuditLog.Builder(TEST_FILE_AUDIT_LOG_NAME)
                .path("audit.log")
                .build();
        AddFileAuditLog addFileAuditLog2 = new AddFileAuditLog.Builder(TEST_FILE_AUDIT_LOG_NAME2)
                .path("audit.log")
                .build();

        client.apply(addFileAuditLog);
        client.apply(addFileAuditLog2);

        assertTrue("File audit log should be created", ops.exists(TEST_FILE_AUDIT_LOG_ADDRESS));
        assertTrue("Second file audit log should be created", ops.exists(TEST_FILE_AUDIT_LOG_ADDRESS2));
    }

    @Test
    public void addFullFileAuditLog() throws Exception {
        AddFileAuditLog addFileAuditLog = new AddFileAuditLog.Builder(TEST_FILE_AUDIT_LOG_NAME)
                .path("audit.log")
                .format(AuditFormat.SIMPLE)
                .paramSynchronized(false)
                .relativeTo("jboss.server.log.dir")
                .build();

        client.apply(addFileAuditLog);

        assertTrue("File audit log should be created", ops.exists(TEST_FILE_AUDIT_LOG_ADDRESS));
        checkAttribute(TEST_FILE_AUDIT_LOG_ADDRESS, "path", "audit.log");
        checkAttribute(TEST_FILE_AUDIT_LOG_ADDRESS, "format", "SIMPLE");
        checkAttribute(TEST_FILE_AUDIT_LOG_ADDRESS, "synchronized", "false");
        checkAttribute(TEST_FILE_AUDIT_LOG_ADDRESS, "relative-to", "jboss.server.log.dir");
    }

    @Test(expected = CommandFailedException.class)
    public void addExistFileAuditLogNotAllowed() throws Exception {
        AddFileAuditLog addFileAuditLog = new AddFileAuditLog.Builder(TEST_FILE_AUDIT_LOG_NAME)
                .path("audit.log")
                .build();
        AddFileAuditLog addFileAuditLog2 = new AddFileAuditLog.Builder(TEST_FILE_AUDIT_LOG_NAME)
                .path("audit.log")
                .build();

        client.apply(addFileAuditLog);
        assertTrue("File audit log should be created", ops.exists(TEST_FILE_AUDIT_LOG_ADDRESS));
        client.apply(addFileAuditLog2);
        fail("File audit log CreaperTestFileAuditLog already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistFileAuditLogAllowed() throws Exception {
        AddFileAuditLog addFileAuditLog = new AddFileAuditLog.Builder(TEST_FILE_AUDIT_LOG_NAME)
                .path("audit.log")
                .build();
        AddFileAuditLog addFileAuditLog2 = new AddFileAuditLog.Builder(TEST_FILE_AUDIT_LOG_NAME)
                .path("modified-audit.log")
                .replaceExisting()
                .build();

        client.apply(addFileAuditLog);
        assertTrue("File audit log should be created", ops.exists(TEST_FILE_AUDIT_LOG_ADDRESS));
        client.apply(addFileAuditLog2);

        // check whether it was really rewritten
        checkAttribute(TEST_FILE_AUDIT_LOG_ADDRESS, "path", "modified-audit.log");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addFileAuditLog_nullName() throws Exception {
        new AddFileAuditLog.Builder(null)
                .path("audit.log")
                .build();

        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addFileAuditLog_emptyName() throws Exception {
        new AddFileAuditLog.Builder("")
                .path("audit.log")
                .build();

        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addFileAuditLog_nullPath() throws Exception {
        new AddFileAuditLog.Builder(TEST_FILE_AUDIT_LOG_NAME)
                .path(null)
                .build();

        fail("Creating command with null path should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addFileAuditLog_emptyPath() throws Exception {
        new AddFileAuditLog.Builder(TEST_FILE_AUDIT_LOG_NAME)
                .path("")
                .build();

        fail("Creating command with empty path should throw exception");
    }
}
