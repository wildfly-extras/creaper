package org.wildfly.extras.creaper.commands.security.realms;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class AddPropertiesAuthorizationOnlineTest {

    private OnlineManagementClient client;
    private Operations ops;
    private Administration administration;

    private static final String TEST_SECURITY_REALM_NAME = "creaperSecRealm";
    private static final Address TEST_SECURITY_REALM_ADDRESS
            = Address.coreService("management").and("security-realm", TEST_SECURITY_REALM_NAME);
    private static final Address TEST_SECURITY_REALM_PROPERTIES_AUTHZ_ADDRESS
            = TEST_SECURITY_REALM_ADDRESS.and("authorization", "properties");

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
        AddPropertiesAuthorization addPropertiesAuthorization
                = new AddPropertiesAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .path("users.properties")
                .build();

        assertFalse("The properties authz in security realm should not exist",
                ops.exists(TEST_SECURITY_REALM_PROPERTIES_AUTHZ_ADDRESS));
        client.apply(addPropertiesAuthorization);
        assertTrue("The properties authz in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_PROPERTIES_AUTHZ_ADDRESS));

    }

    @Test(expected = CommandFailedException.class)
    public void existing() throws Exception {
        AddPropertiesAuthorization addPropertiesAuthorization
                = new AddPropertiesAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .path("users.properties")
                .build();
        AddPropertiesAuthorization addPropertiesAuthorization2
                = new AddPropertiesAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .path("users2.properties")
                .build();

        client.apply(addPropertiesAuthorization);
        assertTrue("The properties authz in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_PROPERTIES_AUTHZ_ADDRESS));

        client.apply(addPropertiesAuthorization2);
        fail("Properties authorization is already configured in security realm, exception should be thrown");
    }

    @Test
    public void overrideExisting() throws Exception {
        AddPropertiesAuthorization addPropertiesAuthorization
                = new AddPropertiesAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .path("users.properties")
                .build();
        AddPropertiesAuthorization addPropertiesAuthorization2
                = new AddPropertiesAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .path("users2.properties")
                .replaceExisting()
                .build();

        client.apply(addPropertiesAuthorization);
        assertTrue("The properties authz in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_PROPERTIES_AUTHZ_ADDRESS));
        checkAttribute("path", "users.properties");

        client.apply(addPropertiesAuthorization2);
        assertTrue("The properties authz in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_PROPERTIES_AUTHZ_ADDRESS));
        checkAttribute("path", "users2.properties");
    }

    @Test
    public void addFull() throws Exception {
        AddPropertiesAuthorization addPropertiesAuthorization
                = new AddPropertiesAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .path("users.properties")
                .relativeTo("jboss.server.config.dir")
                .build();
        client.apply(addPropertiesAuthorization);

        assertTrue("The properties authz in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_PROPERTIES_AUTHZ_ADDRESS));

        checkAttribute("path", "users.properties");
        checkAttribute("relative-to", "jboss.server.config.dir");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_NullSecurityRealm() throws Exception {
        new AddPropertiesAuthorization.Builder(null)
                .path("users.properties")
                .build();
        fail("Creating command with null security realm should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_emptySecurityRealm() throws Exception {
        new AddPropertiesAuthorization.Builder("")
                .path("users.properties")
                .build();
        fail("Creating command with empty security realm should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_NullName() throws Exception {
        new AddPropertiesAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .path(null)
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_emptyName() throws Exception {
        new AddPropertiesAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .path("")
                .build();
        fail("Creating command with empty name should throw exception");
    }

    private void checkAttribute(String attribute, String expectedValue) throws IOException {
        ModelNodeResult readAttribute = ops.readAttribute(TEST_SECURITY_REALM_PROPERTIES_AUTHZ_ADDRESS, attribute);
        readAttribute.assertSuccess("Read operation for " + attribute + " failed");
        assertEquals("Read operation for " + attribute + " return wrong value", expectedValue,
                readAttribute.stringValue());
    }

}
