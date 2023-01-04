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
public class AddPropertiesAuthenticationOnlineTest {

    private OnlineManagementClient client;
    private Operations ops;
    private Administration administration;

    private static final String TEST_SECURITY_REALM_NAME = "creaperSecRealm";
    private static final Address TEST_SECURITY_REALM_ADDRESS
            = Address.coreService("management").and("security-realm", TEST_SECURITY_REALM_NAME);
    private static final Address TEST_SECURITY_REALM_PROPERTIES_AUTHN_ADDRESS
            = TEST_SECURITY_REALM_ADDRESS.and("authentication", "properties");

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
        AddPropertiesAuthentication addPropertiesAuthentication
                = new AddPropertiesAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .path("users.properties")
                .build();

        assertFalse("The properties authn in security realm should not exist",
                ops.exists(TEST_SECURITY_REALM_PROPERTIES_AUTHN_ADDRESS));
        client.apply(addPropertiesAuthentication);
        assertTrue("The properties authn in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_PROPERTIES_AUTHN_ADDRESS));

    }

    @Test(expected = CommandFailedException.class)
    public void existing() throws Exception {
        AddPropertiesAuthentication addPropertiesAuthentication
                = new AddPropertiesAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .path("users.properties")
                .build();
        AddPropertiesAuthentication addPropertiesAuthentication2
                = new AddPropertiesAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .path("users2.properties")
                .build();

        client.apply(addPropertiesAuthentication);
        assertTrue("The properties authn in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_PROPERTIES_AUTHN_ADDRESS));

        client.apply(addPropertiesAuthentication2);
        fail("Properties authentication is already configured in security realm, exception should be thrown");
    }

    @Test
    public void overrideExisting() throws Exception {
        AddPropertiesAuthentication addPropertiesAuthentication
                = new AddPropertiesAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .path("users.properties")
                .build();
        AddPropertiesAuthentication addPropertiesAuthentication2
                = new AddPropertiesAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .path("users2.properties")
                .replaceExisting()
                .build();

        client.apply(addPropertiesAuthentication);
        assertTrue("The properties authn in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_PROPERTIES_AUTHN_ADDRESS));
        checkAttribute("path", "users.properties");

        client.apply(addPropertiesAuthentication2);
        assertTrue("The properties authn in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_PROPERTIES_AUTHN_ADDRESS));
        checkAttribute("path", "users2.properties");
    }

    @Test
    public void addFull() throws Exception {
        AddPropertiesAuthentication addPropertiesAuthentication
                = new AddPropertiesAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .path("users.properties")
                .relativeTo("jboss.server.config.dir")
                .plainText(true)
                .build();
        client.apply(addPropertiesAuthentication);

        assertTrue("The properties authn in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_PROPERTIES_AUTHN_ADDRESS));

        checkAttribute("path", "users.properties");
        checkAttribute("relative-to", "jboss.server.config.dir");
        checkAttribute("plain-text", "true");

    }

    @Test(expected = IllegalArgumentException.class)
    public void add_NullSecurityRealm() throws Exception {
        new AddPropertiesAuthentication.Builder(null)
                .path("users.properties")
                .build();
        fail("Creating command with null security realm should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_emptySecurityRealm() throws Exception {
        new AddPropertiesAuthentication.Builder("")
                .path("users.properties")
                .build();
        fail("Creating command with empty security realm should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_NullName() throws Exception {
        new AddPropertiesAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .path(null)
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_emptyName() throws Exception {
        new AddPropertiesAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .path("")
                .build();
        fail("Creating command with empty name should throw exception");
    }

    private void checkAttribute(String attribute, String expectedValue) throws IOException {
        ModelNodeResult readAttribute = ops.readAttribute(TEST_SECURITY_REALM_PROPERTIES_AUTHN_ADDRESS, attribute);
        readAttribute.assertSuccess("Read operation for " + attribute + " failed");
        assertEquals("Read operation for " + attribute + " return wrong value", expectedValue,
                readAttribute.stringValue());
    }
}
