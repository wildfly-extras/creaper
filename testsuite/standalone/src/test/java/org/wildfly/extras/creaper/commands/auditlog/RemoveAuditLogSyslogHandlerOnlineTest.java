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
public class RemoveAuditLogSyslogHandlerOnlineTest {

    private static final String TEST_HANDLER_NAME = "creaper-syslog-handler";
    private static final Address TEST_HANDLER_ADDRESS = Address.coreService("management")
            .and("access", "audit")
            .and("syslog-handler", TEST_HANDLER_NAME);

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
    public void removeUdpHandler() throws Exception {
        AddAuditLogSyslogHandler addUdpSyslogHandler = new AddAuditLogSyslogHandler.UdpBuilder(TEST_HANDLER_NAME)
                .formatter("json-formatter")
                .build();
        client.apply(addUdpSyslogHandler);
        assertTrue("The syslog handler should be created", ops.exists(TEST_HANDLER_ADDRESS));

        RemoveAuditLogSyslogHandler removeSyslogHandler = new RemoveAuditLogSyslogHandler(TEST_HANDLER_NAME);
        client.apply(removeSyslogHandler);
        assertFalse("The syslog handler should be removed", ops.exists(TEST_HANDLER_ADDRESS));
    }

    @Test
    public void removeTcpHandler() throws Exception {
        AddAuditLogSyslogHandler addTcpSyslogHandler = new AddAuditLogSyslogHandler.TcpBuilder(TEST_HANDLER_NAME)
                .formatter("json-formatter")
                .build();
        client.apply(addTcpSyslogHandler);
        assertTrue("The syslog handler should be created", ops.exists(TEST_HANDLER_ADDRESS));

        RemoveAuditLogSyslogHandler removeSyslogHandler = new RemoveAuditLogSyslogHandler(TEST_HANDLER_NAME);
        client.apply(removeSyslogHandler);
        assertFalse("The syslog handler should be removed", ops.exists(TEST_HANDLER_ADDRESS));
    }

    @Test
    public void removeTLsHandler() throws Exception {
        AddAuditLogSyslogHandler addTlsSyslogHandler = new AddAuditLogSyslogHandler.TlsBuilder(TEST_HANDLER_NAME)
                .formatter("json-formatter")
                .keystorePassword("keystorePassword")
                .authenticationType(AuthenticationType.CLIENT_CERTIFICATE_STORE)
                .build();
        client.apply(addTlsSyslogHandler);
        assertTrue("The syslog handler should be created", ops.exists(TEST_HANDLER_ADDRESS));

        RemoveAuditLogSyslogHandler removeSyslogHandler = new RemoveAuditLogSyslogHandler(TEST_HANDLER_NAME);
        client.apply(removeSyslogHandler);
        assertFalse("The syslog handler should be removed", ops.exists(TEST_HANDLER_ADDRESS));
    }

    @Test(expected = CommandFailedException.class)
    public void removeNonExistingSyslogHandler() throws Exception {
        RemoveAuditLogSyslogHandler removeSyslogHandler = new RemoveAuditLogSyslogHandler("non-existing-file-handler");
        client.apply(removeSyslogHandler);

        fail("Specified syslog handler does not exist in configuration, an exception should be thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeSyslogHandler_nullName() throws Exception {
        new RemoveAuditLogSyslogHandler(null);
        fail("Creating command with null handler name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeSyslogHandler_emptyName() throws Exception {
        new RemoveAuditLogSyslogHandler("");
        fail("Creating command with empty handler name should throw exception");
    }
}
