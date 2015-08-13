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

import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class RemoveDataSourceOnlineTest {
    private Operations ops;
    private Administration administration;
    private OnlineManagementClient client;

    private static final String TEST_DATASOURCE_NAME = "TestDS";
    private static final String TEST_DATASOURCE_JNDI = "java:/" + TEST_DATASOURCE_NAME;
    private static final Address TEST_DATASOURCE_ADDRESS =
            Address.subsystem("datasources").and("data-source", TEST_DATASOURCE_NAME);
    private static final Address TEST_XA_DATASOURCE_ADDRESS =
            Address.subsystem("datasources").and("xa-data-source", TEST_DATASOURCE_NAME);
    private static final String DS_URL = "jdbc:h2:mem:test-creaper;DB_CLOSE_DELAY=-1;";
    private static final String DRIVER_NAME = "h2";

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
            ops.removeIfExists(TEST_XA_DATASOURCE_ADDRESS);
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }


    @Test
    public void removeDataSource() throws CommandFailedException, IOException, OperationException {
        client.apply(new AddDataSource.Builder(TEST_DATASOURCE_NAME)
                .jndiName(TEST_DATASOURCE_JNDI)
                .driverName(DRIVER_NAME)
                .connectionUrl(DS_URL)
                .build());
        assertTrue("The datasource should be created", ops.exists(TEST_DATASOURCE_ADDRESS));

        client.apply(new RemoveDataSource(TEST_DATASOURCE_NAME));
    }

    @Test
    public void removeXaDataSource() throws CommandFailedException, IOException, OperationException {
        client.apply(new AddXADataSource.Builder(TEST_DATASOURCE_NAME)
                .jndiName(TEST_DATASOURCE_JNDI)
                .driverName(DRIVER_NAME)
                .build());
        assertTrue("The XA datasource should be created", ops.exists(TEST_XA_DATASOURCE_ADDRESS));

        client.apply(new RemoveXADataSource(TEST_DATASOURCE_NAME));
    }
}
