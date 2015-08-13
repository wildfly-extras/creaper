package org.wildfly.extras.creaper.commands.datasources;

import org.jboss.arquillian.junit.Arquillian;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.online.CliException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class AddDataSourceOnlineTest {
    private OnlineManagementClient client;
    private Operations ops;
    private Administration administration;

    private static final String TEST_DATASOURCE_NAME = "creaper-ds";
    private static final Address TEST_DATASOURCE_ADDRESS =
            Address.subsystem("datasources").and("data-source", TEST_DATASOURCE_NAME);
    private static final String VALID_DS_URL = "jdbc:h2:mem:test-creaper;DB_CLOSE_DELAY=-1;";

    @Before
    public void connect() throws IOException {
        client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
        ops = new Operations(client);
        administration = new Administration(client);
    }

    @After
    public void cleanup() throws IOException, CliException, OperationException, TimeoutException, InterruptedException {
        try {
            ops.removeIfExists(TEST_DATASOURCE_ADDRESS);
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void addDataSource_commandSuccessful() throws Exception {
        AddDataSource addDataSource = new AddDataSource.Builder(TEST_DATASOURCE_NAME)
                .connectionUrl(VALID_DS_URL)
                .jndiName("java:/jboss/datasources/" + TEST_DATASOURCE_NAME)
                .driverName("h2")
                .connectable(false)
                .jta(false)
                .usernameAndPassword("creaper", "creaper")
                .useJavaContext(true)
                .maxPoolSize(666)
                .minPoolSize(0)
                .statisticsEnabled(false)
                .build();
        client.apply(addDataSource);

        assertTrue("The data source wasn't created", ops.exists(TEST_DATASOURCE_ADDRESS));
    }

    @Test(expected = CommandFailedException.class)
    public void addDataSource_commandFails() throws Exception {
        AddDataSource addDataSource = new AddDataSource.Builder(TEST_DATASOURCE_NAME)
                .jndiName("invalid-jndi-name")
                .driverName("h2")
                .connectionUrl("invalid-url")
                .build();
        client.apply(addDataSource);
    }

    @Test
    public void addDataSourceAndEnable_commandSuccessful() throws Exception {
        AddDataSource addDataSource = new AddDataSource.Builder(TEST_DATASOURCE_NAME)
                .connectionUrl(VALID_DS_URL)
                .jndiName("java:/jboss/datasources/" + TEST_DATASOURCE_NAME)
                .driverName("h2")
                .enableAfterCreate()
                .connectable(false)
                .jta(false)
                .usernameAndPassword("creaper", "creaper")
                .useJavaContext(true)
                .build();
        client.apply(addDataSource);

        assertTrue("The data source wasn't created", ops.exists(TEST_DATASOURCE_ADDRESS));
        assertTrue("The data source should be enabled",
                ops.readAttribute(TEST_DATASOURCE_ADDRESS, "enabled").booleanValue());
    }

    @Test
    public void addDataSourceAndEnable_commandFailed_incorrectPoolSize() throws Exception {
        try {
            AddDataSource addDataSource = new AddDataSource.Builder(TEST_DATASOURCE_NAME)
                    .connectionUrl(VALID_DS_URL)
                    .jndiName("java:/jboss/datasources/" + TEST_DATASOURCE_NAME)
                    .driverName("h2")
                    .maxPoolSize(-1) // improper value which causes enable to fail
                    .enableAfterCreate()
                    .build();
            client.apply(addDataSource);
            fail("Command should fail");
        } catch (IllegalArgumentException expected) {
            assertFalse("The datasource shouldn't be created as the command should be composite and enable should fail",
                    ops.exists(TEST_DATASOURCE_ADDRESS));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void addDataSource_commandFailed_urlNotDefined() throws Exception {
        AddDataSource addDataSource = new AddDataSource.Builder(TEST_DATASOURCE_NAME)
                .jndiName("java:/jboss/datasources/" + TEST_DATASOURCE_NAME)
                .driverName("h2")
                .maxPoolSize(1)
                .usernameAndPassword("creaper", "creaper")
                .securityDomain("other")
                .build();
        client.apply(addDataSource);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addDataSource_commandFailed_usernameAndSecurityDomainDefined() throws Exception {
        AddDataSource addDataSource = new AddDataSource.Builder(TEST_DATASOURCE_NAME)
                .jndiName("java:/jboss/datasources/" + TEST_DATASOURCE_NAME)
                .driverName("h2")
                .build();
        client.apply(addDataSource);
    }

    @Test
    public void addDataSource_commandSuccessful_fullLoad() throws Exception {
        AddDataSource addDataSource = new AddDataSource.Builder(TEST_DATASOURCE_NAME)
                .connectionUrl(VALID_DS_URL)
                .jndiName("java:/jboss/datasources/" + TEST_DATASOURCE_NAME)
                .driverName("h2")
                .usernameAndPassword("creaper", "creaper")

                .allocationRetry(3)
                .allocationRetryWaitMillis(1000)
                .allowMultipleUsers(true)
                .backgroundValidation(true)
                .backgroundValidationMillis(1000)
                .blockingTimeoutWaitMillis(1000)
                .checkValidConnectionSql("SELECT 1")
                .connectable(true)
                .addConnectionProperty("other-user-name", "right-its-other-username")
                .datasourceClass("org.h2.jdbcx.JdbcDataSource")
                .driverClass("org.h2.Driver")
                .exceptionSorterClass("org.jboss.jca.adapters.jdbc.extensions.novendor.NullExceptionSorter")
                .addExceptionSorterProperty("exception-sorter-prop", "ok")
                .idleTimeoutMinutes(10)
                .jta(true)
                .maxPoolSize(1)
                .minPoolSize(1)
                .newConnectionSql("SELECT 1")
                .flushStrategy(PoolFlushStrategy.ENTIRE_POOL)
                .prefill(true)
                .preparedStatementsCacheSize(3)
                .queryTimeout(20)
                // .reauthPluginClass() - no null reauth plugin exists
                // .securityDomain("other") - can't be used as interferes with usage of username
                .setTxQueryTimeout(true)
                .sharePreparedStatements(true)
                .spy(true)
                .staleConnectionCheckerClass("org.jboss.jca.adapters.jdbc.extensions.novendor.NullStaleConnectionChecker")
                .addStaleConnectionCheckerProperty("stale-checker-prop", "ok")
                .statisticsEnabled(true)
                .trackPreparedStatements(TrackStatementType.TRUE)
                .transactionIsolation(TransactionIsolation.TRANSACTION_REPEATABLE_READ)
                .urlDelimiter(";")
                // .urlSelectorStrategyClass() - can't find any implementation of SPI
                .useCcm(true)
                .useFastFailAllocation(false)
                .useJavaContext(true)
                .useStrictMinPoolSize(true)
                .useTryLock(60)
                .validateOnMatch(true)
                .validConnectionCheckerClass("org.jboss.jca.adapters.jdbc.extensions.novendor.NullValidConnectionChecker")
                .addValidConnectionCheckerProperty("valid-checker-prop", "ok")
                .build();

        client.apply(addDataSource);

        assertTrue("The datasource should be created", ops.exists(TEST_DATASOURCE_ADDRESS));
    }
}
