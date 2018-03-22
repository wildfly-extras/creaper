package org.wildfly.extras.creaper.commands.elytron.audit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;

@RunWith(Arquillian.class)
public class AddSizeRotatingFileAuditLogOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_SIZE_ROTATING_FILE_AUDIT_LOG_NAME = "CreaperTestSizeRotatingFileAuditLog";
    private static final Address TEST_SIZE_ROTATING_FILE_AUDIT_LOG_ADDRESS = SUBSYSTEM_ADDRESS
            .and("size-rotating-file-audit-log", TEST_SIZE_ROTATING_FILE_AUDIT_LOG_NAME);
    private static final String TEST_SIZE_ROTATING_FILE_AUDIT_LOG_NAME2 = "CreaperTestSizeRotatingFileAuditLog2";
    private static final Address TEST_SIZE_ROTATING_FILE_AUDIT_LOG_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("size-rotating-file-audit-log", TEST_SIZE_ROTATING_FILE_AUDIT_LOG_NAME2);

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_SIZE_ROTATING_FILE_AUDIT_LOG_ADDRESS);
        ops.removeIfExists(TEST_SIZE_ROTATING_FILE_AUDIT_LOG_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleSizeRotatingFileAuditLog() throws Exception {
        AddSizeRotatingFileAuditLog addSizeRotatingFileAuditLog
                = new AddSizeRotatingFileAuditLog.Builder(TEST_SIZE_ROTATING_FILE_AUDIT_LOG_NAME)
                .path("audit.log")
                .build();

        client.apply(addSizeRotatingFileAuditLog);

        assertTrue("Size rotating file audit log should be created",
                ops.exists(TEST_SIZE_ROTATING_FILE_AUDIT_LOG_ADDRESS));
    }

    @Test
    public void addTwoSizeRotatingFileAuditLogs() throws Exception {
        AddSizeRotatingFileAuditLog addSizeRotatingFileAuditLog
                = new AddSizeRotatingFileAuditLog.Builder(TEST_SIZE_ROTATING_FILE_AUDIT_LOG_NAME)
                .path("audit.log")
                .build();
        AddSizeRotatingFileAuditLog addSizeRotatingFileAuditLog2
                = new AddSizeRotatingFileAuditLog.Builder(TEST_SIZE_ROTATING_FILE_AUDIT_LOG_NAME2)
                .path("audit.log")
                .build();

        client.apply(addSizeRotatingFileAuditLog);
        client.apply(addSizeRotatingFileAuditLog2);

        assertTrue("Size rotating file audit log should be created",
                ops.exists(TEST_SIZE_ROTATING_FILE_AUDIT_LOG_ADDRESS));
        assertTrue("Second size rotating file audit log should be created",
                ops.exists(TEST_SIZE_ROTATING_FILE_AUDIT_LOG_ADDRESS2));
    }

    @Test
    public void addFullSizeRotatingFileAuditLog() throws Exception {
        AddSizeRotatingFileAuditLog addSizeRotatingFileAuditLog
                = new AddSizeRotatingFileAuditLog.Builder(TEST_SIZE_ROTATING_FILE_AUDIT_LOG_NAME)
                .path("audit.log")
                .format(AuditFormat.SIMPLE)
                .paramSynchronized(false)
                .relativeTo("jboss.server.log.dir")
                .suffix("MM.dd")
                .maxBackupIndex(10)
                .rotateOnBoot(true)
                .rotateSize("5M")
                .build();

        client.apply(addSizeRotatingFileAuditLog);

        assertTrue("Size rotating file audit log should be created",
                ops.exists(TEST_SIZE_ROTATING_FILE_AUDIT_LOG_ADDRESS));
        checkAttribute(TEST_SIZE_ROTATING_FILE_AUDIT_LOG_ADDRESS, "path", "audit.log");
        checkAttribute(TEST_SIZE_ROTATING_FILE_AUDIT_LOG_ADDRESS, "format", "SIMPLE");
        checkAttribute(TEST_SIZE_ROTATING_FILE_AUDIT_LOG_ADDRESS, "synchronized", "false");
        checkAttribute(TEST_SIZE_ROTATING_FILE_AUDIT_LOG_ADDRESS, "relative-to", "jboss.server.log.dir");
        checkAttribute(TEST_SIZE_ROTATING_FILE_AUDIT_LOG_ADDRESS, "suffix", "MM.dd");
        checkAttribute(TEST_SIZE_ROTATING_FILE_AUDIT_LOG_ADDRESS, "max-backup-index", "10");
        checkAttribute(TEST_SIZE_ROTATING_FILE_AUDIT_LOG_ADDRESS, "rotate-on-boot", "true");
        checkAttribute(TEST_SIZE_ROTATING_FILE_AUDIT_LOG_ADDRESS, "rotate-size", "5M");
    }

    @Test(expected = CommandFailedException.class)
    public void addExistSizeRotatingFileAuditLogNotAllowed() throws Exception {
        AddSizeRotatingFileAuditLog addSizeRotatingFileAuditLog
                = new AddSizeRotatingFileAuditLog.Builder(TEST_SIZE_ROTATING_FILE_AUDIT_LOG_NAME)
                .path("audit.log")
                .build();
        AddSizeRotatingFileAuditLog addSizeRotatingFileAuditLog2
                = new AddSizeRotatingFileAuditLog.Builder(TEST_SIZE_ROTATING_FILE_AUDIT_LOG_NAME)
                .path("audit.log")
                .build();

        client.apply(addSizeRotatingFileAuditLog);
        assertTrue("Size rotating file audit log should be created",
                ops.exists(TEST_SIZE_ROTATING_FILE_AUDIT_LOG_ADDRESS));
        client.apply(addSizeRotatingFileAuditLog2);
        fail("Size rotating file audit log CreaperTestSizeRotatingFileAuditLog already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistPeriodicRotatingFileAuditLogAllowed() throws Exception {
        AddSizeRotatingFileAuditLog addSizeRotatingFileAuditLog
                = new AddSizeRotatingFileAuditLog.Builder(TEST_SIZE_ROTATING_FILE_AUDIT_LOG_NAME)
                .path("audit.log")
                .build();
        AddSizeRotatingFileAuditLog addSizeRotatingFileAuditLog2
                = new AddSizeRotatingFileAuditLog.Builder(TEST_SIZE_ROTATING_FILE_AUDIT_LOG_NAME)
                .path("modified-audit.log")
                .replaceExisting()
                .build();

        client.apply(addSizeRotatingFileAuditLog);
        assertTrue("Size rotating file audit log should be created",
                ops.exists(TEST_SIZE_ROTATING_FILE_AUDIT_LOG_ADDRESS));
        client.apply(addSizeRotatingFileAuditLog2);

        // check whether it was really rewritten
        checkAttribute(TEST_SIZE_ROTATING_FILE_AUDIT_LOG_ADDRESS, "path", "modified-audit.log");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSizeRotatingFileAuditLog_nullName() throws Exception {
        new AddSizeRotatingFileAuditLog.Builder(null)
                .path("audit.log")
                .build();

        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSizeRotatingFileAuditLog_emptyName() throws Exception {
        new AddSizeRotatingFileAuditLog.Builder("")
                .path("audit.log")
                .build();

        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSizeRotatingFileAuditLog_nullPath() throws Exception {
        new AddSizeRotatingFileAuditLog.Builder(TEST_SIZE_ROTATING_FILE_AUDIT_LOG_NAME)
                .path(null)
                .build();

        fail("Creating command with null path should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSizeRotatingFileAuditLog_emptyPath() throws Exception {
        new AddSizeRotatingFileAuditLog.Builder(TEST_SIZE_ROTATING_FILE_AUDIT_LOG_NAME)
                .path("")
                .build();

        fail("Creating command with empty path should throw exception");
    }
}
