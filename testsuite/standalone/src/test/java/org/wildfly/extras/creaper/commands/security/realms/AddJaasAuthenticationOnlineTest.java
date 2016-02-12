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
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.ServerVersion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class AddJaasAuthenticationOnlineTest {

    private OnlineManagementClient client;
    private Operations ops;
    private Administration administration;

    private static final String TEST_SECURITY_REALM_NAME = "creaperSecRealm";
    private static final Address TEST_SECURITY_REALM_ADDRESS
            = Address.coreService("management").and("security-realm", TEST_SECURITY_REALM_NAME);
    private static final Address TEST_SECURITY_REALM_JAAS_AUTHN_ADDRESS
            = TEST_SECURITY_REALM_ADDRESS.and("authentication", "jaas");

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
        AddJaasAuthentication addJaasAuthentication
                = new AddJaasAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .name("other")
                .build();

        assertFalse("The jaas authn in security realm should not exist",
                ops.exists(TEST_SECURITY_REALM_JAAS_AUTHN_ADDRESS));
        client.apply(addJaasAuthentication);
        assertTrue("The jaas authn in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_JAAS_AUTHN_ADDRESS));

    }

    @Test(expected = CommandFailedException.class)
    public void existing() throws Exception {
        AddJaasAuthentication addJaasAuthentication
                = new AddJaasAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .name("other")
                .build();
        AddJaasAuthentication addJaasAuthentication2
                = new AddJaasAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .name("jboss-web-policy")
                .build();

        client.apply(addJaasAuthentication);
        assertTrue("The jaas authn in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_JAAS_AUTHN_ADDRESS));

        client.apply(addJaasAuthentication2);
        fail("Jaas authentication is already configured in security realm, exception should be thrown");
    }

    @Test
    public void overrideExisting() throws Exception {
        AddJaasAuthentication addJaasAuthentication
                = new AddJaasAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .name("other")
                .build();
        AddJaasAuthentication addJaasAuthentication2
                = new AddJaasAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .name("jboss-web-policy")
                .replaceExisting()
                .build();

        client.apply(addJaasAuthentication);
        assertTrue("The jaas authn in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_JAAS_AUTHN_ADDRESS));
        checkAttribute("name", "other");

        client.apply(addJaasAuthentication2);
        assertTrue("The jaas authn in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_JAAS_AUTHN_ADDRESS));
        checkAttribute("name", "jboss-web-policy");
    }

    @Test
    public void addFull() throws Exception {
        AddJaasAuthentication.Builder addJaasAuthenticationBuilder
                = new AddJaasAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .name("other");

        if (client.version().greaterThanOrEqualTo(ServerVersion.VERSION_1_7_0)
                && !client.version().inRange(ServerVersion.VERSION_2_0_0, ServerVersion.VERSION_2_2_0)) {
            addJaasAuthenticationBuilder.assignGroups(true);
        }

        AddJaasAuthentication addJaasAuthentication = addJaasAuthenticationBuilder.build();

        client.apply(addJaasAuthentication);

        assertTrue("The jaas authn in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_JAAS_AUTHN_ADDRESS));

        checkAttribute("name", "other");
        if (client.version().greaterThanOrEqualTo(ServerVersion.VERSION_1_7_0)
                && !client.version().inRange(ServerVersion.VERSION_2_0_0, ServerVersion.VERSION_2_2_0)) {
            checkAttribute("assign-groups", "true");
        }

    }

    @Test(expected = IllegalArgumentException.class)
    public void add_NullSecurityRealm() throws Exception {
        new AddJaasAuthentication.Builder(null)
                .name("other")
                .build();
        fail("Creating command with null security realm should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_emptySecurityRealm() throws Exception {
        new AddJaasAuthentication.Builder("")
                .name("other")
                .build();
        fail("Creating command with empty security realm should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_NullName() throws Exception {
        new AddJaasAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .name(null)
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_emptyName() throws Exception {
        new AddJaasAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .name("")
                .build();
        fail("Creating command with empty name should throw exception");
    }

    private void checkAttribute(String attribute, String expectedValue) throws IOException {
        ModelNodeResult readAttribute = ops.readAttribute(TEST_SECURITY_REALM_JAAS_AUTHN_ADDRESS, attribute);
        readAttribute.assertSuccess("Read operation for " + attribute + " failed");
        assertEquals("Read operation for " + attribute + " return wrong value", expectedValue,
                readAttribute.stringValue());
    }

}
