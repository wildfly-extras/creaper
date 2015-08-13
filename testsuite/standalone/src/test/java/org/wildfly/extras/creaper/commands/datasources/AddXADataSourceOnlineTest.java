package org.wildfly.extras.creaper.commands.datasources;

import org.jboss.arquillian.junit.Arquillian;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class AddXADataSourceOnlineTest {
    private OnlineManagementClient client;
    private Operations ops;
    private Administration administration;

    private static final String TEST_XA_DATASOURCE_NAME = "creaper-xa-ds";
    private static final Address TEST_XA_DATASOURCE_ADDRESS =
            Address.subsystem("datasources").and("xa-data-source", TEST_XA_DATASOURCE_NAME);

    @Before
    public void connect() throws IOException {
        client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
        ops = new Operations(client);
        administration = new Administration(client);
    }

    @After
    public void cleanup() throws Exception {
        try {
            ops.removeIfExists(TEST_XA_DATASOURCE_ADDRESS);
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test(expected = CommandFailedException.class)
    public void addXADataSource_commandFails() throws Exception {
        AddXADataSource addDataSource = new AddXADataSource.Builder(TEST_XA_DATASOURCE_NAME)
                .jndiName("invalid-jndi-name")
                .driverName("h2")
                .build();
        client.apply(addDataSource);
    }

    @Test
    public void addXADataSourceWithoutProps_commandSuccessful() throws Exception {
        client.apply(new AddXADataSource.Builder(TEST_XA_DATASOURCE_NAME)
                .jndiName("java:/jboss/datasources/" + TEST_XA_DATASOURCE_NAME)
                .driverName("h2")
                .usernameAndPassword("creaper", "creaper")
                .build());
        assertTrue("The XA datasource should be created", ops.exists(TEST_XA_DATASOURCE_ADDRESS));
    }

    @Test
    public void addXADataSource_replaceExisting() throws Exception {
        client.apply(new AddXADataSource.Builder(TEST_XA_DATASOURCE_NAME)
            .jndiName("java:/jboss/datasources/" + TEST_XA_DATASOURCE_NAME)
            .driverName("h2")
            .usernameAndPassword("creaper", "creaper")
            .build());
        assertTrue("The XA datasource should be created", ops.exists(TEST_XA_DATASOURCE_ADDRESS));

        client.apply(new AddXADataSource.Builder(TEST_XA_DATASOURCE_NAME)
            .jndiName("java:/jboss/datasources/" + TEST_XA_DATASOURCE_NAME)
            .driverName("h2")
            .usernameAndPassword("creaper2", "creaper2")
            .replaceExisting()
            .build());
        assertTrue("The XA datasource should be created", ops.exists(TEST_XA_DATASOURCE_ADDRESS));
        assertEquals("The XA datasource should be replaced with the new one with different username",
                "creaper2", ops.readAttribute(TEST_XA_DATASOURCE_ADDRESS, "user-name").get("result").asString());
    }

    @Test(expected = CommandFailedException.class)
    public void addXADataSource_failOnExisting() throws Exception {
        client.apply(new AddXADataSource.Builder(TEST_XA_DATASOURCE_NAME)
                .jndiName("java:/jboss/datasources/" + TEST_XA_DATASOURCE_NAME)
                .driverName("h2")
                .usernameAndPassword("creaper", "creaper")
                .build());
        assertTrue("The XA datasource should be created", ops.exists(TEST_XA_DATASOURCE_ADDRESS));

        client.apply(new AddXADataSource.Builder(TEST_XA_DATASOURCE_NAME)
                .jndiName("java:/jboss/datasources/" + TEST_XA_DATASOURCE_NAME)
                .driverName("h2")
                .usernameAndPassword("creaper2", "creaper2")
                .build());
        fail("The XA datasource should already exist, so an exception should be thrown");
    }

    @Test
    public void addXADataSourceWithProps_commandSuccessful() throws Exception {
        client.apply(new AddXADataSource.Builder(TEST_XA_DATASOURCE_NAME)
                .jndiName("java:/jboss/datasources/" + TEST_XA_DATASOURCE_NAME)
                .driverName("h2")
                .usernameAndPassword("creaper", "creaper")
                .xaDatasourceClass("org.h2.jdbcx.JdbcDataSource")
                .addXaDatasourceProperty("URL", "jdbc:h2:mem:test-xa-creaper;DB_CLOSE_DELAY=-1")
                .maxPoolSize(666)
                .minPoolSize(0)
                .statisticsEnabled(false)
                .enableAfterCreate()
                .build());
        assertTrue("The XA datasource should be created", ops.exists(TEST_XA_DATASOURCE_ADDRESS));
    }

    @Test
    public void addXADataSource_commandSuccessful_fullLoad() throws Exception {
        AddXADataSource addDataSource = new AddXADataSource.Builder(TEST_XA_DATASOURCE_NAME)
            .jndiName("java:/jboss/datasources/" + TEST_XA_DATASOURCE_NAME)
            .driverName("h2")
            .usernameAndPassword("creaper", "creaper")
            .xaDatasourceClass("org.h2.jdbcx.JdbcDataSource")
            .addXaDatasourceProperty("URL", "jdbc:h2:mem:test-xa-creaper;DB_CLOSE_DELAY=-1")

            .recoveryUsernameAndPassword("creaper", "creaper")
            // .recoverySecurityDomain("creaper-sd") invalid when username is set
            .recoveryPluginClass("org.jboss.jca.core.recovery.DefaultRecoveryPlugin")
            .addRecoveryPluginProperty("some-property", "some-value")
            .xaResourceTimeout(42)
            .sameRmOverride(true)
            .wrapXaResource(true)
            .noTxSeparatePool(true)
            .noRecovery(true)
            .padXid(true)
            .interleaving(true)

            .allocationRetry(3)
            .allocationRetryWaitMillis(1000)
            .allowMultipleUsers(true)
            .backgroundValidation(true)
            .backgroundValidationMillis(1000)
            .blockingTimeoutWaitMillis(1000)
            .checkValidConnectionSql("SELECT 1")
            .addXaDatasourceProperty("other-user-name", "right-its-other-username")
            .addXaDatasourceProperties(new HashMap<String, String>())
            .exceptionSorterClass("org.jboss.jca.adapters.jdbc.extensions.novendor.NullExceptionSorter")
            .addExceptionSorterProperty("exception-sorter-prop", "ok")
            .idleTimeoutMinutes(10)
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

        assertTrue("The XA datasource should be created", ops.exists(TEST_XA_DATASOURCE_ADDRESS));
    }
}
