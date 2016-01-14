package org.wildfly.extras.creaper.commands.transactions;

import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.foundation.offline.ConfigurationFileBackup;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.ManagementVersion;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.test.ManualTests;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(ManualTests.class)
@RunWith(Arquillian.class)
public class ChangeBasicAttributesOnlineTest {

    private static final Address TEST_TRANSACTIONS_ADDRESS = Address.subsystem("transactions");
    private static final ConfigurationFileBackup CONFIGURATION_BACKUP = new ConfigurationFileBackup();
    private static OfflineManagementClient offlineClient;

    private OnlineManagementClient onlineClient = ManagementClient.onlineLazy(
            OnlineOptions.standalone().localDefault().build());
    private Operations ops = new Operations(onlineClient);

    @ArquillianResource
    private ContainerController controller;

    @Test
    @InSequence(1)
    public void startServer() throws IOException, CommandFailedException {
        offlineClient = ManagementClient.offline(
                OfflineOptions.standalone()
                        .rootDirectory(new File(System.getProperty("user.dir"), "target/jboss-as"))
                        .configurationFile("standalone.xml")
                        .build());
        offlineClient.apply(CONFIGURATION_BACKUP.backup());
        controller.start(ManualTests.ARQUILLIAN_CONTAINER);
    }

    @Test
    @InSequence(2)
    public void changeAll() throws Exception {
        OnlineCommand cmd = TransactionManager.basicAttributes()
                .enableTsmStatus(true)
                .journalStoreEnableAsyncIO(true)
                .jts(true)
                .nodeIdentifier("3")
                .timeout(999)
                .statisticsEnabled(true)
                .useJournalStore(true)
                .processIdSocketBinding("a")
                .processIdSocketMaxPorts(100)
                .socketBinding("socket-binding")
                .statusSocketBinding("status-binding")
                .recoveryListener(true)
                .objectStorePath("path")
                .objectStoreRelativeTo("relative-to")
                .build();

        onlineClient.apply(cmd);


        ModelNodeResult result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS, "node-identifier");
        result.assertSuccess();
        assertEquals("node identifier should be changed", "3", result.stringValue());

        result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS, "enable-tsm-status");
        result.assertSuccess();
        assertTrue("tsm status should be enabled", result.booleanValue());

        result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS, "default-timeout");
        result.assertSuccess();
        assertEquals("default timeout should be changed", 999, result.intValue());

        result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS,
                onlineClient.serverVersion().lessThanOrEqualTo(ManagementVersion.VERSION_3_0_0)
                        ? "enable-statistics" : "statistics-enabled");
        result.assertSuccess();
        assertTrue("statistics should be enabled", result.booleanValue());

        result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS, "jts");
        result.assertSuccess();
        assertTrue("jts should be enabled", result.booleanValue());

        result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS,
                onlineClient.serverVersion().lessThanOrEqualTo(ManagementVersion.VERSION_3_0_0)
                        ? "use-hornetq-store" : "use-journal-store");
        result.assertSuccess();
        assertTrue("journal store should be used", result.booleanValue());

        if (onlineClient.serverVersion().greaterThan(ManagementVersion.VERSION_3_0_0)) {
            result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS, "journal-store-enable-async-io");
            result.assertSuccess();
            assertTrue("async io should be enabled", result.booleanValue());
        }

        result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS, "process-id-uuid");
        result.assertSuccess();
        assertFalse("process id uuid should not be enabled", result.booleanValue());

        result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS, "process-id-socket-binding");
        result.assertSuccess();
        assertEquals("process-id-socket-binding should be changed", "a", result.stringValue());

        result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS, "process-id-socket-max-ports");
        result.assertSuccess();
        assertEquals("process-id-socket-max-ports should be changed", 100, result.intValue());

        result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS, "socket-binding");
        result.assertSuccess();
        assertEquals("socket-binding should be changed", "socket-binding", result.stringValue());

        result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS, "status-socket-binding");
        result.assertSuccess();
        assertEquals("status-socket-binding should be changed", "status-binding", result.stringValue());

        result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS, "recovery-listener");
        result.assertSuccess();
        assertTrue("recovery-listener should be enabled", result.booleanValue());

        result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS, "object-store-path");
        result.assertSuccess();
        assertEquals("object-store-path should be changed", "path", result.stringValue());

        result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS, "object-store-relative-to");
        result.assertSuccess();
        assertEquals("object-store-relative-to should be changed", "relative-to", result.stringValue());


        cmd = TransactionManager.basicAttributes()
                .processIdUuid(true)
                .build();

        onlineClient.apply(cmd);

        result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS, "process-id-uuid");
        result.assertSuccess();
        assertTrue("process id uuid should be used", result.booleanValue());


        result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS, "node-identifier");
        result.assertSuccess();
        assertEquals("node identifier should not be changed", "3", result.stringValue());

        result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS, "enable-tsm-status");
        result.assertSuccess();
        assertTrue("tsm status should be enabled", result.booleanValue());

        result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS, "default-timeout");
        result.assertSuccess();
        assertEquals("default timeout should be changed", 999, result.intValue());

        result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS,
                onlineClient.serverVersion().lessThanOrEqualTo(ManagementVersion.VERSION_3_0_0)
                        ? "enable-statistics" : "statistics-enabled");
        result.assertSuccess();
        assertTrue("statistics should be enabled", result.booleanValue());

        result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS, "jts");
        result.assertSuccess();
        assertTrue("jts should be enabled", result.booleanValue());

        result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS,
                onlineClient.serverVersion().lessThanOrEqualTo(ManagementVersion.VERSION_3_0_0)
                        ? "use-hornetq-store" : "use-journal-store");
        result.assertSuccess();
        assertTrue("journal store should be used", result.booleanValue());

        if (onlineClient.serverVersion().greaterThan(ManagementVersion.VERSION_3_0_0)) {
            result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS, "journal-store-enable-async-io");
            result.assertSuccess();
            assertTrue("async io should be enabled", result.booleanValue());
        }

        result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS, "process-id-uuid");
        result.assertSuccess();
        assertTrue("process id uuid should be enabled", result.booleanValue());

        result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS, "socket-binding");
        result.assertSuccess();
        assertEquals("socket-binding should not be changed", "socket-binding", result.stringValue());

        result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS, "status-socket-binding");
        result.assertSuccess();
        assertEquals("status-socket-binding should not be changed", "status-binding", result.stringValue());

        result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS, "recovery-listener");
        result.assertSuccess();
        assertTrue("recovery-listener should not be enabled", result.booleanValue());

        result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS, "object-store-path");
        result.assertSuccess();
        assertEquals("object-store-path should not be changed", "path", result.stringValue());

        result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS, "object-store-relative-to");
        result.assertSuccess();
        assertEquals("object-store-relative-to should not be changed", "relative-to", result.stringValue());
    }

    @Test
    @InSequence(3)
    public void stopServer() throws CommandFailedException, IOException {
        controller.stop(ManualTests.ARQUILLIAN_CONTAINER);
        offlineClient.apply(CONFIGURATION_BACKUP.restore());
    }
}
