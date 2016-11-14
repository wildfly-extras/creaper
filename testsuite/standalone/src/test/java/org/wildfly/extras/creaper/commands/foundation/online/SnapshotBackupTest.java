package org.wildfly.extras.creaper.commands.foundation.online;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.logging.AddConsoleLogHandler;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.test.WildFly11Tests;
import org.wildfly.extras.creaper.test.WildFlyTests;
import org.xml.sax.SAXException;

@Category({WildFlyTests.class, WildFly11Tests.class})
@RunWith(Arquillian.class)
public class SnapshotBackupTest {

    private static final String TEST_RESOURCE_NAME = "TEST-CONSOLE-HANDLER";
    private static final Address TEST_RESOURCE_ADDRESS = Address.subsystem("logging")
            .and("console-handler", TEST_RESOURCE_NAME);

    private static OnlineManagementClient client;
    private static Operations ops;

    @Before
    public void connect() throws IOException {
        client = ManagementClient.online(OnlineOptions
                    .standalone()
                    .localDefault()
                    .build());
        ops = new Operations(client);
    }

    @After
    public void cleanup() throws IOException {
        if (client != null) {
            client.close();
        }
    }

    @Test(expected = CommandFailedException.class)
    public void restoreBeforeBackup() throws CommandFailedException, IOException {
        SnapshotBackup snapshotBackup = new SnapshotBackup();

        client.apply(snapshotBackup.restore()); // fail
    }

    @Test(expected = CommandFailedException.class)
    public void backupTwice() throws CommandFailedException, IOException, SAXException {
        SnapshotBackup snapshotBackup = new SnapshotBackup();

        client.apply(snapshotBackup.backup());
        client.apply(snapshotBackup.backup()); // fail

    }

    @Test
    public void restoreTwice() throws CommandFailedException, IOException, SAXException, InterruptedException {
        SnapshotBackup snapshotBackup = new SnapshotBackup();

        client.apply(snapshotBackup.backup());

        client.apply(snapshotBackup.restore());
        client.apply(snapshotBackup.restore());
    }

    @Test
    public void backupRestore() throws CommandFailedException, IOException, OperationException, InterruptedException {
        SnapshotBackup snapshotBackup = new SnapshotBackup();

        client.apply(snapshotBackup.backup());

        assertFalse("Resource should not exists", ops.exists(TEST_RESOURCE_ADDRESS));
        client.apply(new AddConsoleLogHandler.Builder(TEST_RESOURCE_NAME).build());
        assertTrue("Resource should exists", ops.exists(TEST_RESOURCE_ADDRESS));

        client.apply(snapshotBackup.restore());
        assertFalse("Resource should not exists", ops.exists(TEST_RESOURCE_ADDRESS));
    }

}
