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
import org.wildfly.extras.creaper.core.ServerVersion;
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
public class AddAuditLogTlsSyslogHandlerOnlineTest {
    private static final String HANDLER_NAME = "creaper-tls-handler";
    private static final Address TEST_HANDLER_ADDRESS = Address.coreService("management")
            .and("access", "audit")
            .and("syslog-handler", HANDLER_NAME);
    private static final Address TEST_HANDLER_PROTOCOL_ADDRESS = Address.coreService("management")
            .and("access", "audit")
            .and("syslog-handler", HANDLER_NAME)
            .and("protocol", "tls");
    private static final Address TEST_HANDLER_TRUSTSTORE_ADDRESS = Address.coreService("management")
            .and("access", "audit")
            .and("syslog-handler", HANDLER_NAME)
            .and("protocol", "tls")
            .and("authentication", "truststore");
    private static final Address TEST_HANDLER_CLIENT_CERTIFICATE_STORE_ADDRESS = Address.coreService("management")
            .and("access", "audit")
            .and("syslog-handler", HANDLER_NAME)
            .and("protocol", "tls")
            .and("authentication", "client-certificate-store");

    private OnlineManagementClient client;
    private Operations ops;
    private Administration administration;

    private boolean reconnectTimeoutSupported;

    @Before
    public void connect() throws IOException {
        client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
        ops = new Operations(client);
        administration = new Administration(client);

        reconnectTimeoutSupported = true;
        if (client.version().lessThan(ServerVersion.VERSION_1_7_0)
                || client.version().inRange(ServerVersion.VERSION_2_0_0, ServerVersion.VERSION_2_2_0)) {
            reconnectTimeoutSupported = false;
        }
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
    public void addTlsTruststoreHandler() throws Exception {
        AddAuditLogSyslogHandler.TlsBuilder addTlsSyslogHandler = new AddAuditLogSyslogHandler.TlsBuilder(HANDLER_NAME)
                .appName("application-name")
                .facility(SyslogFacilityType.CLOCK_DAEMON)
                .formatter("json-formatter")
                .maxFailureCount(20)
                .maxLength(2048)
                .syslogFormat(SyslogFormatType.RFC5424)
                .truncate(false)
                .messageTransfer(MessageTransferType.OCTET_COUNTING)
                .port(514)
                .host("127.0.0.1")
                .keystorePassword("keystorePassword")
                .keystorePath("test.keystore")
                .keystoreRelativeTo("jboss.dir")
                .authenticationType(AuthenticationType.TRUSTSTORE)
                .replaceExisting();
        if (reconnectTimeoutSupported) {
            addTlsSyslogHandler.reconnectTimeout(-1);
        }
        client.apply(addTlsSyslogHandler.build());

        assertTrue("The TLS syslog handler should be created", ops.exists(TEST_HANDLER_ADDRESS));

        checkAttribute(TEST_HANDLER_ADDRESS, "app-name", "application-name");
        checkAttribute(TEST_HANDLER_ADDRESS, "facility", "CLOCK_DAEMON");
        checkAttribute(TEST_HANDLER_ADDRESS, "formatter", "json-formatter");
        checkAttribute(TEST_HANDLER_ADDRESS, "max-failure-count", "20");
        checkAttribute(TEST_HANDLER_ADDRESS, "max-length", "2048");
        checkAttribute(TEST_HANDLER_ADDRESS, "syslog-format", "RFC5424");
        checkAttribute(TEST_HANDLER_ADDRESS, "truncate", "false");
        checkAttribute(TEST_HANDLER_PROTOCOL_ADDRESS, "host", "127.0.0.1");
        checkAttribute(TEST_HANDLER_PROTOCOL_ADDRESS, "port", "514");
        checkAttribute(TEST_HANDLER_PROTOCOL_ADDRESS, "message-transfer", "OCTET_COUNTING");
        if (reconnectTimeoutSupported) {
            checkAttribute(TEST_HANDLER_PROTOCOL_ADDRESS, "reconnect-timeout", "-1");
        }
        checkAttribute(TEST_HANDLER_TRUSTSTORE_ADDRESS, "keystore-password", "keystorePassword");
        checkAttribute(TEST_HANDLER_TRUSTSTORE_ADDRESS, "keystore-path", "test.keystore");
        checkAttribute(TEST_HANDLER_TRUSTSTORE_ADDRESS, "keystore-relative-to", "jboss.dir");
    }

    @Test
    public void addTlsClientCertHandler() throws Exception {
        AddAuditLogSyslogHandler.TlsBuilder addTlsSyslogHandler = new AddAuditLogSyslogHandler.TlsBuilder(HANDLER_NAME)
                .appName("application-name")
                .facility(SyslogFacilityType.CLOCK_DAEMON)
                .formatter("json-formatter")
                .maxFailureCount(20)
                .maxLength(2048)
                .syslogFormat(SyslogFormatType.RFC5424)
                .truncate(false)
                .messageTransfer(MessageTransferType.OCTET_COUNTING)
                .port(514)
                .host("127.0.0.1")
                .keyPassword("keyPassword")
                .keystorePassword("keystorePassword")
                .keystorePath("test.keystore")
                .keystoreRelativeTo("jboss.dir")
                .authenticationType(AuthenticationType.CLIENT_CERTIFICATE_STORE)
                .replaceExisting();
        if (reconnectTimeoutSupported) {
            addTlsSyslogHandler.reconnectTimeout(-1);
        }
        client.apply(addTlsSyslogHandler.build());

        assertTrue("The TLS syslog handler should be created", ops.exists(TEST_HANDLER_ADDRESS));

        checkAttribute(TEST_HANDLER_ADDRESS, "app-name", "application-name");
        checkAttribute(TEST_HANDLER_ADDRESS, "facility", "CLOCK_DAEMON");
        checkAttribute(TEST_HANDLER_ADDRESS, "formatter", "json-formatter");
        checkAttribute(TEST_HANDLER_ADDRESS, "max-failure-count", "20");
        checkAttribute(TEST_HANDLER_ADDRESS, "max-length", "2048");
        checkAttribute(TEST_HANDLER_ADDRESS, "syslog-format", "RFC5424");
        checkAttribute(TEST_HANDLER_ADDRESS, "truncate", "false");
        checkAttribute(TEST_HANDLER_PROTOCOL_ADDRESS, "host", "127.0.0.1");
        checkAttribute(TEST_HANDLER_PROTOCOL_ADDRESS, "port", "514");
        checkAttribute(TEST_HANDLER_PROTOCOL_ADDRESS, "message-transfer", "OCTET_COUNTING");
        if (reconnectTimeoutSupported) {
            checkAttribute(TEST_HANDLER_PROTOCOL_ADDRESS, "reconnect-timeout", "-1");
        }
        checkAttribute(TEST_HANDLER_CLIENT_CERTIFICATE_STORE_ADDRESS, "key-password", "keyPassword");
        checkAttribute(TEST_HANDLER_CLIENT_CERTIFICATE_STORE_ADDRESS, "keystore-password", "keystorePassword");
        checkAttribute(TEST_HANDLER_CLIENT_CERTIFICATE_STORE_ADDRESS, "keystore-path", "test.keystore");
        checkAttribute(TEST_HANDLER_CLIENT_CERTIFICATE_STORE_ADDRESS, "keystore-relative-to", "jboss.dir");
    }

    @Test
    public void overrideExistingTlsHandler() throws Exception {
        AddAuditLogSyslogHandler addTlsSyslogHandler = new AddAuditLogSyslogHandler.TlsBuilder(HANDLER_NAME)
                .formatter("json-formatter")
                .keystorePassword("keystorePassword")
                .authenticationType(AuthenticationType.CLIENT_CERTIFICATE_STORE)
                .build();
        client.apply(addTlsSyslogHandler);
        assertTrue("The TLS syslog handler should be created", ops.exists(TEST_HANDLER_ADDRESS));

        AddAuditLogSyslogHandler.TlsBuilder addTlsSyslogHandler2 = new AddAuditLogSyslogHandler.TlsBuilder(HANDLER_NAME)
                .appName("application-name")
                .facility(SyslogFacilityType.LOCAL_USE_3)
                .formatter("json-formatter")
                .maxFailureCount(10)
                .maxLength(1024)
                .syslogFormat(SyslogFormatType.RFC3164)
                .truncate(false)
                .messageTransfer(MessageTransferType.OCTET_COUNTING)
                .port(514)
                .host("127.0.0.1")
                .keyPassword("keyPassword")
                .keystorePassword("keystorePassword")
                .keystorePath("test.keystore")
                .keystoreRelativeTo("jboss.dir")
                .authenticationType(AuthenticationType.CLIENT_CERTIFICATE_STORE)
                .replaceExisting();
        if (reconnectTimeoutSupported) {
            addTlsSyslogHandler2.reconnectTimeout(-1);
        }
        client.apply(addTlsSyslogHandler2.build());

        assertTrue("The TLS syslog handler should be created", ops.exists(TEST_HANDLER_ADDRESS));

        checkAttribute(TEST_HANDLER_ADDRESS, "app-name", "application-name");
        checkAttribute(TEST_HANDLER_ADDRESS, "facility", "LOCAL_USE_3");
        checkAttribute(TEST_HANDLER_ADDRESS, "formatter", "json-formatter");
        checkAttribute(TEST_HANDLER_ADDRESS, "max-failure-count", "10");
        checkAttribute(TEST_HANDLER_ADDRESS, "max-length", "1024");
        checkAttribute(TEST_HANDLER_ADDRESS, "syslog-format", "RFC3164");
        checkAttribute(TEST_HANDLER_ADDRESS, "truncate", "false");
        checkAttribute(TEST_HANDLER_PROTOCOL_ADDRESS, "host", "127.0.0.1");
        checkAttribute(TEST_HANDLER_PROTOCOL_ADDRESS, "port", "514");
        checkAttribute(TEST_HANDLER_PROTOCOL_ADDRESS, "message-transfer", "OCTET_COUNTING");
        if (reconnectTimeoutSupported) {
            checkAttribute(TEST_HANDLER_PROTOCOL_ADDRESS, "reconnect-timeout", "-1");
        }
        checkAttribute(TEST_HANDLER_CLIENT_CERTIFICATE_STORE_ADDRESS, "key-password", "keyPassword");
        checkAttribute(TEST_HANDLER_CLIENT_CERTIFICATE_STORE_ADDRESS, "keystore-password", "keystorePassword");
        checkAttribute(TEST_HANDLER_CLIENT_CERTIFICATE_STORE_ADDRESS, "keystore-path", "test.keystore");
        checkAttribute(TEST_HANDLER_CLIENT_CERTIFICATE_STORE_ADDRESS, "keystore-relative-to", "jboss.dir");
    }

    @Test(expected = CommandFailedException.class)
    public void overrideExistingTlsHandler_notAllowed() throws Exception {
        AddAuditLogSyslogHandler addTlsSyslogHandler = new AddAuditLogSyslogHandler.TlsBuilder(HANDLER_NAME)
                .formatter("json-formatter")
                .keystorePassword("keystorePassword")
                .authenticationType(AuthenticationType.CLIENT_CERTIFICATE_STORE)
                .build();
        client.apply(addTlsSyslogHandler);
        assertTrue("The TLS syslog handler should be created", ops.exists(TEST_HANDLER_ADDRESS));

        AddAuditLogSyslogHandler addTlsSyslogHandler2 = new AddAuditLogSyslogHandler.TlsBuilder(HANDLER_NAME)
                .formatter("json-formatter")
                .keystorePassword("keystorePassword")
                .authenticationType(AuthenticationType.CLIENT_CERTIFICATE_STORE)
                .build();
        client.apply(addTlsSyslogHandler2);

        fail("File handler creaper-tls-handler already exists, an exception should be thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTlsHandler_nullName() throws Exception {
        AddAuditLogSyslogHandler addTlsSyslogHandler = new AddAuditLogSyslogHandler.TlsBuilder(null)
                .formatter("json-formatter")
                .keystorePassword("keystorePassord")
                .authenticationType(AuthenticationType.TRUSTSTORE)
                .build();
        client.apply(addTlsSyslogHandler);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTlsHandler_emptyName() throws Exception {
        AddAuditLogSyslogHandler addTlsSyslogHandler = new AddAuditLogSyslogHandler.TlsBuilder("")
                .formatter("json-formatter")
                .keystorePassword("keystorePassord")
                .authenticationType(AuthenticationType.CLIENT_CERTIFICATE_STORE)
                .build();
        client.apply(addTlsSyslogHandler);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTlsHandler_nullFormatter() throws Exception {
        AddAuditLogSyslogHandler addTlsSyslogHandler = new AddAuditLogSyslogHandler.TlsBuilder(HANDLER_NAME)
                .formatter(null)
                .keystorePassword("keystorePassord")
                .authenticationType(AuthenticationType.CLIENT_CERTIFICATE_STORE)
                .build();
        client.apply(addTlsSyslogHandler);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTlsHandler_emptyFormatter() throws Exception {
        AddAuditLogSyslogHandler addTlsSyslogHandler = new AddAuditLogSyslogHandler.TlsBuilder(HANDLER_NAME)
                .formatter("")
                .keystorePassword("keystorePassord")
                .authenticationType(AuthenticationType.TRUSTSTORE)
                .build();
        client.apply(addTlsSyslogHandler);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTlsHandler_nullKeystorePassword() throws Exception {
        AddAuditLogSyslogHandler addTlsSyslogHandler = new AddAuditLogSyslogHandler.TlsBuilder(HANDLER_NAME)
                .formatter("json-formatter")
                .keystorePassword(null)
                .authenticationType(AuthenticationType.CLIENT_CERTIFICATE_STORE)
                .build();
        client.apply(addTlsSyslogHandler);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTlsHandler_emptyKeystorePassword() throws Exception {
        AddAuditLogSyslogHandler addTlsSyslogHandler = new AddAuditLogSyslogHandler.TlsBuilder(HANDLER_NAME)
                .formatter("json-formatter")
                .keystorePassword("")
                .authenticationType(AuthenticationType.TRUSTSTORE)
                .build();
        client.apply(addTlsSyslogHandler);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTlsHandler_nullAuthenticationType() throws Exception {
        AddAuditLogSyslogHandler addTlsSyslogHandler = new AddAuditLogSyslogHandler.TlsBuilder(HANDLER_NAME)
                .formatter("json-formatter")
                .keystorePassword("keystorePassword")
                .authenticationType(null)
                .build();
        client.apply(addTlsSyslogHandler);
    }

    private void checkAttribute(Address address, String attribute, String expectedValue) throws IOException {
        ModelNodeResult readAttribute = ops.readAttribute(address, attribute);
        readAttribute.assertSuccess("Read operation for " + attribute + " failed");
        assertEquals("Read operation for " + attribute + " return wrong value", expectedValue,
                readAttribute.stringValue());
    }
}
