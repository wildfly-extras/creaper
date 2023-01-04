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
import org.wildfly.extras.creaper.core.online.CliException;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;
import org.wildfly.extras.creaper.core.ServerVersion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class AddLdapAuthenticationOnlineTest {

    private OnlineManagementClient client;
    private Operations ops;
    private Administration administration;

    private static final String TEST_SECURITY_REALM_NAME = "creaperSecRealm";
    private static final String TEST_LDAP_CONNECTION = "creaperLdapConnection";
    private static final Address TEST_SECURITY_REALM_ADDRESS
            = Address.coreService("management").and("security-realm", TEST_SECURITY_REALM_NAME);
    private static final Address TEST_SECURITY_REALM_LDAP_AUTHN_ADDRESS
            = TEST_SECURITY_REALM_ADDRESS.and("authentication", "ldap");
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

        AddSecurityRealm addSecurityRealm = new AddSecurityRealm.Builder(TEST_SECURITY_REALM_NAME).build();
        client.apply(addSecurityRealm);
        assertTrue("The security realm should be created", ops.exists(TEST_SECURITY_REALM_ADDRESS));

        AddLdapConnection addLdapConnection = new AddLdapConnection.Builder(TEST_LDAP_CONNECTION)
                .url("ldap://localhost:10389")
                .build();
        client.apply(addLdapConnection);
        assertTrue("Ldap outbound connection should be created", ops.exists(TEST_LDAP_CONNECTION_ADDRESS));
    }

    @After
    public void cleanup() throws IOException, CliException, OperationException, TimeoutException, InterruptedException {
        try {
            ops.removeIfExists(TEST_SECURITY_REALM_ADDRESS);
            ops.removeIfExists(TEST_LDAP_CONNECTION_ADDRESS);
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void addSimple_usernameAttribute() throws Exception {
        AddLdapAuthentication addSecurityRealmLdapAuthentication
                = new AddLdapAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .usernameAttribute("uid")
                .connection(TEST_LDAP_CONNECTION)
                .baseDn("ou=People,dc=jboss,dc=org")
                .build();

        assertFalse("The ldap authn in security realm should not exist",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHN_ADDRESS));
        client.apply(addSecurityRealmLdapAuthentication);
        assertTrue("The ldap authn in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHN_ADDRESS));
    }

    @Test
    public void addSimple_advancedFilter() throws Exception {
        AddLdapAuthentication addSecurityRealmLdapAuthentication
                = new AddLdapAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .advancedFilter("(uid={0})")
                .connection(TEST_LDAP_CONNECTION)
                .baseDn("ou=People,dc=jboss,dc=org")
                .build();

        assertFalse("The ldap authn in security realm should not exist",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHN_ADDRESS));
        client.apply(addSecurityRealmLdapAuthentication);
        assertTrue("The ldap authn in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHN_ADDRESS));
    }

    @Test(expected = CommandFailedException.class)
    public void existing() throws Exception {
        AddLdapAuthentication addSecurityRealmLdapAuthentication
                = new AddLdapAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .usernameAttribute("uid")
                .connection(TEST_LDAP_CONNECTION)
                .baseDn("ou=People,dc=jboss,dc=org")
                .build();
        AddLdapAuthentication addSecurityRealmLdapAuthentication2
                = new AddLdapAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .usernameAttribute("cn")
                .connection(TEST_LDAP_CONNECTION)
                .baseDn("ou=Person,dc=jboss,dc=org")
                .build();

        client.apply(addSecurityRealmLdapAuthentication);
        assertTrue("The ldap authn in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHN_ADDRESS));

        client.apply(addSecurityRealmLdapAuthentication2);
        fail("Ldap authentication is already configured in security realm, exception should be thrown");
    }

    @Test
    public void overrideExisting() throws Exception {
        AddLdapAuthentication addSecurityRealmLdapAuthentication
                = new AddLdapAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .usernameAttribute("uid")
                .connection(TEST_LDAP_CONNECTION)
                .baseDn("ou=People,dc=jboss,dc=org")
                .build();
        AddLdapAuthentication addSecurityRealmLdapAuthentication2
                = new AddLdapAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .usernameAttribute("cn")
                .connection(TEST_LDAP_CONNECTION)
                .baseDn("ou=Person,dc=jboss,dc=org")
                .replaceExisting()
                .build();

        client.apply(addSecurityRealmLdapAuthentication);
        assertTrue("The ldap authn in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHN_ADDRESS));
        checkAttribute("username-attribute", "uid");
        checkAttribute("connection", TEST_LDAP_CONNECTION);
        checkAttribute("base-dn", "ou=People,dc=jboss,dc=org");

        client.apply(addSecurityRealmLdapAuthentication2);
        assertTrue("The ldap authn in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHN_ADDRESS));
        checkAttribute("username-attribute", "cn");
        checkAttribute("connection", TEST_LDAP_CONNECTION);
        checkAttribute("base-dn", "ou=Person,dc=jboss,dc=org");
    }

    @Test
    public void addFull_usernameAttribute() throws Exception {
        AddLdapAuthentication.Builder addSecurityRealmLdapAuthenticationBuilder
                = new AddLdapAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .usernameAttribute("uid")
                .connection(TEST_LDAP_CONNECTION)
                .baseDn("ou=People,dc=jboss,dc=org")
                .allowEmptyPasswords(true)
                .recursive(true)
                .userDn("cn");

        if (client.version().greaterThanOrEqualTo(ServerVersion.VERSION_2_0_0)) {
            addSecurityRealmLdapAuthenticationBuilder.usernameLoad("description");
        }

        AddLdapAuthentication addSecurityRealmLdapAuthentication
                = addSecurityRealmLdapAuthenticationBuilder.build();

        client.apply(addSecurityRealmLdapAuthentication);

        assertTrue("The ldap authn in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHN_ADDRESS));
        checkAttribute("username-attribute", "uid");
        checkAttribute("connection", TEST_LDAP_CONNECTION);
        checkAttribute("base-dn", "ou=People,dc=jboss,dc=org");
        checkAttribute("allow-empty-passwords", "true");
        checkAttribute("recursive", "true");
        checkAttribute("user-dn", "cn");
        if (client.version().greaterThanOrEqualTo(ServerVersion.VERSION_2_0_0)) {
            checkAttribute("username-load", "description");
        }
    }

    @Test
    public void addFull_advancedFilter() throws Exception {

        AddLdapAuthentication.Builder addSecurityRealmLdapAuthenticationBuilder
                = new AddLdapAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .advancedFilter("(uid={0})")
                .connection(TEST_LDAP_CONNECTION)
                .baseDn("ou=People,dc=jboss,dc=org")
                .allowEmptyPasswords(false)
                .recursive(false)
                .userDn("uid");

        if (client.version().greaterThanOrEqualTo(ServerVersion.VERSION_2_0_0)) {
            addSecurityRealmLdapAuthenticationBuilder.usernameLoad("sn");
        }

        AddLdapAuthentication addSecurityRealmLdapAuthentication
                = addSecurityRealmLdapAuthenticationBuilder.build();

        client.apply(addSecurityRealmLdapAuthentication);

        assertTrue("The ldap authn in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHN_ADDRESS));
        checkAttribute("advanced-filter", "(uid={0})");
        checkAttribute("connection", TEST_LDAP_CONNECTION);
        checkAttribute("base-dn", "ou=People,dc=jboss,dc=org");
        checkAttribute("allow-empty-passwords", "false");
        checkAttribute("recursive", "false");
        checkAttribute("user-dn", "uid");
        if (client.version().greaterThanOrEqualTo(ServerVersion.VERSION_2_0_0)) {
            checkAttribute("username-load", "sn");
        }
    }

    @Test
    public void addWithCache_byAccessTime() throws Exception {
        AddLdapAuthentication addSecurityRealmLdapAuthentication
                = new AddLdapAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .usernameAttribute("uid")
                .connection(TEST_LDAP_CONNECTION)
                .baseDn("ou=People,dc=jboss,dc=org")
                .cache(new LdapCache.Builder()
                        .byAccessTime()
                        .cacheFailures(true)
                        .evictionTime(3)
                        .maxCacheSize(10)
                        .build())
                .build();

        client.apply(addSecurityRealmLdapAuthentication);

        assertTrue("The ldap authn in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHN_ADDRESS));

        Address cacheAddress = TEST_SECURITY_REALM_LDAP_AUTHN_ADDRESS.and("cache", "by-access-time");

        assertTrue("The ldap authn in security realm should be created", ops.exists(cacheAddress));
        checkAttribute(cacheAddress, "cache-failures", "true");
        checkAttribute(cacheAddress, "eviction-time", "3");
        checkAttribute(cacheAddress, "max-cache-size", "10");
    }

    @Test
    public void addWithCache_bySearchTime() throws Exception {
        AddLdapAuthentication addSecurityRealmLdapAuthentication
                = new AddLdapAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .usernameAttribute("uid")
                .connection(TEST_LDAP_CONNECTION)
                .baseDn("ou=People,dc=jboss,dc=org")
                .cache(new LdapCache.Builder()
                        .bySearchTime()
                        .cacheFailures(false)
                        .evictionTime(1)
                        .maxCacheSize(5)
                        .build())
                .build();

        client.apply(addSecurityRealmLdapAuthentication);

        assertTrue("The ldap authn in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHN_ADDRESS));

        Address cacheAddress = TEST_SECURITY_REALM_LDAP_AUTHN_ADDRESS.and("cache", "by-search-time");

        assertTrue("The ldap authn in security realm should be created", ops.exists(cacheAddress));
        checkAttribute(cacheAddress, "cache-failures", "false");
        checkAttribute(cacheAddress, "eviction-time", "1");
        checkAttribute(cacheAddress, "max-cache-size", "5");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_NullSecurityRealm() throws Exception {
        new AddLdapAuthentication.Builder(null)
                .advancedFilter("(uid={0})")
                .connection(TEST_LDAP_CONNECTION)
                .baseDn("ou=People,dc=jboss,dc=org")
                .build();
        fail("Creating command with null security realm should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_EmptySecurityRealm() throws Exception {
        new AddLdapAuthentication.Builder("")
                .advancedFilter("(uid={0})")
                .connection(TEST_LDAP_CONNECTION)
                .baseDn("ou=People,dc=jboss,dc=org")
                .build();
        fail("Creating command with empty security realm should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_NullConnection() throws Exception {
        new AddLdapAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .advancedFilter("(uid={0})")
                .connection(null)
                .baseDn("ou=People,dc=jboss,dc=org")
                .build();
        fail("Creating command with null connection should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_EmptyConnection() throws Exception {
        new AddLdapAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .advancedFilter("(uid={0})")
                .connection("")
                .baseDn("ou=People,dc=jboss,dc=org")
                .build();
        fail("Creating command with empty connection should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_NullBaseDn() throws Exception {
        new AddLdapAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .advancedFilter("(uid={0})")
                .connection(TEST_LDAP_CONNECTION)
                .baseDn(null)
                .build();
        fail("Creating command with null base-dn should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_EmptyBaseDn() throws Exception {
        new AddLdapAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .advancedFilter("(uid={0})")
                .connection(TEST_LDAP_CONNECTION)
                .baseDn("")
                .build();
        fail("Creating command with empty base-dn should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_BothUsernameAttributeAndAdvancedFilter() throws Exception {
        new AddLdapAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .advancedFilter("(uid={0})")
                .usernameAttribute("uid")
                .connection(TEST_LDAP_CONNECTION)
                .baseDn("ou=People,dc=jboss,dc=org")
                .build();
        fail("Creating command with both username-attribute and advanced-filter should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_NullUsernameAttribute_NullAdvancedFilter() throws Exception {
        new AddLdapAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .advancedFilter(null)
                .usernameAttribute(null)
                .connection(TEST_LDAP_CONNECTION)
                .baseDn("ou=People,dc=jboss,dc=org")
                .build();
        fail("Creating command without username-attribute and advanced-filter should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_EmptyUsernameAttribute_NullAdvancedFilter() throws Exception {
        new AddLdapAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .advancedFilter(null)
                .usernameAttribute("")
                .connection(TEST_LDAP_CONNECTION)
                .baseDn("ou=People,dc=jboss,dc=org")
                .build();
        fail("Creating command with empty username-attribute and null advanced-filter should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_NullUsernameAttribute_EmptyAdvancedFilter() throws Exception {
        new AddLdapAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .advancedFilter("")
                .usernameAttribute(null)
                .connection(TEST_LDAP_CONNECTION)
                .baseDn("ou=People,dc=jboss,dc=org")
                .build();
        fail("Creating command with null username-attribute and empty advanced-filter should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_EmptyUsernameAttribute_EmptyAdvancedFilter() throws Exception {
        new AddLdapAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .advancedFilter("")
                .usernameAttribute("")
                .connection(TEST_LDAP_CONNECTION)
                .baseDn("ou=People,dc=jboss,dc=org")
                .build();
        fail("Creating command with empty username-attribute and empty advanced-filter should throw exception");
    }

    private void checkAttribute(String attribute, String expectedValue) throws IOException {
        checkAttribute(TEST_SECURITY_REALM_LDAP_AUTHN_ADDRESS, attribute, expectedValue);
    }

    private void checkAttribute(Address address, String attribute, String expectedValue) throws IOException {
        ModelNodeResult readAttribute = ops.readAttribute(address, attribute);
        readAttribute.assertSuccess("Read operation for " + attribute + " failed");
        assertEquals("Read operation for " + attribute + " return wrong value", expectedValue,
                readAttribute.stringValue());
    }

}
