package org.wildfly.extras.creaper.core.online;

import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.wildfly.extras.creaper.commands.auth.PropertiesFileAuth;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;
import org.wildfly.extras.creaper.test.ManualTests;

import java.io.File;
import java.io.IOException;

public class HttpsOnlineManagementClientTest extends SslOnlineManagementClientTest {
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpass";
    private static OfflineManagementClient offlineClient;

    @BeforeClass
    public static void addUser() throws IOException, CommandFailedException {
        offlineClient = ManagementClient.offline(OfflineOptions.standalone()
                .rootDirectory(new File("target/jboss-as"))
                .configurationFile("standalone.xml")
                .build());
        offlineClient.apply(PropertiesFileAuth.mgmtUsers().defineUser(USERNAME, PASSWORD));
    }

    @AfterClass
    public static void removeUser() throws CommandFailedException {
        offlineClient.apply(PropertiesFileAuth.mgmtUsers().undefineUser(USERNAME));
    }

    @Before
    @Override
    public void connect() throws IOException {
        if (this.controller.isStarted(ManualTests.ARQUILLIAN_CONTAINER_MGMT_PROTOCOL_REMOTE)) {
            client = ManagementClient.online(OnlineOptions
                    .standalone()
                    .localDefault()
                    .ssl(sslOptions)
                    .protocol(ManagementProtocol.HTTPS)
                    .build());
        }
    }

    @Test
    @Override
    public void executeThroughCli_reload() throws IOException {
        // WildFly 10.0.0 seems to have a bug with :reload operation over HTTP, other versions are fine
        Assume.assumeTrue(client.version() != ServerVersion.VERSION_4_0_0);
        super.executeThroughCli_reload();
    }

    @Test(expected = IllegalStateException.class)
    @Override
    public void clientConfiguredForDomain() throws IOException {
        ManagementClient.online(OnlineOptions.domain().build()
                .localDefault()
                .protocol(ManagementProtocol.HTTPS)
                .ssl(sslOptions)
                .auth(USERNAME, PASSWORD)
                .connectionTimeout(5000)
                .build()
        );
    }
}
