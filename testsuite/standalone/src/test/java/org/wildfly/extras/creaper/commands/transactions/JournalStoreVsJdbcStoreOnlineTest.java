package org.wildfly.extras.creaper.commands.transactions;

import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.foundation.offline.ConfigurationFileBackup;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.test.ManualTests;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

@Category(ManualTests.class)
@RunWith(Arquillian.class)
@Ignore("https://github.com/wildfly-extras/creaper/issues/166")
public class JournalStoreVsJdbcStoreOnlineTest {

    private static final Address TRANSACTIONS_ADDRESS = Address.subsystem("transactions");
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
    public void enableJournalStore() throws Exception {
        assumeTrue(onlineClient.version().greaterThanOrEqualTo(ServerVersion.VERSION_1_5_0));
        assumeFalse(onlineClient.version().equals(ServerVersion.VERSION_3_0_0));

        onlineClient.apply(TransactionManager.basicAttributes().useJournalStore(true).build());

        ModelNodeResult result = ops.readAttribute(TRANSACTIONS_ADDRESS, journalStoreAttribute());
        result.assertSuccess();
        assertTrue(result.booleanValue());

        result = ops.readAttribute(TRANSACTIONS_ADDRESS, "use-jdbc-store");
        result.assertSuccess();
        assertFalse(result.booleanValue());

        onlineClient.apply(TransactionManager.basicAttributes().useJournalStore(false).build());

        result = ops.readAttribute(TRANSACTIONS_ADDRESS, journalStoreAttribute());
        result.assertSuccess();
        assertFalse(result.booleanValue());
    }

    @Test
    @InSequence(3)
    public void reloadServer() throws CommandFailedException, IOException {
        controller.stop(ManualTests.ARQUILLIAN_CONTAINER);
        controller.start(ManualTests.ARQUILLIAN_CONTAINER);
    }

    @Test
    @InSequence(4)
    public void enableJdbcStore() throws Exception {
        assumeTrue(onlineClient.version().greaterThanOrEqualTo(ServerVersion.VERSION_1_5_0));
        assumeFalse(onlineClient.version().equals(ServerVersion.VERSION_3_0_0));

        onlineClient.apply(TransactionManager.jdbc().useJdbcStore(true).storeDatasource("datasource").build());

        ModelNodeResult result = ops.readAttribute(TRANSACTIONS_ADDRESS, "use-jdbc-store");
        result.assertSuccess();
        assertTrue(result.booleanValue());

        result = ops.readAttribute(TRANSACTIONS_ADDRESS, journalStoreAttribute());
        result.assertSuccess();
        assertFalse(result.booleanValue());
    }

    @Test
    @InSequence(5)
    public void stopServer() throws CommandFailedException, IOException {
        controller.stop(ManualTests.ARQUILLIAN_CONTAINER);
        offlineClient.apply(CONFIGURATION_BACKUP.restore());
    }

    private String journalStoreAttribute() throws IOException {
        return onlineClient.version().lessThan(ServerVersion.VERSION_4_0_0) ? "use-hornetq-store" : "use-journal-store";
    }
}

