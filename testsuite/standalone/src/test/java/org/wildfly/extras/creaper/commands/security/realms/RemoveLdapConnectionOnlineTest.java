package org.wildfly.extras.creaper.commands.security.realms;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.CliException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class RemoveLdapConnectionOnlineTest {

    private OnlineManagementClient client;
    private Operations ops;
    private Administration administration;

    private static final String TEST_LDAP_CONNECTION_NAME = "creaperLdapConnection";
    private static final Address TEST_LDAP_CONNECTION_ADDRESS
            = Address.coreService("management").and("ldap-connection", TEST_LDAP_CONNECTION_NAME);

    @BeforeClass
    public static void checkServerVersionIsSupported() throws Exception {
        // check version is supported
        ServerVersion serverVersion
                = ManagementClient.online(OnlineOptions.standalone().localDefault().build()).version();
        Assume.assumeFalse("Legacy security was removed in WildFly 25.",
                serverVersion.greaterThanOrEqualTo(ServerVersion.VERSION_18_0_0));
    }

    @Before
    public void connect() throws IOException {
        client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
        ops = new Operations(client);
        administration = new Administration(client);
    }

    @After
    public void cleanup() throws IOException, CliException, OperationException, TimeoutException, InterruptedException {
        try {
            ops.removeIfExists(TEST_LDAP_CONNECTION_ADDRESS);
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void removeLdapConnection() throws Exception {
        AddLdapConnection addLdapConnection = new AddLdapConnection.Builder(TEST_LDAP_CONNECTION_NAME)
                .url("ldap://localhost:389")
                .build();
        client.apply(addLdapConnection);

        assertTrue("Ldap connection should be created", ops.exists(TEST_LDAP_CONNECTION_ADDRESS));

        client.apply(new RemoveLdapConnection(TEST_LDAP_CONNECTION_NAME));

        assertFalse("Ldap connection should be removed", ops.exists(TEST_LDAP_CONNECTION_ADDRESS));
    }

    @Test(expected = CommandFailedException.class)
    public void removeNonExistingLdapConnection() throws Exception {
        client.apply(new RemoveLdapConnection(TEST_LDAP_CONNECTION_NAME));
        fail("Ldap connection creaperLdapConnection does not exist in configuration, exception should be thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeNullNameLdapConnection() throws Exception {
        client.apply(new RemoveLdapConnection(null));
        fail("Creating command with null ldap connection name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeEmptyNameLdapConnection() throws Exception {
        client.apply(new RemoveLdapConnection(""));
        fail("Creating command with empty ldap connection name should throw exception");
    }
}
