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
public class AddPeriodicRotatingFileAuditLogOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_PERIODIC_ROTATING_FILE_AUDIT_LOG_NAME = "CreaperTestPeriodicRotatingFileAuditLog";
    private static final Address TEST_PERIODIC_ROTATING_FILE_AUDIT_LOG_ADDRESS = SUBSYSTEM_ADDRESS
            .and("periodic-rotating-file-audit-log", TEST_PERIODIC_ROTATING_FILE_AUDIT_LOG_NAME);
    private static final String TEST_PERIODIC_ROTATING_FILE_AUDIT_LOG_NAME2
            = "CreaperTestPeriodicRotatingFileAuditLog2";
    private static final Address TEST_PERIODIC_ROTATING_FILE_AUDIT_LOG_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("periodic-rotating-file-audit-log", TEST_PERIODIC_ROTATING_FILE_AUDIT_LOG_NAME2);

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_PERIODIC_ROTATING_FILE_AUDIT_LOG_ADDRESS);
        ops.removeIfExists(TEST_PERIODIC_ROTATING_FILE_AUDIT_LOG_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimplePeriodicRotatingFileAuditLog() throws Exception {
        AddPeriodicRotatingFileAuditLog addPeriodicRotatingFileAuditLog
                = new AddPeriodicRotatingFileAuditLog.Builder(TEST_PERIODIC_ROTATING_FILE_AUDIT_LOG_NAME)
                .path("audit.log")
                .suffix("MM.dd")
                .build();

        client.apply(addPeriodicRotatingFileAuditLog);

        assertTrue("Periodic rotating file audit log should be created",
                ops.exists(TEST_PERIODIC_ROTATING_FILE_AUDIT_LOG_ADDRESS));
    }

    @Test
    public void addTwoPeriodicRotatingFileAuditLogs() throws Exception {
        AddPeriodicRotatingFileAuditLog addPeriodicRotatingFileAuditLog
                = new AddPeriodicRotatingFileAuditLog.Builder(TEST_PERIODIC_ROTATING_FILE_AUDIT_LOG_NAME)
                .path("audit.log")
                .suffix("MM.dd")
                .build();
        AddPeriodicRotatingFileAuditLog addPeriodicRotatingFileAuditLog2
                = new AddPeriodicRotatingFileAuditLog.Builder(TEST_PERIODIC_ROTATING_FILE_AUDIT_LOG_NAME2)
                .path("audit.log")
                .suffix("MM.dd")
                .build();

        client.apply(addPeriodicRotatingFileAuditLog);
        client.apply(addPeriodicRotatingFileAuditLog2);

        assertTrue("Periodic rotating file audit log should be created",
                ops.exists(TEST_PERIODIC_ROTATING_FILE_AUDIT_LOG_ADDRESS));
        assertTrue("Second periodic rotating file audit log should be created",
                ops.exists(TEST_PERIODIC_ROTATING_FILE_AUDIT_LOG_ADDRESS2));
    }

    @Test
    public void addFullPeriodicRotatingFileAuditLog() throws Exception {
        AddPeriodicRotatingFileAuditLog addPeriodicRotatingFileAuditLog
                = new AddPeriodicRotatingFileAuditLog.Builder(TEST_PERIODIC_ROTATING_FILE_AUDIT_LOG_NAME)
                .path("audit.log")
                .format(AuditFormat.SIMPLE)
                .paramSynchronized(false)
                .relativeTo("jboss.server.log.dir")
                .suffix("MM.dd")
                .build();

        client.apply(addPeriodicRotatingFileAuditLog);

        assertTrue("Periodic rotating file audit log should be created",
                ops.exists(TEST_PERIODIC_ROTATING_FILE_AUDIT_LOG_ADDRESS));
        checkAttribute(TEST_PERIODIC_ROTATING_FILE_AUDIT_LOG_ADDRESS, "path", "audit.log");
        checkAttribute(TEST_PERIODIC_ROTATING_FILE_AUDIT_LOG_ADDRESS, "format", "SIMPLE");
        checkAttribute(TEST_PERIODIC_ROTATING_FILE_AUDIT_LOG_ADDRESS, "synchronized", "false");
        checkAttribute(TEST_PERIODIC_ROTATING_FILE_AUDIT_LOG_ADDRESS, "relative-to", "jboss.server.log.dir");
        checkAttribute(TEST_PERIODIC_ROTATING_FILE_AUDIT_LOG_ADDRESS, "suffix", "MM.dd");
    }

    @Test(expected = CommandFailedException.class)
    public void addExistPeriodicRotatingFileAuditLogNotAllowed() throws Exception {
        AddPeriodicRotatingFileAuditLog addPeriodicRotatingFileAuditLog
                = new AddPeriodicRotatingFileAuditLog.Builder(TEST_PERIODIC_ROTATING_FILE_AUDIT_LOG_NAME)
                .path("audit.log")
                .suffix("MM.dd")
                .build();
        AddPeriodicRotatingFileAuditLog addPeriodicRotatingFileAuditLog2
                = new AddPeriodicRotatingFileAuditLog.Builder(TEST_PERIODIC_ROTATING_FILE_AUDIT_LOG_NAME)
                .path("audit.log")
                .suffix("MM.dd")
                .build();

        client.apply(addPeriodicRotatingFileAuditLog);
        assertTrue("Periodic rotating file audit log should be created",
                ops.exists(TEST_PERIODIC_ROTATING_FILE_AUDIT_LOG_ADDRESS));
        client.apply(addPeriodicRotatingFileAuditLog2);
        fail("Periodic rotating file audit log CreaperTestPeriodicRotatingFileAuditLog already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistPeriodicRotatingFileAuditLogAllowed() throws Exception {
        AddPeriodicRotatingFileAuditLog addPeriodicRotatingFileAuditLog
                = new AddPeriodicRotatingFileAuditLog.Builder(TEST_PERIODIC_ROTATING_FILE_AUDIT_LOG_NAME)
                .path("audit.log")
                .suffix("MM.dd")
                .build();
        AddPeriodicRotatingFileAuditLog addPeriodicRotatingFileAuditLog2
                = new AddPeriodicRotatingFileAuditLog.Builder(TEST_PERIODIC_ROTATING_FILE_AUDIT_LOG_NAME)
                .path("modified-audit.log")
                .suffix("MM.dd")
                .replaceExisting()
                .build();

        client.apply(addPeriodicRotatingFileAuditLog);
        assertTrue("Periodic rotating file audit log should be created",
                ops.exists(TEST_PERIODIC_ROTATING_FILE_AUDIT_LOG_ADDRESS));
        client.apply(addPeriodicRotatingFileAuditLog2);

        // check whether it was really rewritten
        checkAttribute(TEST_PERIODIC_ROTATING_FILE_AUDIT_LOG_ADDRESS, "path", "modified-audit.log");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addPeriodicRotatingFileAuditLog_nullName() throws Exception {
        new AddPeriodicRotatingFileAuditLog.Builder(null)
                .path("audit.log")
                .suffix("MM.dd")
                .build();

        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addPeriodicRotatingFileAuditLog_emptyName() throws Exception {
        new AddPeriodicRotatingFileAuditLog.Builder("")
                .path("audit.log")
                .suffix("MM.dd")
                .build();

        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addPeriodicRotatingFileAuditLog_nullPath() throws Exception {
        new AddPeriodicRotatingFileAuditLog.Builder(TEST_PERIODIC_ROTATING_FILE_AUDIT_LOG_NAME)
                .path(null)
                .suffix("MM.dd")
                .build();

        fail("Creating command with null path should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addPeriodicRotatingFileAuditLog_emptyPath() throws Exception {
        new AddPeriodicRotatingFileAuditLog.Builder(TEST_PERIODIC_ROTATING_FILE_AUDIT_LOG_NAME)
                .path("")
                .suffix("MM.dd")
                .build();

        fail("Creating command with empty path should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addPeriodicRotatingFileAuditLog_nullSuffix() throws Exception {
        new AddPeriodicRotatingFileAuditLog.Builder(TEST_PERIODIC_ROTATING_FILE_AUDIT_LOG_NAME)
                .path("audit.log")
                .suffix(null)
                .build();

        fail("Creating command with null suffix should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addPeriodicRotatingFileAuditLog_emptySuffix() throws Exception {
        new AddPeriodicRotatingFileAuditLog.Builder(TEST_PERIODIC_ROTATING_FILE_AUDIT_LOG_NAME)
                .path("audit.log")
                .suffix("")
                .build();

        fail("Creating command with empty suffix should throw exception");
    }
}
