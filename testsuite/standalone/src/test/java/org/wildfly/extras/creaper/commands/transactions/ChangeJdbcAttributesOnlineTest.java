package org.wildfly.extras.creaper.commands.transactions;

import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Assume;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category(ManualTests.class)
@RunWith(Arquillian.class)
public class ChangeJdbcAttributesOnlineTest {

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
        Assume.assumeTrue(onlineClient.version().greaterThanOrEqualTo(ServerVersion.VERSION_2_0_0));

        ChangeJdbcTransactionAttributes cmd = TransactionManager.jdbc()
                .useJdbcStore(true)
                .storeDatasource("datasource")
                .actionStoreDropTable(true)
                .actionStoreTablePrefix("action")
                .communicationStoreDropTable(true)
                .communicationStoreTablePrefix("communication")
                .stateStoreDropTable(true)
                .stateStoreTablePrefix("state")
                .build();

        onlineClient.apply(cmd);


        ModelNodeResult result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS, "use-jdbc-store");
        result.assertSuccess();
        assertTrue("jdbc store should be used", result.booleanValue());

        result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS, "jdbc-store-datasource");
        result.assertSuccess();
        assertEquals("jdbc store datasource should be changed", "datasource", result.stringValue());

        result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS, "jdbc-action-store-drop-table");
        result.assertSuccess();
        assertTrue("jdbc action store drop table should be enabled", result.booleanValue());

        result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS, "jdbc-action-store-table-prefix");
        result.assertSuccess();
        assertEquals("jdbc store datasource should be changed", "action", result.stringValue());

        result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS, "jdbc-communication-store-drop-table");
        result.assertSuccess();
        assertTrue("jdbc communication store drop table should be enabled", result.booleanValue());

        result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS, "jdbc-communication-store-table-prefix");
        result.assertSuccess();
        assertEquals("jdbc store datasource should be changed", "communication", result.stringValue());

        result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS, "jdbc-state-store-drop-table");
        result.assertSuccess();
        assertTrue("jdbc state store drop table should be enabled", result.booleanValue());

        result = ops.readAttribute(TEST_TRANSACTIONS_ADDRESS, "jdbc-state-store-table-prefix");
        result.assertSuccess();
        assertEquals("jdbc store datasource should be changed", "state", result.stringValue());
    }

    @Test
    @InSequence(3)
    public void stopServer() throws CommandFailedException, IOException {
        controller.stop(ManualTests.ARQUILLIAN_CONTAINER);
        offlineClient.apply(CONFIGURATION_BACKUP.restore());
    }
}
