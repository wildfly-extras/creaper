package org.wildfly.extras.creaper.commands.security.realms;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Assert;
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class AddSecurityRealmOnlineTest {

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
            ops.removeIfExists(TEST_SECURITY_REALM_ADDRESS);
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void addSimple() throws Exception {
        AddSecurityRealm addSecurityRealm = new AddSecurityRealm.Builder(TEST_SECURITY_REALM_NAME).build();

        assertFalse("The security realm should not exist", ops.exists(TEST_SECURITY_REALM_ADDRESS));
        client.apply(addSecurityRealm);
        assertTrue("The security realm should be created", ops.exists(TEST_SECURITY_REALM_ADDRESS));
    }

    @Test
    public void addFull() throws Exception {
        AddSecurityRealm addSecurityRealm = new AddSecurityRealm.Builder(TEST_SECURITY_REALM_NAME)
                .mapGroupsToRoles(true)
                .build();
        client.apply(addSecurityRealm);

        assertTrue("The security realm should be created", ops.exists(TEST_SECURITY_REALM_ADDRESS));

        ModelNodeResult readAttribute = ops.readAttribute(TEST_SECURITY_REALM_ADDRESS, "map-groups-to-roles");
        readAttribute.assertSuccess("Read operation for map-groups-to-roles failed");
        Assert.assertEquals("Read operation for map-groups-to-roles return wrong value", "true",
                readAttribute.stringValue());
    }

    @Test(expected = CommandFailedException.class)
    public void existing() throws Exception {
        AddSecurityRealm addSecurityRealm = new AddSecurityRealm.Builder(TEST_SECURITY_REALM_NAME)
                .build();
        AddSecurityRealm addSecurityRealm2 = new AddSecurityRealm.Builder(TEST_SECURITY_REALM_NAME)
                .mapGroupsToRoles(true)
                .build();

        client.apply(addSecurityRealm);
        client.apply(addSecurityRealm2);

        fail("Security realm creaperSecRealm already exists in configuration, exception should be thrown");
    }

    @Test
    public void overrideExisting() throws Exception {
        AddSecurityRealm addSecurityRealm = new AddSecurityRealm.Builder(TEST_SECURITY_REALM_NAME)
                .build();
        AddSecurityRealm addSecurityRealm2 = new AddSecurityRealm.Builder(TEST_SECURITY_REALM_NAME)
                .mapGroupsToRoles(true)
                .replaceExisting()
                .build();

        client.apply(addSecurityRealm);
        assertTrue("The security realm should be created", ops.exists(TEST_SECURITY_REALM_ADDRESS));
        client.apply(addSecurityRealm2);
        assertTrue("The security realm should be created", ops.exists(TEST_SECURITY_REALM_ADDRESS));

        ModelNodeResult readAttribute = ops.readAttribute(TEST_SECURITY_REALM_ADDRESS, "map-groups-to-roles");
        readAttribute.assertSuccess("Read operation for map-groups-to-roles failed");
        Assert.assertEquals("Read operation for map-groups-to-roles return wrong value", "true",
                readAttribute.stringValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSecurityRealm_nullName() throws Exception {
        new AddSecurityRealm.Builder(null).build();
        fail("Creating command with null security realm name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSecurityRealm_emptyName() throws Exception {
        new AddSecurityRealm.Builder("").build();
        fail("Creating command with empty security realm name should throw exception");
    }
}
