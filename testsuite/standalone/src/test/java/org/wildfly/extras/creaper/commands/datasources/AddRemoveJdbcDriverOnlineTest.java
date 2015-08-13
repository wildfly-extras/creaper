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
public class AddRemoveJdbcDriverOnlineTest {
    private Operations ops;
    private Administration administration;
    private OnlineManagementClient client;

    private static final String TEST_DRIVER_NAME = "testing-h2";
    private static final String TEST_DRIVER_MODULE_NAME = "com.h2database.h2";
    private static final Address TEST_DRIVER_ADDRESS =
            Address.subsystem("datasources").and("jdbc-driver", TEST_DRIVER_NAME);

    @Before
    public void connect() throws IOException {
        client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
        ops = new Operations(client);
        administration = new Administration(client);
    }

    @After
    public void cleanup() throws IOException, CliException, OperationException, TimeoutException, InterruptedException {
        try {
            ops.removeIfExists(TEST_DRIVER_ADDRESS);
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }


    @Test
    public void addDriver() throws CommandFailedException, IOException, OperationException {
        client.apply(new AddJdbcDriver.Builder(TEST_DRIVER_NAME, TEST_DRIVER_MODULE_NAME)
            .moduleSlot("main")
            .driverClass("org.h2.Driver")
            .datasourceClass("org.h2.jdbcx.JdbcDataSource")
            .xaDatasourceClass("org.h2.jdbcx.JdbcDataSource")
            .build());
        assertTrue("The driver should be created", ops.exists(TEST_DRIVER_ADDRESS));

        client.apply(new RemoveJdbcDriver(TEST_DRIVER_NAME));
        assertFalse("The driver should be removed", ops.exists(TEST_DRIVER_ADDRESS));
    }

    @Test(expected = CommandFailedException.class)
    public void addDriverExisting() throws CommandFailedException, IOException, OperationException {
        client.apply(new AddJdbcDriver.Builder("h2", TEST_DRIVER_MODULE_NAME).build());
        fail("Driver name 'h2' should exist in configuration, so the command should fail.");
    }

    @Test(expected = CommandFailedException.class)
    public void removeDriverNotExisting() throws CommandFailedException, IOException, OperationException {
        client.apply(new RemoveJdbcDriver("h2-non-existing"));
        fail("Driver name 'h2-non-existing' should not exist in configuration, so the command should fail.");
    }
}
