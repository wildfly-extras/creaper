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
public class AddAuditLogUdpSyslogHandlerOnlineTest {

    private static final String TEST_HANDLER_NAME = "creaper-udp-handler";
    private static final Address TEST_HANDLER_ADDRESS = Address.coreService("management")
            .and("access", "audit")
            .and("syslog-handler", TEST_HANDLER_NAME);
    private static final Address TEST_HANDLER_PROTOCOL_ADDRESS = Address.coreService("management")
            .and("access", "audit")
            .and("syslog-handler", TEST_HANDLER_NAME)
            .and("protocol", "udp");

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
    public void addUdpHandler() throws Exception {
        AddAuditLogSyslogHandler addUdpSyslogHandler = new AddAuditLogSyslogHandler.UdpBuilder(TEST_HANDLER_NAME)
                .appName("application-name")
                .facility(SyslogFacilityType.NETWORK_NEWS)
                .formatter("json-formatter")
                .maxFailureCount(20)
                .maxLength(2048)
                .syslogFormat(SyslogFormatType.RFC5424)
                .truncate(false)
                .port(9898)
                .host("127.0.0.1")
                .build();
        client.apply(addUdpSyslogHandler);

        assertTrue("The UDP syslog handler should be created", ops.exists(TEST_HANDLER_ADDRESS));

        checkAttribute(TEST_HANDLER_ADDRESS, "app-name", "application-name");
        checkAttribute(TEST_HANDLER_ADDRESS, "facility", "NETWORK_NEWS");
        checkAttribute(TEST_HANDLER_ADDRESS, "formatter", "json-formatter");
        checkAttribute(TEST_HANDLER_ADDRESS, "max-failure-count", "20");
        checkAttribute(TEST_HANDLER_ADDRESS, "max-length", "2048");
        checkAttribute(TEST_HANDLER_ADDRESS, "syslog-format", "RFC5424");
        checkAttribute(TEST_HANDLER_ADDRESS, "truncate", "false");
        checkAttribute(TEST_HANDLER_PROTOCOL_ADDRESS, "host", "127.0.0.1");
        checkAttribute(TEST_HANDLER_PROTOCOL_ADDRESS, "port", "9898");
    }

    @Test
    public void overrideExistingUdpHandler() throws Exception {
        AddAuditLogSyslogHandler addUdpSyslogHandler = new AddAuditLogSyslogHandler.UdpBuilder(TEST_HANDLER_NAME)
                .formatter("json-formatter")
                .build();
        client.apply(addUdpSyslogHandler);
        assertTrue("The UDP syslog handler should be created", ops.exists(TEST_HANDLER_ADDRESS));

        AddAuditLogSyslogHandler addUdpSyslogHandler2 = new AddAuditLogSyslogHandler.UdpBuilder(TEST_HANDLER_NAME)
                .appName("application-name")
                .facility(SyslogFacilityType.MAIL_SYSTEM)
                .formatter("json-formatter")
                .maxFailureCount(20)
                .maxLength(2048)
                .syslogFormat(SyslogFormatType.RFC5424)
                .truncate(false)
                .port(9898)
                .host("127.0.0.1")
                .replaceExisting()
                .build();
        client.apply(addUdpSyslogHandler2);

        assertTrue("The UDP syslog handler should be created", ops.exists(TEST_HANDLER_ADDRESS));

        checkAttribute(TEST_HANDLER_ADDRESS, "app-name", "application-name");
        checkAttribute(TEST_HANDLER_ADDRESS, "facility", "MAIL_SYSTEM");
        checkAttribute(TEST_HANDLER_ADDRESS, "formatter", "json-formatter");
        checkAttribute(TEST_HANDLER_ADDRESS, "max-failure-count", "20");
        checkAttribute(TEST_HANDLER_ADDRESS, "max-length", "2048");
        checkAttribute(TEST_HANDLER_ADDRESS, "syslog-format", "RFC5424");
        checkAttribute(TEST_HANDLER_ADDRESS, "truncate", "false");
        checkAttribute(TEST_HANDLER_PROTOCOL_ADDRESS, "host", "127.0.0.1");
        checkAttribute(TEST_HANDLER_PROTOCOL_ADDRESS, "port", "9898");
    }

    @Test(expected = CommandFailedException.class)
    public void overrideExistingUdpHandler_notAllowed() throws Exception {
        AddAuditLogSyslogHandler addUdpSyslogHandler = new AddAuditLogSyslogHandler.UdpBuilder(TEST_HANDLER_NAME)
                .formatter("json-formatter")
                .build();
        client.apply(addUdpSyslogHandler);
        assertTrue("The UDP syslog handler should be created", ops.exists(TEST_HANDLER_ADDRESS));

        AddAuditLogSyslogHandler addUdpSyslogHandler2 = new AddAuditLogSyslogHandler.UdpBuilder(TEST_HANDLER_NAME)
                .appName("application-name")
                .formatter("json-formatter")
                .build();
        client.apply(addUdpSyslogHandler2);

        fail("File handler creaper-udp-handler already exists, an exception should be thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addUdpHandler_nullName() throws Exception {
        AddAuditLogSyslogHandler addUdpSyslogHandler = new AddAuditLogSyslogHandler.UdpBuilder(null)
                .formatter("json-formatter")
                .build();
        client.apply(addUdpSyslogHandler);

        fail("Creating command with null name should throw an exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addUdpHandler_emptyName() throws Exception {
        AddAuditLogSyslogHandler addUdpSyslogHandler = new AddAuditLogSyslogHandler.UdpBuilder("")
                .formatter("json-formatter")
                .build();
        client.apply(addUdpSyslogHandler);

        fail("Creating command with empty name should throw an exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addUdpHandler_nullFormatter() throws Exception {
        AddAuditLogSyslogHandler addUdpSyslogHandler = new AddAuditLogSyslogHandler.UdpBuilder(TEST_HANDLER_NAME)
                .formatter(null)
                .build();
        client.apply(addUdpSyslogHandler);

        fail("Creating command with null formatter should throw an exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addUdpHandler_emptyFormatter() throws Exception {
        AddAuditLogSyslogHandler addUdpSyslogHandler = new AddAuditLogSyslogHandler.UdpBuilder(TEST_HANDLER_NAME)
                .formatter("")
                .build();
        client.apply(addUdpSyslogHandler);

        fail("Creating command with empty formatter should throw an exception");
    }

    private void checkAttribute(Address address, String attribute, String expectedValue) throws IOException {
        ModelNodeResult readAttribute = ops.readAttribute(address, attribute);
        readAttribute.assertSuccess("Read operation for " + attribute + " failed");
        assertEquals("Read operation for " + attribute + " return wrong value", expectedValue,
                readAttribute.stringValue());
    }
}
