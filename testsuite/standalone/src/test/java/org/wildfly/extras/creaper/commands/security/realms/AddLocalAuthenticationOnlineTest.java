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
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class AddLocalAuthenticationOnlineTest {

    private OnlineManagementClient client;
    private Operations ops;
    private Administration administration;

    private static final String TEST_SECURITY_REALM_NAME = "creaperSecRealm";
    private static final Address TEST_SECURITY_REALM_ADDRESS
            = Address.coreService("management").and("security-realm", TEST_SECURITY_REALM_NAME);
    private static final Address TEST_SECURITY_REALM_LOCAL_AUTHN_ADDRESS
            = TEST_SECURITY_REALM_ADDRESS.and("authentication", "local");

    @BeforeClass
    public static void checkServerVersionIsSupported() throws Exception {
        // check version is supported
        ServerVersion serverVersion
                = ManagementClient.online(OnlineOptions.standalone().localDefault().build()).version();
        Assume.assumeFalse("Legacy security was removed in WildFly 25.",
                serverVersion.greaterThanOrEqualTo(ServerVersion.VERSION_18_0_0));
    }

    @Before
    public void connect() throws Exception {
        client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
        ops = new Operations(client);
        administration = new Administration(client);

        AddSecurityRealm addSecurityRealm = new AddSecurityRealm.Builder(TEST_SECURITY_REALM_NAME).build();
        client.apply(addSecurityRealm);
        assertTrue("The security realm should be created", ops.exists(TEST_SECURITY_REALM_ADDRESS));
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
    public void addSimple() throws Exception {
        AddLocalAuthentication addLocalAuthentication
                = new AddLocalAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .build();

        assertFalse("The local authn in security realm should not exist",
                ops.exists(TEST_SECURITY_REALM_LOCAL_AUTHN_ADDRESS));
        client.apply(addLocalAuthentication);
        assertTrue("The local authn in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LOCAL_AUTHN_ADDRESS));

    }

    @Test(expected = CommandFailedException.class)
    public void existing() throws Exception {
        AddLocalAuthentication addLocalAuthentication
                = new AddLocalAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .defaultUser("userA")
                .build();
        AddLocalAuthentication addLocalAuthentication2
                = new AddLocalAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .defaultUser("userB")
                .build();

        client.apply(addLocalAuthentication);
        assertTrue("The local authn in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LOCAL_AUTHN_ADDRESS));

        client.apply(addLocalAuthentication2);
        fail("Local authentication is already configured in security realm, exception should be thrown");
    }

    @Test
    public void overrideExisting() throws Exception {
        AddLocalAuthentication addLocalAuthentication
                = new AddLocalAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .defaultUser("userA")
                .build();
        AddLocalAuthentication addLocalAuthentication2
                = new AddLocalAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .defaultUser("userB")
                .replaceExisting()
                .build();

        client.apply(addLocalAuthentication);
        assertTrue("The local authn in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LOCAL_AUTHN_ADDRESS));
        checkAttribute("default-user", "userA");

        client.apply(addLocalAuthentication2);
        assertTrue("The local authn in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LOCAL_AUTHN_ADDRESS));
        checkAttribute("default-user", "userB");
    }

    @Test
    public void addFull() throws Exception {
        AddLocalAuthentication addLocalAuthentication
                = new AddLocalAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .allowedUsers("AllUsers")
                .defaultUser("UserA")
                .skipGroupLoading(true)
                .build();
        client.apply(addLocalAuthentication);

        assertTrue("The local authn in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LOCAL_AUTHN_ADDRESS));

        checkAttribute("allowed-users", "AllUsers");
        checkAttribute("default-user", "UserA");
        checkAttribute("skip-group-loading", "true");

    }

    @Test(expected = IllegalArgumentException.class)
    public void add_NullSecurityRealm() throws Exception {
        new AddLocalAuthentication.Builder(null)
                .build();
        fail("Creating command with null security realm should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_emptySecurityRealm() throws Exception {
        new AddLocalAuthentication.Builder("")
                .build();
        fail("Creating command with empty security realm should throw exception");
    }

    private void checkAttribute(String attribute, String expectedValue) throws IOException {
        ModelNodeResult readAttribute = ops.readAttribute(TEST_SECURITY_REALM_LOCAL_AUTHN_ADDRESS, attribute);
        readAttribute.assertSuccess("Read operation for " + attribute + " failed");
        assertEquals("Read operation for " + attribute + " return wrong value", expectedValue,
                readAttribute.stringValue());
    }
}
