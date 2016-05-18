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
public class AddAuditLogTcpSyslogHandlerOnlineTest {

    private static final String TEST_HANDLER_NAME = "creaper-tcp-handler";
    private static final Address TEST_HANDLER_ADDRESS = Address.coreService("management")
            .and("access", "audit")
            .and("syslog-handler", TEST_HANDLER_NAME);
    private static final Address TEST_HANDLER_PROTOCOL_ADDRESS = Address.coreService("management")
            .and("access", "audit")
            .and("syslog-handler", TEST_HANDLER_NAME)
            .and("protocol", "tcp");

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
    public void addTcpHandler() throws Exception {
        AddAuditLogSyslogHandler addTcpSyslogHandler = new AddAuditLogSyslogHandler.TcpBuilder(TEST_HANDLER_NAME)
                .appName("application-name")
                .facility(SyslogFacilityType.NETWORK_NEWS)
                .formatter("json-formatter")
                .maxFailureCount(20)
                .maxLength(2048)
                .syslogFormat(SyslogFormatType.RFC5424)
                .truncate(false)
                .messageTransfer(MessageTransferType.OCTET_COUNTING)
                .reconnectTimeout(-1)
                .port(9898)
                .host("127.0.0.1")
                .build();
        client.apply(addTcpSyslogHandler);

        assertTrue("The TCP syslog handler should be created", ops.exists(TEST_HANDLER_ADDRESS));

        checkAttribute(TEST_HANDLER_ADDRESS, "app-name", "application-name");
        checkAttribute(TEST_HANDLER_ADDRESS, "facility", "NETWORK_NEWS");
        checkAttribute(TEST_HANDLER_ADDRESS, "formatter", "json-formatter");
        checkAttribute(TEST_HANDLER_ADDRESS, "max-failure-count", "20");
        checkAttribute(TEST_HANDLER_ADDRESS, "max-length", "2048");
        checkAttribute(TEST_HANDLER_ADDRESS, "syslog-format", "RFC5424");
        checkAttribute(TEST_HANDLER_ADDRESS, "truncate", "false");
        checkAttribute(TEST_HANDLER_PROTOCOL_ADDRESS, "host", "127.0.0.1");
        checkAttribute(TEST_HANDLER_PROTOCOL_ADDRESS, "port", "9898");
        checkAttribute(TEST_HANDLER_PROTOCOL_ADDRESS, "message-transfer", "OCTET_COUNTING");
        checkAttribute(TEST_HANDLER_PROTOCOL_ADDRESS, "reconnect-timeout", "-1");
    }

    @Test
    public void overrideExistingTcpHandler() throws Exception {
        AddAuditLogSyslogHandler addTcpSyslogHandler = new AddAuditLogSyslogHandler.TcpBuilder(TEST_HANDLER_NAME)
                .formatter("json-formatter")
                .build();
        client.apply(addTcpSyslogHandler);
        assertTrue("The TCP syslog handler should be created", ops.exists(TEST_HANDLER_ADDRESS));

        AddAuditLogSyslogHandler addTcpSyslogHandler2 = new AddAuditLogSyslogHandler.TcpBuilder(TEST_HANDLER_NAME)
                .appName("appName")
                .facility(SyslogFacilityType.SYSLOGD)
                .formatter("json-formatter")
                .maxFailureCount(15)
                .maxLength(1024)
                .syslogFormat(SyslogFormatType.RFC3164)
                .truncate(true)
                .host("localhost")
                .port(9898)
                .messageTransfer(MessageTransferType.NON_TRANSPARENT_FRAMING)
                .reconnectTimeout(-1)
                .replaceExisting()
                .build();
        client.apply(addTcpSyslogHandler2);

        assertTrue("The TCP syslog handler should be created", ops.exists(TEST_HANDLER_ADDRESS));

        checkAttribute(TEST_HANDLER_ADDRESS, "app-name", "appName");
        checkAttribute(TEST_HANDLER_ADDRESS, "facility", "SYSLOGD");
        checkAttribute(TEST_HANDLER_ADDRESS, "formatter", "json-formatter");
        checkAttribute(TEST_HANDLER_ADDRESS, "max-failure-count", "15");
        checkAttribute(TEST_HANDLER_ADDRESS, "max-length", "1024");
        checkAttribute(TEST_HANDLER_ADDRESS, "syslog-format", "RFC3164");
        checkAttribute(TEST_HANDLER_ADDRESS, "truncate", "true");
        checkAttribute(TEST_HANDLER_PROTOCOL_ADDRESS, "host", "localhost");
        checkAttribute(TEST_HANDLER_PROTOCOL_ADDRESS, "port", "9898");
        checkAttribute(TEST_HANDLER_PROTOCOL_ADDRESS, "message-transfer", "NON_TRANSPARENT_FRAMING");
        checkAttribute(TEST_HANDLER_PROTOCOL_ADDRESS, "reconnect-timeout", "-1");
    }

    @Test(expected = CommandFailedException.class)
    public void overrideExistingTcpHandler_notAllowed() throws Exception {
        AddAuditLogSyslogHandler addTcpSyslogHandler = new AddAuditLogSyslogHandler.TcpBuilder(TEST_HANDLER_NAME)
                .formatter("json-formatter")
                .build();
        client.apply(addTcpSyslogHandler);
        assertTrue("The TCP syslog handler should be created", ops.exists(TEST_HANDLER_ADDRESS));

        AddAuditLogSyslogHandler addTcpSyslogHandler2 = new AddAuditLogSyslogHandler.TcpBuilder(TEST_HANDLER_NAME)
                .appName("application-name")
                .formatter("json-formatter")
                .build();
        client.apply(addTcpSyslogHandler2);

        fail("File handler creaper-tcp-handler already exists, an exception should be thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTcpHandler_nullName() throws Exception {
        AddAuditLogSyslogHandler addTcpSyslogHandler = new AddAuditLogSyslogHandler.TcpBuilder(null)
                .formatter("json-formatter")
                .build();
        client.apply(addTcpSyslogHandler);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTcpHandler_emptyName() throws Exception {
        AddAuditLogSyslogHandler addTcpSyslogHandler = new AddAuditLogSyslogHandler.TcpBuilder("")
                .formatter("json-formatter")
                .build();
        client.apply(addTcpSyslogHandler);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTcpHandler_nullFormatter() throws Exception {
        AddAuditLogSyslogHandler addTcpSyslogHandler = new AddAuditLogSyslogHandler.TcpBuilder(TEST_HANDLER_NAME)
                .formatter(null)
                .build();
        client.apply(addTcpSyslogHandler);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTcpHandler_emptyFormatter() throws Exception {
        AddAuditLogSyslogHandler addTcpSyslogHandler = new AddAuditLogSyslogHandler.TcpBuilder(TEST_HANDLER_NAME)
                .formatter("")
                .build();
        client.apply(addTcpSyslogHandler);
    }

    private void checkAttribute(Address address, String attribute, String expectedValue) throws IOException {
        ModelNodeResult readAttribute = ops.readAttribute(address, attribute);
        readAttribute.assertSuccess("Read operation for " + attribute + " failed");
        assertEquals("Read operation for " + attribute + " return wrong value", expectedValue,
                readAttribute.stringValue());
    }
}
