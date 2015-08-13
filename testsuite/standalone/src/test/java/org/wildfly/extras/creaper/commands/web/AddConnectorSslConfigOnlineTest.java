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
import org.wildfly.extras.creaper.test.AS7Tests;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.io.IOException;

@Category(AS7Tests.class)
@RunWith(Arquillian.class)
public class AddConnectorSslConfigOnlineTest {
    private static final String TEST_CONNECTOR_NAME = "test-http-ssl";

    private OnlineManagementClient client;
    private Operations ops;

    @Before
    public void connect() throws IOException, CommandFailedException, OperationException {
        client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
        ops = new Operations(client);

        ops.removeIfExists(Address.subsystem("web").and("connector", TEST_CONNECTOR_NAME));

        client.apply(new AddConnector.Builder(TEST_CONNECTOR_NAME)
                .protocol("HTTP/1.1")
                .scheme("https")
                .socketBinding("https")
                .enabled(false)
                .build());
    }

    @After
    public void close() throws IOException, CliException, OperationException {
        ops.removeIfExists(Address.subsystem("web").and("connector", TEST_CONNECTOR_NAME));
        client.close();
    }

    @Test
    public void addConnectorWithSslConfig_commandSucceeds() throws CommandFailedException, IOException {
        client.apply(new AddConnectorSslConfig.Builder(TEST_CONNECTOR_NAME)
                .caCertificateFile("/path/to/ca-certificate-file")
                .caCertificatePassword("password")
                .caRevocationUrl("file:///revocationURL")
                .certificateFile("/path/to/certificate-file")
                .certificateKeyFile("/path/to/certificate-key-file")
                .cipherSuite("cipherSuite")
                .keyAlias("keyAlias")
                .keystoreType("JKS")
                .password("password")
                .protocol("protocol")
                .sessionCacheSize(100)
                .sessionTimeout(30)
                .sslProtocol("TLS")
                .truststoreType("JKS")
                .verifyClient("false")
                .verifyDepth(1)
                .build());

        ModelNodeResult result = ops.readResource(
                Address.subsystem("web").and("connector", TEST_CONNECTOR_NAME).and("configuration", "ssl"));
        result.assertSuccess();
    }
}
