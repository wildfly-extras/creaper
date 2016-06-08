package org.wildfly.extras.creaper.core.online.operations.admin;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.auth.PropertiesFileAuth;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;
import org.wildfly.extras.creaper.core.online.ManagementProtocol;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.test.SlowTests;

import java.io.File;
import java.io.IOException;

@Category(SlowTests.class)
@RunWith(Arquillian.class)
public class Reload500TimesHttpTest extends Reload500TimesTest {
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpass";

    private static OfflineManagementClient offlineClient;

    @BeforeClass
    public static void addUser() throws IOException, CommandFailedException {
        offlineClient = ManagementClient.offline(OfflineOptions.standalone()
                .rootDirectory(new File("target/jboss-as"))
                .configurationFile("standalone.xml")
                .build()
        );

        offlineClient.apply(PropertiesFileAuth.mgmtUsers().defineUser(USERNAME, PASSWORD));
    }

    @AfterClass
    public static void removeUser() throws CommandFailedException {
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
}
