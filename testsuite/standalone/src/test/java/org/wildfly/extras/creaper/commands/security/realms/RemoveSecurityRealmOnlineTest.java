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
public class RemoveSecurityRealmOnlineTest {

    private OnlineManagementClient client;
    private Operations ops;
    private Administration administration;

    private static final String TEST_SECURITY_REALM_NAME = "creaperSecRealm";
    private static final Address TEST_SECURITY_REALM_ADDRESS
            = Address.coreService("management").and("security-realm", TEST_SECURITY_REALM_NAME);

    @BeforeClass
    public static void checkServerVersionIsSupported() throws Exception {
        // check version is supported
        ServerVersion serverVersion
                = ManagementClient.online(OnlineOptions.standalone().localDefault().build()).version();
        Assume.assumeFalse("Legacy security was removed in WildFly 15.",
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
            ops.removeIfExists(TEST_SECURITY_REALM_ADDRESS);
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void removeSecurityRealm() throws Exception {
        AddSecurityRealm addSecurityRealm = new AddSecurityRealm.Builder(TEST_SECURITY_REALM_NAME).build();
        client.apply(addSecurityRealm);

        assertTrue("The security realm should be created", ops.exists(TEST_SECURITY_REALM_ADDRESS));

        client.apply(new RemoveSecurityRealm(TEST_SECURITY_REALM_NAME));

        assertFalse("The security realm should be removed", ops.exists(TEST_SECURITY_REALM_ADDRESS));
    }

    @Test(expected = CommandFailedException.class)
    public void removeNonExistingSecurityRealm() throws Exception {
        client.apply(new RemoveSecurityRealm(TEST_SECURITY_REALM_NAME));
        fail("Security realm creaperSecRealm does not exist in configuration, exception should be thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeNullNameSecurityRealm() throws Exception {
        client.apply(new RemoveSecurityRealm(null));
        fail("Creating command with null security realm name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeEmptyNameSecurityRealm() throws Exception {
        client.apply(new RemoveSecurityRealm(""));
        fail("Creating command with empty security realm name should throw exception");
    }
}
