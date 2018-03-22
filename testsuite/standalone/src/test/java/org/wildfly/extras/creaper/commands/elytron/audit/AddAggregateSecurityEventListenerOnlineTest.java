package org.wildfly.extras.creaper.commands.elytron.audit;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class AddAggregateSecurityEventListenerOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_AGGREGATE_SECURITY_EVENT_LISTENER_NAME = "CreaperTestAggregateSecurityEventListener";
    private static final Address TEST_AGGREGATE_SECURITY_EVENT_LISTENER_ADDRESS = SUBSYSTEM_ADDRESS.and("aggregate-security-event-listener",
            TEST_AGGREGATE_SECURITY_EVENT_LISTENER_NAME);
    private static final String TEST_AGGREGATE_SECURITY_EVENT_LISTENER_NAME2 = "CreaperTestAggregateSecurityEventListener2";
    private static final Address TEST_AGGREGATE_SECURITY_EVENT_LISTENER_ADDRESS2 = SUBSYSTEM_ADDRESS.and("aggregate-security-event-listener",
            TEST_AGGREGATE_SECURITY_EVENT_LISTENER_NAME2);

    private static final String TEST_FILE_AUDIT_LOG_NAME = "CreaperTestFileAuditLog";
    private static final Address TEST_FILE_AUDIT_LOG_ADDRESS = SUBSYSTEM_ADDRESS
            .and("file-audit-log", TEST_FILE_AUDIT_LOG_NAME);
    private static final String TEST_FILE_AUDIT_LOG_NAME2 = "CreaperTestFileAuditLog2";
    private static final Address TEST_FILE_AUDIT_LOG_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("file-audit-log", TEST_FILE_AUDIT_LOG_NAME2);
    private static final String TEST_SYSLOG_AUDIT_LOG_NAME = "CreaperTestSyslogAuditLog";
    private static final Address TEST_SYSLOG_AUDIT_LOG_ADDRESS = SUBSYSTEM_ADDRESS
            .and("syslog-audit-log", TEST_SYSLOG_AUDIT_LOG_NAME);

    @BeforeClass
    public static void createElytronAuditLogs() throws Exception {
        OnlineManagementClient client = null;
        try {
            client = createManagementClient();
            AddFileAuditLog addFileAuditLog = new AddFileAuditLog.Builder(TEST_FILE_AUDIT_LOG_NAME)
                    .path("audit.log")
                    .build();
            client.apply(addFileAuditLog);
            AddFileAuditLog addFileAuditLog2 = new AddFileAuditLog.Builder(TEST_FILE_AUDIT_LOG_NAME2)
                    .path("second-audit.log")
                    .build();
            client.apply(addFileAuditLog2);

            AddSyslogAuditLog addSyslogAuditLog = new AddSyslogAuditLog.Builder(TEST_SYSLOG_AUDIT_LOG_NAME)
                    .serverAddress("localhost")
                    .port(9898)
                    .hostName("Elytron-audit")
                    .build();
            client.apply(addSyslogAuditLog);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @AfterClass
    public static void removeElytronAuditLogs() throws Exception {
        OnlineManagementClient client = null;
        try {
            client = createManagementClient();
            Operations ops = new Operations(client);
            ops.removeIfExists(TEST_FILE_AUDIT_LOG_ADDRESS);
            ops.removeIfExists(TEST_FILE_AUDIT_LOG_ADDRESS2);
            ops.removeIfExists(TEST_SYSLOG_AUDIT_LOG_ADDRESS);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_AGGREGATE_SECURITY_EVENT_LISTENER_ADDRESS);
        ops.removeIfExists(TEST_AGGREGATE_SECURITY_EVENT_LISTENER_ADDRESS2);

        administration.reloadIfRequired();
    }

    @Test
    public void addAggregateSecurityEventListener() throws Exception {
        AddAggregateSecurityEventListener addAggregateSecurityEventListener
                = new AddAggregateSecurityEventListener.Builder(TEST_AGGREGATE_SECURITY_EVENT_LISTENER_NAME)
                .addSecurityEventListeners(TEST_FILE_AUDIT_LOG_NAME, TEST_SYSLOG_AUDIT_LOG_NAME)
                .build();

        client.apply(addAggregateSecurityEventListener);

        assertTrue("Aggregate security event listener should be created",
                ops.exists(TEST_AGGREGATE_SECURITY_EVENT_LISTENER_ADDRESS));
        checkAttribute(TEST_AGGREGATE_SECURITY_EVENT_LISTENER_ADDRESS, "security-event-listeners[0]",
                TEST_FILE_AUDIT_LOG_NAME);
        checkAttribute(TEST_AGGREGATE_SECURITY_EVENT_LISTENER_ADDRESS, "security-event-listeners[1]",
                TEST_SYSLOG_AUDIT_LOG_NAME);
    }

    @Test
    public void addTwoAggregateSecurityEventListeners() throws Exception {
        AddAggregateSecurityEventListener addAggregateSecurityEventListener
                = new AddAggregateSecurityEventListener.Builder(TEST_AGGREGATE_SECURITY_EVENT_LISTENER_NAME)
                .addSecurityEventListeners(TEST_FILE_AUDIT_LOG_NAME, TEST_SYSLOG_AUDIT_LOG_NAME)
                .build();
        AddAggregateSecurityEventListener addAggregateSecurityEventListener2
                = new AddAggregateSecurityEventListener.Builder(TEST_AGGREGATE_SECURITY_EVENT_LISTENER_NAME2)
                .addSecurityEventListeners(TEST_FILE_AUDIT_LOG_NAME, TEST_SYSLOG_AUDIT_LOG_NAME)
                .build();

        client.apply(addAggregateSecurityEventListener);
        client.apply(addAggregateSecurityEventListener2);

        assertTrue("Aggregate security event listener should be created",
                ops.exists(TEST_AGGREGATE_SECURITY_EVENT_LISTENER_ADDRESS));
        assertTrue("Second aggregate security event listener should be created",
                ops.exists(TEST_AGGREGATE_SECURITY_EVENT_LISTENER_ADDRESS2));
    }

    @Test(expected = CommandFailedException.class)
    public void addExistAggregateSecurityEventListenersNotAllowed() throws Exception {
        AddAggregateSecurityEventListener addAggregateSecurityEventListener
                = new AddAggregateSecurityEventListener.Builder(TEST_AGGREGATE_SECURITY_EVENT_LISTENER_NAME)
                .addSecurityEventListeners(TEST_FILE_AUDIT_LOG_NAME, TEST_SYSLOG_AUDIT_LOG_NAME)
                .build();
        AddAggregateSecurityEventListener addAggregateSecurityEventListener2
                = new AddAggregateSecurityEventListener.Builder(TEST_AGGREGATE_SECURITY_EVENT_LISTENER_NAME)
                .addSecurityEventListeners(TEST_FILE_AUDIT_LOG_NAME, TEST_SYSLOG_AUDIT_LOG_NAME)
                .build();

        client.apply(addAggregateSecurityEventListener);
        assertTrue("Aggregate security event listener should be created",
                ops.exists(TEST_AGGREGATE_SECURITY_EVENT_LISTENER_ADDRESS));
        client.apply(addAggregateSecurityEventListener2);
        fail("Aggregate security event listener CreaperTestAggregateSecurityEventListener already exists in configuration, exception should be thrown");
    }

    @Test(expected = CommandFailedException.class)
    public void addExistAggregateSecurityEventListenersAllowed() throws Exception {
        AddAggregateSecurityEventListener addAggregateSecurityEventListener
                = new AddAggregateSecurityEventListener.Builder(TEST_AGGREGATE_SECURITY_EVENT_LISTENER_NAME)
                .addSecurityEventListeners(TEST_FILE_AUDIT_LOG_NAME, TEST_SYSLOG_AUDIT_LOG_NAME)
                .build();
        AddAggregateSecurityEventListener addAggregateSecurityEventListener2
                = new AddAggregateSecurityEventListener.Builder(TEST_AGGREGATE_SECURITY_EVENT_LISTENER_NAME)
                .addSecurityEventListeners(TEST_FILE_AUDIT_LOG_NAME, TEST_FILE_AUDIT_LOG_NAME2)
                .build();

        client.apply(addAggregateSecurityEventListener);
        assertTrue("Aggregate security event listener should be created",
                ops.exists(TEST_AGGREGATE_SECURITY_EVENT_LISTENER_ADDRESS));
        client.apply(addAggregateSecurityEventListener2);

        // check whether it was really rewritten
        checkAttribute(TEST_AGGREGATE_SECURITY_EVENT_LISTENER_ADDRESS, "security-event-listeners[1]",
                TEST_FILE_AUDIT_LOG_NAME2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregateSecurityEventListener_nullName() throws Exception {
        new AddAggregateSecurityEventListener.Builder(null)
                .addSecurityEventListeners(TEST_FILE_AUDIT_LOG_NAME, TEST_SYSLOG_AUDIT_LOG_NAME)
                .build();

        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregateSecurityEventListener_emptyName() throws Exception {
        new AddAggregateSecurityEventListener.Builder("")
                .addSecurityEventListeners(TEST_FILE_AUDIT_LOG_NAME, TEST_SYSLOG_AUDIT_LOG_NAME)
                .build();

        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregateSecurityEventListener_nullSecurityEventListeners() throws Exception {
        new AddAggregateSecurityEventListener.Builder(TEST_AGGREGATE_SECURITY_EVENT_LISTENER_NAME)
                .addSecurityEventListeners(null)
                .build();

        fail("Creating command with null security-event-listener name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregateSecurityEventListener_emptySecurityEventListeners() throws Exception {
        new AddAggregateSecurityEventListener.Builder(TEST_AGGREGATE_SECURITY_EVENT_LISTENER_NAME)
                .addSecurityEventListeners("")
                .build();

        fail("Creating command with empty security-event-listener name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregateSecurityEventListener_oneSecurityEventListener() throws Exception {
        new AddAggregateSecurityEventListener.Builder(TEST_AGGREGATE_SECURITY_EVENT_LISTENER_NAME)
                .addSecurityEventListeners(TEST_FILE_AUDIT_LOG_NAME)
                .build();

        fail("Creating command with only one security-event-listener name should throw exception");
    }
}
