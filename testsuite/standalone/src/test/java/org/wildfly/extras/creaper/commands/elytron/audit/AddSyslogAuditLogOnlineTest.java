package org.wildfly.extras.creaper.commands.elytron.audit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.ServerSocket;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.commands.elytron.CredentialRef;
import org.wildfly.extras.creaper.commands.elytron.tls.AddClientSSLContext;
import org.wildfly.extras.creaper.commands.elytron.tls.AddKeyManager;
import org.wildfly.extras.creaper.commands.elytron.tls.AddKeyStore;

@RunWith(Arquillian.class)
public class AddSyslogAuditLogOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_SYSLOG_AUDIT_LOG_NAME = "CreaperTestSyslogAuditLog";
    private static final Address TEST_SYSLOG_AUDIT_LOG_ADDRESS = SUBSYSTEM_ADDRESS
            .and("syslog-audit-log", TEST_SYSLOG_AUDIT_LOG_NAME);
    private static final String TEST_SYSLOG_AUDIT_LOG_NAME2 = "CreaperTestSyslogAuditLog2";
    private static final Address TEST_SYSLOG_AUDIT_LOG_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("syslog-audit-log", TEST_SYSLOG_AUDIT_LOG_NAME2);

    private static final String TEST_CLIENT_SSL_CONTEXT_NAME = "CreaperTestSslContext";
    private static final Address TEST_CLIENT_SSL_CONTEXT_ADDRESS = SUBSYSTEM_ADDRESS
            .and("client-ssl-context", TEST_CLIENT_SSL_CONTEXT_NAME);
    private static final String TEST_KEY_STORE_NAME = "CreaperTestKeyStore";
    private static final Address TEST_KEY_STORE_NAME_ADDRESS = SUBSYSTEM_ADDRESS
            .and("key-store", TEST_KEY_STORE_NAME);
    private static final String TEST_KEY_MNGR_NAME = "CreaperTestKeyManager";
    private static final Address TEST_KEY_MNGR_NAME_ADDRESS = SUBSYSTEM_ADDRESS
            .and("key-manager", TEST_KEY_MNGR_NAME);

    private static final int SYSLOG_PORT = 9898;

    // Simple thread that just listens on defined port. This is because syslog handlers
    // check that the configured port is available and something listens there.
    private static Thread syslogServerThread = new Thread() {
        @Override
        public void run() {
            try (ServerSocket serverSocket = new ServerSocket(SYSLOG_PORT)) {
                while (true) {
                    serverSocket.accept();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    };

    @BeforeClass
    public static void prepare() {
        // Start the 'syslog server' because without it addition of the syslog handlers
        // would fail unless 'reconnect-attempts' is set to non-zero. Such check is
        // implemented since WildFly 18.
        syslogServerThread.start();
    }

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_SYSLOG_AUDIT_LOG_ADDRESS);
        ops.removeIfExists(TEST_SYSLOG_AUDIT_LOG_ADDRESS2);
        ops.removeIfExists(TEST_CLIENT_SSL_CONTEXT_ADDRESS);
        ops.removeIfExists(TEST_KEY_MNGR_NAME_ADDRESS);
        ops.removeIfExists(TEST_KEY_STORE_NAME_ADDRESS);
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void afterClass() {
        // Gently close the thread with fake 'syslog server'.
        syslogServerThread.interrupt();
    }

    @Test
    public void addSimpleSyslogAuditLog() throws Exception {
        AddSyslogAuditLog addSyslogAuditLog = new AddSyslogAuditLog.Builder(TEST_SYSLOG_AUDIT_LOG_NAME)
                .serverAddress("localhost")
                .port(SYSLOG_PORT)
                .hostName("Elytron-audit")
                .build();

        client.apply(addSyslogAuditLog);

        assertTrue("Syslog audit log should be created", ops.exists(TEST_SYSLOG_AUDIT_LOG_ADDRESS));
    }

    @Test
    public void addTwoSyslogAuditLogs() throws Exception {
        AddSyslogAuditLog addSyslogAuditLog = new AddSyslogAuditLog.Builder(TEST_SYSLOG_AUDIT_LOG_NAME)
                .serverAddress("localhost")
                .port(SYSLOG_PORT)
                .hostName("Elytron-audit")
                .build();
        AddSyslogAuditLog addSyslogAuditLog2 = new AddSyslogAuditLog.Builder(TEST_SYSLOG_AUDIT_LOG_NAME2)
                .serverAddress("localhost")
                .port(SYSLOG_PORT)
                .hostName("Elytron-audit")
                .build();

        client.apply(addSyslogAuditLog);
        client.apply(addSyslogAuditLog2);

        assertTrue("Syslog audit log should be created", ops.exists(TEST_SYSLOG_AUDIT_LOG_ADDRESS));
        assertTrue("Second syslog audit log should be created", ops.exists(TEST_SYSLOG_AUDIT_LOG_ADDRESS2));
    }

    @Test
    public void addFullSyslogAuditLog() throws Exception {
        AddKeyStore addKeyStore = new AddKeyStore.Builder(TEST_KEY_STORE_NAME)
                .type("JKS")
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText("password")
                        .build())
                .build();
        client.apply(addKeyStore);
        AddKeyManager addKeyManager = new AddKeyManager.Builder(TEST_KEY_MNGR_NAME)
                .keyStore(TEST_KEY_STORE_NAME)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText("password")
                        .build())
                .build();
        client.apply(addKeyManager);
        AddClientSSLContext addClientSSLContext = new AddClientSSLContext.Builder(TEST_CLIENT_SSL_CONTEXT_NAME)
                .keyManager(TEST_KEY_MNGR_NAME)
                .build();
        client.apply(addClientSSLContext);

        AddSyslogAuditLog addSyslogAuditLog = new AddSyslogAuditLog.Builder(TEST_SYSLOG_AUDIT_LOG_NAME)
                .serverAddress("localhost")
                .port(SYSLOG_PORT)
                .hostName("Elytron-audit")
                .format(AuditFormat.JSON)
                .transport(AddSyslogAuditLog.TransportProtocolType.UDP)
                .sslContext(TEST_CLIENT_SSL_CONTEXT_NAME)
                .build();

        client.apply(addSyslogAuditLog);

        assertTrue("Syslog audit log should be created", ops.exists(TEST_SYSLOG_AUDIT_LOG_ADDRESS));
        checkAttribute(TEST_SYSLOG_AUDIT_LOG_ADDRESS, "server-address", "localhost");
        checkAttribute(TEST_SYSLOG_AUDIT_LOG_ADDRESS, "port", String.valueOf(SYSLOG_PORT));
        checkAttribute(TEST_SYSLOG_AUDIT_LOG_ADDRESS, "host-name", "Elytron-audit");
        checkAttribute(TEST_SYSLOG_AUDIT_LOG_ADDRESS, "format", "JSON");
        checkAttribute(TEST_SYSLOG_AUDIT_LOG_ADDRESS, "transport", "UDP");
        checkAttribute(TEST_SYSLOG_AUDIT_LOG_ADDRESS, "ssl-context", TEST_CLIENT_SSL_CONTEXT_NAME);
    }

    @Test(expected = CommandFailedException.class)
    public void addExistSyslogAuditLogNotAllowed() throws Exception {
        AddSyslogAuditLog addSyslogAuditLog = new AddSyslogAuditLog.Builder(TEST_SYSLOG_AUDIT_LOG_NAME)
                .serverAddress("localhost")
                .port(SYSLOG_PORT)
                .hostName("Elytron-audit")
                .build();
        AddSyslogAuditLog addSyslogAuditLog2 = new AddSyslogAuditLog.Builder(TEST_SYSLOG_AUDIT_LOG_NAME)
                .serverAddress("localhost")
                .port(SYSLOG_PORT)
                .hostName("Elytron-audit")
                .build();

        client.apply(addSyslogAuditLog);
        assertTrue("Syslog audit log should be created", ops.exists(TEST_SYSLOG_AUDIT_LOG_ADDRESS));
        client.apply(addSyslogAuditLog2);
        fail("Syslog audit log CreaperTestSyslogAuditLog already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistSyslogAuditLogAllowed() throws Exception {
        AddSyslogAuditLog addSyslogAuditLog = new AddSyslogAuditLog.Builder(TEST_SYSLOG_AUDIT_LOG_NAME)
                .serverAddress("localhost")
                .port(SYSLOG_PORT)
                .hostName("Elytron-audit")
                .build();
        AddSyslogAuditLog addSyslogAuditLog2 = new AddSyslogAuditLog.Builder(TEST_SYSLOG_AUDIT_LOG_NAME)
                .serverAddress("localhost")
                .port(SYSLOG_PORT)
                .hostName("another-hostname")
                .replaceExisting()
                .build();

        client.apply(addSyslogAuditLog);
        assertTrue("Syslog audit log should be created", ops.exists(TEST_SYSLOG_AUDIT_LOG_ADDRESS));
        client.apply(addSyslogAuditLog2);

        // check whether it was really rewritten
        checkAttribute(TEST_SYSLOG_AUDIT_LOG_ADDRESS, "host-name", "another-hostname");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSyslogAuditLog_nullName() throws Exception {
        new AddSyslogAuditLog.Builder(null)
                .serverAddress("localhost")
                .port(SYSLOG_PORT)
                .hostName("Elytron-audit")
                .build();

        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSyslogAuditLog_emptyName() throws Exception {
        new AddSyslogAuditLog.Builder("")
                .serverAddress("localhost")
                .port(SYSLOG_PORT)
                .hostName("Elytron-audit")
                .build();

        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSyslogAuditLog_nullServerAddress() throws Exception {
        new AddSyslogAuditLog.Builder(TEST_SYSLOG_AUDIT_LOG_NAME)
                .serverAddress(null)
                .port(SYSLOG_PORT)
                .hostName("Elytron-audit")
                .build();

        fail("Creating command with null server-address should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSyslogAuditLog_emptyServerAddress() throws Exception {
        new AddSyslogAuditLog.Builder(TEST_SYSLOG_AUDIT_LOG_NAME)
                .serverAddress("")
                .port(SYSLOG_PORT)
                .hostName("Elytron-audit")
                .build();

        fail("Creating command with empty server-address should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSyslogAuditLog_undefinedPort() throws Exception {
        new AddSyslogAuditLog.Builder(TEST_SYSLOG_AUDIT_LOG_NAME)
                .serverAddress("localhost")
                .hostName("Elytron-audit")
                .build();

        fail("Creating command with undefined port should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSyslogAuditLog_nullHostName() throws Exception {
        new AddSyslogAuditLog.Builder(TEST_SYSLOG_AUDIT_LOG_NAME)
                .serverAddress("localhost")
                .port(SYSLOG_PORT)
                .hostName(null)
                .build();

        fail("Creating command with null host-name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSyslogAuditLog_emptyHostName() throws Exception {
        new AddSyslogAuditLog.Builder(TEST_SYSLOG_AUDIT_LOG_NAME)
                .serverAddress("localhost")
                .port(SYSLOG_PORT)
                .hostName("")
                .build();

        fail("Creating command with empty host-name should throw exception");
    }
}
