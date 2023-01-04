package org.wildfly.extras.creaper.core.online;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.wildfly.extras.creaper.commands.auth.PropertiesFileAuth;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assume.assumeTrue;

public class HttpOnlineManagementClientTest extends OnlineManagementClientTest {
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpass";
    private static OfflineManagementClient offlineClient;
    private static OnlineManagementClient nativeOnlineClient;

    @BeforeClass
    public static void addUser() throws IOException, CommandFailedException, CliException, InterruptedException,
            TimeoutException {
        // first add user
        offlineClient = ManagementClient.offline(OfflineOptions.standalone()
                .rootDirectory(new File("target/jboss-as"))
                .configurationFile("standalone.xml")
                .build()
        );

        offlineClient.apply(PropertiesFileAuth.mgmtUsers().defineUser(USERNAME, PASSWORD));

        if (offlineClient.version().greaterThanOrEqualTo(ServerVersion.VERSION_18_0_0)) {
            // then enable http authentication, so we can use http protocol
            nativeOnlineClient = ManagementClient.online(OnlineOptions.standalone()
                    .localDefault()
                    .build());
            nativeOnlineClient.executeCli("security enable-http-auth-management --no-reload");
            new Administration(nativeOnlineClient).reload();
        }
    }

    @AfterClass
    public static void removeUser() throws CommandFailedException, CliException, IOException, InterruptedException,
            TimeoutException {
        if (offlineClient.version().greaterThanOrEqualTo(ServerVersion.VERSION_18_0_0)) {
            nativeOnlineClient.executeCli("security disable-http-auth-management --no-reload");
            new Administration(nativeOnlineClient).reload();
        }

        offlineClient.apply(PropertiesFileAuth.mgmtUsers().undefineUser(USERNAME));
    }

    @Before
    @Override
    public void connect() throws IOException {
        client = ManagementClient.online(OnlineOptions.standalone()
                .localDefault()
                .protocol(ManagementProtocol.HTTP)
                .auth(USERNAME, PASSWORD)
                .connectionTimeout(5000)
                .build()
        );
    }

    @Test
    @Override
    public void executeThroughCli_reload() throws IOException {
        // WildFly 10.0.0 seems to have a bug with :reload operation over HTTP, other versions are fine
        assumeTrue(client.version() != ServerVersion.VERSION_4_0_0);
        super.executeThroughCli_reload();
    }

    @Test(expected = IllegalStateException.class)
    @Override
    public void clientConfiguredForDomain() throws IOException {
        ManagementClient.online(OnlineOptions.domain().build()
                .localDefault()
                .protocol(ManagementProtocol.HTTP)
                .auth(USERNAME, PASSWORD)
                .connectionTimeout(5000)
                .build()
        );
    }
}
