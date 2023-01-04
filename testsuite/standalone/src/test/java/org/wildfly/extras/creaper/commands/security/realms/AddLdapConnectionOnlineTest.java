package org.wildfly.extras.creaper.commands.security.realms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
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
public class AddLdapConnectionOnlineTest {

    private OnlineManagementClient client;
    private Operations ops;
    private Administration administration;

    private static final String TEST_LDAP_CONNECTION = "creaperLdapConnection";
    private static final Address TEST_LDAP_CONNECTION_ADDRESS
            = Address.coreService("management").and("ldap-connection", TEST_LDAP_CONNECTION);

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
    public void addSimple() throws Exception {
        AddLdapConnection addLdapConnection = new AddLdapConnection.Builder(TEST_LDAP_CONNECTION)
                .url("ldap://localhost:389")
                .build();

        assertFalse("Ldap outbound connection should not exist", ops.exists(TEST_LDAP_CONNECTION_ADDRESS));
        client.apply(addLdapConnection);
        assertTrue("Ldap outbound connection should be created", ops.exists(TEST_LDAP_CONNECTION_ADDRESS));
    }

    @Test(expected = CommandFailedException.class)
    public void existing() throws Exception {
        AddLdapConnection addLdapConnection = new AddLdapConnection.Builder(TEST_LDAP_CONNECTION)
                .url("ldap://localhost:389")
                .build();
        AddLdapConnection addLdapConnection2 = new AddLdapConnection.Builder(TEST_LDAP_CONNECTION)
                .url("ldap://localhost:10389")
                .build();

        client.apply(addLdapConnection);
        client.apply(addLdapConnection2);

        fail("Ldap outbound connection creaperLdapConnection already exists in configuration, exception should be thrown");
    }

    @Test
    public void overrideExisting() throws Exception {
        AddLdapConnection addLdapConnection = new AddLdapConnection.Builder(TEST_LDAP_CONNECTION)
                .url("ldap://localhost:389")
                .build();
        AddLdapConnection addLdapConnection2 = new AddLdapConnection.Builder(TEST_LDAP_CONNECTION)
                .url("ldap://localhost:10389")
                .replaceExisting()
                .build();

        client.apply(addLdapConnection);
        assertTrue("Ldap outbound connection should be created", ops.exists(TEST_LDAP_CONNECTION_ADDRESS));
        checkAttribute(TEST_LDAP_CONNECTION_ADDRESS, "url", "ldap://localhost:389");

        client.apply(addLdapConnection2);
        assertTrue("Ldap outbound connection should be created", ops.exists(TEST_LDAP_CONNECTION_ADDRESS));
        checkAttribute(TEST_LDAP_CONNECTION_ADDRESS, "url", "ldap://localhost:10389");
    }

    @Test
    public void addFull() throws Exception {
        AddLdapConnection addLdapConnection = new AddLdapConnection.Builder(TEST_LDAP_CONNECTION)
                .url("ldap://localhost:389")
                .searchDn("uid=admin,ou=system")
                .searchCredential("secret")
                .initialContextFactory("ContextFactory")
                .referrals("THROW")
                .addHandlesReferralsFor("localhost")
                .addHandlesReferralsFor("127.0.0.1")
                .addProperty("propertyA", "valueA")
                .addProperty("propertyB", "valueB")
                .build();

        client.apply(addLdapConnection);
        assertTrue("Ldap outbound connection should be created", ops.exists(TEST_LDAP_CONNECTION_ADDRESS));
        checkAttribute(TEST_LDAP_CONNECTION_ADDRESS, "url", "ldap://localhost:389");
        checkAttribute(TEST_LDAP_CONNECTION_ADDRESS, "search-dn", "uid=admin,ou=system");
        checkAttribute(TEST_LDAP_CONNECTION_ADDRESS, "search-credential", "secret");
        checkAttribute(TEST_LDAP_CONNECTION_ADDRESS, "initial-context-factory", "ContextFactory");
        checkAttribute(TEST_LDAP_CONNECTION_ADDRESS, "referrals", "THROW");
        checkHandlesReferralsForAttribute(TEST_LDAP_CONNECTION_ADDRESS, "localhost", "127.0.0.1");
        checkAttribute(TEST_LDAP_CONNECTION_ADDRESS.and("property", "propertyA"), "value", "valueA");
        checkAttribute(TEST_LDAP_CONNECTION_ADDRESS.and("property", "propertyB"), "value", "valueB");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_nullConnectionName() throws Exception {
        new AddLdapConnection.Builder(null).build();
        fail("Creating command with null ldap connection name should throw exception");

    }

    @Test(expected = IllegalArgumentException.class)
    public void add_emptyConnectionName() throws Exception {
        new AddLdapConnection.Builder("").build();
        fail("Creating command with empty ldap connection name should throw exception");

    }

    @Test(expected = IllegalArgumentException.class)
    public void add_nullUrl() throws Exception {
        new AddLdapConnection.Builder(TEST_LDAP_CONNECTION)
                .url(null)
                .build();
        fail("Creating command with null url should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_emptyUrl() throws Exception {
        new AddLdapConnection.Builder(TEST_LDAP_CONNECTION)
                .url("")
                .build();
        fail("Creating command with empty url should throw exception");
    }

    private void checkAttribute(Address address, String attribute, String expectedValue) throws IOException {
        ModelNodeResult readAttribute = ops.readAttribute(address, attribute);
        readAttribute.assertSuccess("Read operation for " + attribute + " failed");
        assertEquals("Read operation for " + attribute + " return wrong value", expectedValue,
                readAttribute.stringValue());
    }

    private void checkHandlesReferralsForAttribute(Address address, String... expectedValues) throws IOException {
        ModelNodeResult readAttribute = ops.readAttribute(address, "handles-referrals-for");
        readAttribute.assertSuccess("Read operation for handles-referrals-for failed");
        ModelNode result = readAttribute.get("result");
        if (result == null) {
            fail("Read operation for handles-referrals-for has not defined result.");
        }
        List<ModelNode> attributes = result.asList();
        List<String> toCheck = new ArrayList<String>();
        for (ModelNode node : attributes) {
            toCheck.add(node.asString());
        }
        for (String expectedValue : expectedValues) {
            assertTrue("Expected value " + expectedValue + " was not added to handles-referrals-for attribute",
                    toCheck.contains(expectedValue));
        }
    }
}
