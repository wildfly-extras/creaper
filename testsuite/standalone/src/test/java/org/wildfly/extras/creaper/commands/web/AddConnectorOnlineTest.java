package org.wildfly.extras.creaper.commands.web;

import org.jboss.arquillian.junit.Arquillian;
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
import org.wildfly.extras.creaper.test.AS7Tests;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Category(AS7Tests.class)
@RunWith(Arquillian.class)
public class AddConnectorOnlineTest {
    private static final String TEST_CONNECTOR_NAME = "test-http";

    private OnlineManagementClient client;
    private Operations ops;
    private Administration admin;

    @Before
    public void connect() throws IOException {
        client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
        ops = new Operations(client);
        admin = new Administration(client);
    }

    @After
    public void close() throws IOException, CliException, OperationException, TimeoutException, InterruptedException {
        ops.removeIfExists(Address.subsystem("web").and("connector", TEST_CONNECTOR_NAME));
        admin.reloadIfRequired();
        client.close();
    }

    @Test
    public void addSimpleConnector_commandSucceeds() throws CommandFailedException, IOException {
        client.apply(new AddConnector.Builder(TEST_CONNECTOR_NAME)
                .protocol("HTTP/1.1")
                .scheme("http")
                .socketBinding("http")
                .enabled(false)
                .build());

        ModelNodeResult result = ops.readAttribute(Address.subsystem("web").and("connector", TEST_CONNECTOR_NAME),
                "scheme");
        result.assertSuccess();
    }

    @Test
    public void addConnectorWithAllAttributes_commandSucceeds() throws CommandFailedException, IOException {
        client.apply(new AddConnector.Builder(TEST_CONNECTOR_NAME)
                .protocol("HTTP/1.1")
                .scheme("http")
                .socketBinding("http")
                .enabled(false)
                .enableLookups(false)
                .maxConnections(10)
                .maxPostSize(20)
                .maxSavePostSize(20)
                .proxyBinding("test-binding")
                .proxyName("test-proxy")
                .proxyPort(7000)
                .redirectBinding("https")
                .redirectPort(8443)
                .secure(false)
                .virtualServer("default-host")
                .executor("test-executor")
                .build());

        ModelNodeResult result = ops.readAttribute(Address.subsystem("web").and("connector", TEST_CONNECTOR_NAME),
                "scheme");
        result.assertSuccess();
    }
}
