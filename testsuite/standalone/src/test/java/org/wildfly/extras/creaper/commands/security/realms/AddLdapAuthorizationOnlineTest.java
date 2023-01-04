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
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.online.CliException;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ServerVersion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class AddLdapAuthorizationOnlineTest {

    private OnlineManagementClient client;
    private Operations ops;
    private Administration administration;

    private static final String TEST_SECURITY_REALM_NAME = "creaperSecRealm";
    private static final String TEST_LDAP_CONNECTION = "creaperLdapConnection";
    private static final Address TEST_SECURITY_REALM_ADDRESS
            = Address.coreService("management").and("security-realm", TEST_SECURITY_REALM_NAME);
    private static final Address TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS
            = TEST_SECURITY_REALM_ADDRESS.and("authorization", "ldap");
    private static final Address TEST_PRINCIPAL_TO_GROUP_ADDRESS = TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS
            .and("group-search", "principal-to-group");
    private static final Address TEST_GROUP_TO_PRINCIPAL_ADDRESS = TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS
            .and("group-search", "group-to-principal");
    private static final Address TEST_ADVANCED_FILTER_ADDRESS = TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS
            .and("username-to-dn", "advanced-filter");
    private static final Address TEST_USERNAME_FILTER_ADDRESS = TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS
            .and("username-to-dn", "username-filter");
    private static final Address TEST_USERNAME_IS_DN_ADDRESS = TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS
            .and("username-to-dn", "username-is-dn");
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
    public void addSimple_principalToGroup() throws Exception {
        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .build();

        assertFalse("The ldap authz in security realm should not exist",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        client.apply(addLdapAuthorization);
        assertTrue("The ldap authz in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        assertTrue("The group-search=principal-to-group should be created in ldap authz of security realm",
                ops.exists(TEST_PRINCIPAL_TO_GROUP_ADDRESS));
    }

    @Test
    public void addSimple_groupToPrincipal() throws Exception {
        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .groupToPrincipal(new GroupToPrincipal.Builder()
                        .baseDn("ou=People,dc=jboss,dc=org")
                        .build())
                .build();

        assertFalse("The ldap authz in security realm should not exist",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        client.apply(addLdapAuthorization);
        assertTrue("The ldap authz in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        assertTrue("The group-search=group-to-principal should be created in ldap authz of security realm",
                ops.exists(TEST_GROUP_TO_PRINCIPAL_ADDRESS));
    }

    @Test
    public void addSimple_advancedFilter_P2G() throws Exception {
        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .advancedFilter(new AdvancedFilter.Builder()
                        .baseDn("ou=People,dc=jboss,dc=org")
                        .filter("(uid={0})")
                        .build())
                .build();

        assertFalse("The ldap authz in security realm should not exist",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        client.apply(addLdapAuthorization);
        assertTrue("The ldap authz in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        assertTrue("The username-to-dn=advanced-filter should be created in ldap authz of security realm",
                ops.exists(TEST_ADVANCED_FILTER_ADDRESS));
        assertTrue("The group-search=principal-to-group should be created in ldap authz of security realm",
                ops.exists(TEST_PRINCIPAL_TO_GROUP_ADDRESS));
    }

    @Test
    public void addSimple_usernameFilter_P2G() throws Exception {
        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .usernameFilter(new UsernameFilter.Builder()
                        .baseDn("ou=People,dc=jboss,dc=org")
                        .attribute("uid")
                        .build())
                .build();

        assertFalse("The ldap authz in security realm should not exist",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        client.apply(addLdapAuthorization);
        assertTrue("The ldap authz in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        assertTrue("The username-to-dn=username-filter should be created in ldap authz of security realm",
                ops.exists(TEST_USERNAME_FILTER_ADDRESS));
        assertTrue("The group-search=principal-to-group should be created in ldap authz of security realm",
                ops.exists(TEST_PRINCIPAL_TO_GROUP_ADDRESS));
    }

    @Test
    public void addSimple_usernameIsDn_P2G() throws Exception {
        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .usernameIsDn(new UsernameIsDn.Builder().build())
                .build();

        assertFalse("The ldap authz in security realm should not exist",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        client.apply(addLdapAuthorization);
        assertTrue("The ldap authz in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        assertTrue("The username-to-dn=username-is-dn should be created in ldap authz of security realm",
                ops.exists(TEST_USERNAME_IS_DN_ADDRESS));
        assertTrue("The group-search=principal-to-group should be created in ldap authz of security realm",
                ops.exists(TEST_PRINCIPAL_TO_GROUP_ADDRESS));
    }

    @Test
    public void addSimple_advancedFilter_G2P() throws Exception {
        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .groupToPrincipal(new GroupToPrincipal.Builder()
                        .baseDn("ou=People,dc=jboss,dc=org")
                        .build())
                .advancedFilter(new AdvancedFilter.Builder()
                        .baseDn("ou=People,dc=jboss,dc=org")
                        .filter("(uid={0})")
                        .build())
                .build();

        assertFalse("The ldap authz in security realm should not exist",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        client.apply(addLdapAuthorization);
        assertTrue("The ldap authz in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        assertTrue("The username-to-dn=advanced-filter should be created in ldap authz of security realm",
                ops.exists(TEST_ADVANCED_FILTER_ADDRESS));
        assertTrue("The group-search=group-to-principal should be created in ldap authz of security realm",
                ops.exists(TEST_GROUP_TO_PRINCIPAL_ADDRESS));
    }

    @Test
    public void addSimple_usernameFilter_G2P() throws Exception {
        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .groupToPrincipal(new GroupToPrincipal.Builder()
                        .baseDn("ou=People,dc=jboss,dc=org")
                        .build())
                .usernameFilter(new UsernameFilter.Builder()
                        .baseDn("ou=People,dc=jboss,dc=org")
                        .attribute("uid")
                        .build())
                .build();

        assertFalse("The ldap authz in security realm should not exist",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        client.apply(addLdapAuthorization);
        assertTrue("The ldap authz in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        assertTrue("The username-to-dn=username-filter should be created in ldap authz of security realm",
                ops.exists(TEST_USERNAME_FILTER_ADDRESS));
        assertTrue("The group-search=group-to-principal should be created in ldap authz of security realm",
                ops.exists(TEST_GROUP_TO_PRINCIPAL_ADDRESS));
    }

    @Test
    public void addSimple_usernameIsDn_G2P() throws Exception {
        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .groupToPrincipal(new GroupToPrincipal.Builder()
                        .baseDn("ou=People,dc=jboss,dc=org")
                        .build())
                .usernameIsDn(new UsernameIsDn.Builder().build())
                .build();

        assertFalse("The ldap authz in security realm should not exist",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        client.apply(addLdapAuthorization);
        assertTrue("The ldap authz in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        assertTrue("The username-to-dn=username-is-dn should be created in ldap authz of security realm",
                ops.exists(TEST_USERNAME_IS_DN_ADDRESS));
        assertTrue("The group-search=group-to-principal should be created in ldap authz of security realm",
                ops.exists(TEST_GROUP_TO_PRINCIPAL_ADDRESS));
    }

    @Test(expected = CommandFailedException.class)
    public void existing() throws Exception {
        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .groupToPrincipal(new GroupToPrincipal.Builder()
                        .baseDn("ou=People,dc=jboss,dc=org")
                        .build())
                .build();
        AddLdapAuthorization addLdapAuthorization2 = new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .groupToPrincipal(new GroupToPrincipal.Builder()
                        .baseDn("ou=Person,dc=jboss,dc=org")
                        .build())
                .build();

        client.apply(addLdapAuthorization);
        assertTrue("The ldap authz in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));

        client.apply(addLdapAuthorization2);
        fail("Ldap authorization is already configured in security realm, exception should be thrown");
    }

    @Test
    public void overrideExisting() throws Exception {
        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .groupToPrincipal(new GroupToPrincipal.Builder()
                        .baseDn("ou=People,dc=jboss,dc=org")
                        .build())
                .build();
        AddLdapAuthorization addLdapAuthorization2 = new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .groupToPrincipal(new GroupToPrincipal.Builder()
                        .baseDn("ou=Person,dc=jboss,dc=org")
                        .build())
                .replaceExisting()
                .build();

        client.apply(addLdapAuthorization);
        assertTrue("The ldap authz in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        assertTrue("The group-search=group-to-principal should be created in ldap authz of security realm",
                ops.exists(TEST_GROUP_TO_PRINCIPAL_ADDRESS));
        checkAttribute(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS, "connection", TEST_LDAP_CONNECTION);
        checkAttribute(TEST_GROUP_TO_PRINCIPAL_ADDRESS, "base-dn", "ou=People,dc=jboss,dc=org");

        client.apply(addLdapAuthorization2);
        assertTrue("The ldap authz in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        assertTrue("The group-search=group-to-principal should be created in ldap authz of security realm",
                ops.exists(TEST_GROUP_TO_PRINCIPAL_ADDRESS));
        checkAttribute(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS, "connection", TEST_LDAP_CONNECTION);
        checkAttribute(TEST_GROUP_TO_PRINCIPAL_ADDRESS, "base-dn", "ou=Person,dc=jboss,dc=org");
    }

    @Test
    public void addFull_principalToGroup() throws Exception {
        PrincipalToGroup.Builder principalToGroupBuilder = new PrincipalToGroup.Builder()
                .groupAttribute("testGroupAttribute")
                .groupDnAttribute("testGroupDnAttribute")
                .groupName("SIMPLE")
                .groupNameAttribute("testGroupNameAttribute")
                .iterative(true);

        if (client.version().greaterThanOrEqualTo(ServerVersion.VERSION_1_7_0)
                && !client.version().equalTo(ServerVersion.VERSION_2_0_0)) {
            principalToGroupBuilder.preferOriginalConnection(true);
        }
        if (client.version().greaterThanOrEqualTo(ServerVersion.VERSION_1_7_0)
                && !client.version().inRange(ServerVersion.VERSION_2_0_0, ServerVersion.VERSION_2_2_0)) {
            principalToGroupBuilder.skipMissingGroups(true);
        }

        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .principalToGroup(principalToGroupBuilder.build())
                .build();

        assertFalse("The ldap authz in security realm should not exist",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        client.apply(addLdapAuthorization);
        assertTrue("The ldap authz in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        assertTrue("The group-search=principal-to-group should be created in ldap authz of security realm",
                ops.exists(TEST_PRINCIPAL_TO_GROUP_ADDRESS));

        checkAttribute(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS, "connection", TEST_LDAP_CONNECTION);
        checkAttribute(TEST_PRINCIPAL_TO_GROUP_ADDRESS, "group-attribute", "testGroupAttribute");
        checkAttribute(TEST_PRINCIPAL_TO_GROUP_ADDRESS, "group-dn-attribute", "testGroupDnAttribute");
        checkAttribute(TEST_PRINCIPAL_TO_GROUP_ADDRESS, "group-name", "SIMPLE");
        checkAttribute(TEST_PRINCIPAL_TO_GROUP_ADDRESS, "group-name-attribute", "testGroupNameAttribute");
        checkAttribute(TEST_PRINCIPAL_TO_GROUP_ADDRESS, "iterative", "true");
        if (client.version().greaterThanOrEqualTo(ServerVersion.VERSION_1_7_0)
                && !client.version().equalTo(ServerVersion.VERSION_2_0_0)) {
            checkAttribute(TEST_PRINCIPAL_TO_GROUP_ADDRESS, "prefer-original-connection", "true");
        }
        if (client.version().greaterThanOrEqualTo(ServerVersion.VERSION_1_7_0)
                && !client.version().inRange(ServerVersion.VERSION_2_0_0, ServerVersion.VERSION_2_2_0)) {
            checkAttribute(TEST_PRINCIPAL_TO_GROUP_ADDRESS, "skip-missing-groups", "true");
        }
    }

    @Test
    public void addFull_groupToPrincipal() throws Exception {
        GroupToPrincipal.Builder groupToPrincipalBuilder = new GroupToPrincipal.Builder()
                .baseDn("testBaseDn")
                .groupDnAttribute("testGroupDnAttribute")
                .groupName("SIMPLE")
                .groupNameAttribute("testGroupNameAttribute")
                .iterative(false)
                .principalAttribute("testPrincipalAttribute")
                .recursive(false)
                .searchBy("SIMPLE");

        if (client.version().greaterThanOrEqualTo(ServerVersion.VERSION_1_7_0)
                && !client.version().inRange(ServerVersion.VERSION_2_0_0, ServerVersion.VERSION_2_2_0)) {
            groupToPrincipalBuilder.preferOriginalConnection(false);
        }

        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .groupToPrincipal(groupToPrincipalBuilder.build())
                .build();

        assertFalse("The ldap authz in security realm should not exist",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        client.apply(addLdapAuthorization);
        assertTrue("The ldap authz in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        assertTrue("The group-search=group-to-principal should be created in ldap authz of security realm",
                ops.exists(TEST_GROUP_TO_PRINCIPAL_ADDRESS));

        checkAttribute(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS, "connection", TEST_LDAP_CONNECTION);
        checkAttribute(TEST_GROUP_TO_PRINCIPAL_ADDRESS, "base-dn", "testBaseDn");
        checkAttribute(TEST_GROUP_TO_PRINCIPAL_ADDRESS, "group-dn-attribute", "testGroupDnAttribute");
        checkAttribute(TEST_GROUP_TO_PRINCIPAL_ADDRESS, "group-name", "SIMPLE");
        checkAttribute(TEST_GROUP_TO_PRINCIPAL_ADDRESS, "group-name-attribute", "testGroupNameAttribute");
        checkAttribute(TEST_GROUP_TO_PRINCIPAL_ADDRESS, "iterative", "false");
        if (client.version().greaterThanOrEqualTo(ServerVersion.VERSION_1_7_0)
                && !client.version().inRange(ServerVersion.VERSION_2_0_0, ServerVersion.VERSION_2_2_0)) {
            checkAttribute(TEST_GROUP_TO_PRINCIPAL_ADDRESS, "prefer-original-connection", "false");
        }
        checkAttribute(TEST_GROUP_TO_PRINCIPAL_ADDRESS, "principal-attribute", "testPrincipalAttribute");
        checkAttribute(TEST_GROUP_TO_PRINCIPAL_ADDRESS, "recursive", "false");
        checkAttribute(TEST_GROUP_TO_PRINCIPAL_ADDRESS, "search-by", "SIMPLE");
    }

    @Test
    public void addFull_advancedFilter() throws Exception {
        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .advancedFilter(new AdvancedFilter.Builder()
                        .baseDn("testBaseDn")
                        .filter("testFilter")
                        .force(true)
                        .recursive(true)
                        .userDnAttribute("testUserDnAttribute")
                        .build())
                .build();

        assertFalse("The ldap authz in security realm should not exist",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        client.apply(addLdapAuthorization);
        assertTrue("The ldap authz in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        assertTrue("The username-to-dn=advanced-filter should be created in ldap authz of security realm",
                ops.exists(TEST_ADVANCED_FILTER_ADDRESS));

        checkAttribute(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS, "connection", TEST_LDAP_CONNECTION);
        checkAttribute(TEST_ADVANCED_FILTER_ADDRESS, "base-dn", "testBaseDn");
        checkAttribute(TEST_ADVANCED_FILTER_ADDRESS, "filter", "testFilter");
        checkAttribute(TEST_ADVANCED_FILTER_ADDRESS, "force", "true");
        checkAttribute(TEST_ADVANCED_FILTER_ADDRESS, "recursive", "true");
        checkAttribute(TEST_ADVANCED_FILTER_ADDRESS, "user-dn-attribute", "testUserDnAttribute");
    }

    @Test
    public void addFull_usernameFilter() throws Exception {
        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .groupToPrincipal(new GroupToPrincipal.Builder()
                        .baseDn("ou=People,dc=jboss,dc=org")
                        .build())
                .usernameFilter(new UsernameFilter.Builder()
                        .attribute("testAttribute")
                        .baseDn("testBaseDn")
                        .force(false)
                        .recursive(false)
                        .userDnAttribute("testUserDnAttribute")
                        .build())
                .build();

        assertFalse("The ldap authz in security realm should not exist",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        client.apply(addLdapAuthorization);
        assertTrue("The ldap authz in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        assertTrue("The username-to-dn=username-filter should be created in ldap authz of security realm",
                ops.exists(TEST_USERNAME_FILTER_ADDRESS));

        checkAttribute(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS, "connection", TEST_LDAP_CONNECTION);
        checkAttribute(TEST_USERNAME_FILTER_ADDRESS, "attribute", "testAttribute");
        checkAttribute(TEST_USERNAME_FILTER_ADDRESS, "base-dn", "testBaseDn");
        checkAttribute(TEST_USERNAME_FILTER_ADDRESS, "force", "false");
        checkAttribute(TEST_USERNAME_FILTER_ADDRESS, "recursive", "false");
        checkAttribute(TEST_USERNAME_FILTER_ADDRESS, "user-dn-attribute", "testUserDnAttribute");
    }

    @Test
    public void addFull_usernameIsDn() throws Exception {
        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .usernameIsDn(new UsernameIsDn.Builder()
                        .force(true)
                        .build())
                .build();

        assertFalse("The ldap authz in security realm should not exist",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        client.apply(addLdapAuthorization);
        assertTrue("The ldap authz in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        assertTrue("The username-to-dn=username-is-dn should be created in ldap authz of security realm",
                ops.exists(TEST_USERNAME_IS_DN_ADDRESS));

        checkAttribute(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS, "connection", TEST_LDAP_CONNECTION);
        checkAttribute(TEST_USERNAME_IS_DN_ADDRESS, "force", "true");
    }

    @Test
    public void addWithCache_byAccessTime_principalToGroup() throws Exception {
        Assume.assumeTrue("Cache for principal-to-group is available since WildFly 9 or in EAP 6.4.x.",
                client.version().greaterThanOrEqualTo(ServerVersion.VERSION_1_7_0)
                && !client.version().inRange(ServerVersion.VERSION_2_0_0, ServerVersion.VERSION_2_2_0));

        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .principalToGroup(new PrincipalToGroup.Builder()
                        .cache(new LdapCache.Builder()
                                .byAccessTime()
                                .cacheFailures(true)
                                .evictionTime(3)
                                .maxCacheSize(5)
                                .build())
                        .build())
                .build();

        Address cacheAddress = TEST_PRINCIPAL_TO_GROUP_ADDRESS.and("cache", "by-access-time");

        client.apply(addLdapAuthorization);
        assertTrue("The ldap authz in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        assertTrue("The group-search=principal-to-group should be created in ldap authz of security realm",
                ops.exists(TEST_PRINCIPAL_TO_GROUP_ADDRESS));
        assertTrue("The cache=by-access-time element should be created in group-search=principal-to-group",
                ops.exists(cacheAddress));

        checkAttribute(cacheAddress, "cache-failures", "true");
        checkAttribute(cacheAddress, "eviction-time", "3");
        checkAttribute(cacheAddress, "max-cache-size", "5");
    }

    @Test
    public void addWithCache_bySearchTime_principalToGroup() throws Exception {
        Assume.assumeTrue("Cache for principal-to-group is available since WildFly 9 or in EAP 6.4.x.",
                client.version().greaterThanOrEqualTo(ServerVersion.VERSION_1_7_0)
                && !client.version().inRange(ServerVersion.VERSION_2_0_0, ServerVersion.VERSION_2_2_0));

        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .principalToGroup(new PrincipalToGroup.Builder()
                        .cache(new LdapCache.Builder()
                                .bySearchTime()
                                .cacheFailures(false)
                                .evictionTime(2)
                                .maxCacheSize(10)
                                .build())
                        .build())
                .build();

        Address cacheAddress = TEST_PRINCIPAL_TO_GROUP_ADDRESS.and("cache", "by-search-time");

        client.apply(addLdapAuthorization);
        assertTrue("The ldap authz in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        assertTrue("The group-search=principal-to-group should be created in ldap authz of security realm",
                ops.exists(TEST_PRINCIPAL_TO_GROUP_ADDRESS));
        assertTrue("The cache=by-search-time element should be created in group-search=principal-to-group",
                ops.exists(cacheAddress));

        checkAttribute(cacheAddress, "cache-failures", "false");
        checkAttribute(cacheAddress, "eviction-time", "2");
        checkAttribute(cacheAddress, "max-cache-size", "10");
    }

    @Test
    public void addWithCache_byAccessTime_groupToPrincipal() throws Exception {
        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .groupToPrincipal(new GroupToPrincipal.Builder()
                        .baseDn("ou=People,dc=jboss,dc=org")
                        .cache(new LdapCache.Builder()
                                .byAccessTime()
                                .cacheFailures(false)
                                .evictionTime(2)
                                .maxCacheSize(10)
                                .build())
                        .build())
                .build();

        Address cacheAddress = TEST_GROUP_TO_PRINCIPAL_ADDRESS.and("cache", "by-access-time");

        client.apply(addLdapAuthorization);
        assertTrue("The ldap authz in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        assertTrue("The group-search=group-to-principal should be created in ldap authz of security realm",
                ops.exists(TEST_GROUP_TO_PRINCIPAL_ADDRESS));
        assertTrue("The cache=by-access-time element should be created in group-search=group-to-principal",
                ops.exists(cacheAddress));

        checkAttribute(cacheAddress, "cache-failures", "false");
        checkAttribute(cacheAddress, "eviction-time", "2");
        checkAttribute(cacheAddress, "max-cache-size", "10");

    }

    @Test
    public void addWithCache_bySearchTime_groupToPrincipal() throws Exception {
        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .groupToPrincipal(new GroupToPrincipal.Builder()
                        .baseDn("ou=People,dc=jboss,dc=org")
                        .cache(new LdapCache.Builder()
                                .bySearchTime()
                                .cacheFailures(true)
                                .evictionTime(3)
                                .maxCacheSize(5)
                                .build())
                        .build())
                .build();

        Address cacheAddress = TEST_GROUP_TO_PRINCIPAL_ADDRESS.and("cache", "by-search-time");

        client.apply(addLdapAuthorization);
        assertTrue("The ldap authz in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        assertTrue("The group-search=group-to-principal should be created in ldap authz of security realm",
                ops.exists(TEST_GROUP_TO_PRINCIPAL_ADDRESS));
        assertTrue("The cache=by-search-time element should be created in group-search=group-to-principal",
                ops.exists(cacheAddress));

        checkAttribute(cacheAddress, "cache-failures", "true");
        checkAttribute(cacheAddress, "eviction-time", "3");
        checkAttribute(cacheAddress, "max-cache-size", "5");
    }

    @Test
    public void addWithCache_byAccessTime_advancedFilter() throws Exception {
        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .advancedFilter(new AdvancedFilter.Builder()
                        .baseDn("ou=People,dc=jboss,dc=org")
                        .filter("(uid={0})")
                        .cache(new LdapCache.Builder()
                                .byAccessTime()
                                .cacheFailures(true)
                                .evictionTime(3)
                                .maxCacheSize(5)
                                .build())
                        .build())
                .build();

        Address cacheAddress = TEST_ADVANCED_FILTER_ADDRESS.and("cache", "by-access-time");

        client.apply(addLdapAuthorization);
        assertTrue("The ldap authz in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        assertTrue("The username-to-dn=advanced-filter should be created in ldap authz of security realm",
                ops.exists(TEST_ADVANCED_FILTER_ADDRESS));
        assertTrue("The cache=by-access-time element should be created in username-to-dn=advanced-filter",
                ops.exists(cacheAddress));

        checkAttribute(cacheAddress, "cache-failures", "true");
        checkAttribute(cacheAddress, "eviction-time", "3");
        checkAttribute(cacheAddress, "max-cache-size", "5");
    }

    @Test
    public void addWithCache_bySearchTime_advancedFilter() throws Exception {
        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .groupToPrincipal(new GroupToPrincipal.Builder()
                        .baseDn("ou=People,dc=jboss,dc=org")
                        .build())
                .advancedFilter(new AdvancedFilter.Builder()
                        .baseDn("ou=People,dc=jboss,dc=org")
                        .filter("(uid={0})")
                        .cache(new LdapCache.Builder()
                                .bySearchTime()
                                .cacheFailures(false)
                                .evictionTime(2)
                                .maxCacheSize(10)
                                .build())
                        .build())
                .build();

        Address cacheAddress = TEST_ADVANCED_FILTER_ADDRESS.and("cache", "by-search-time");

        client.apply(addLdapAuthorization);
        assertTrue("The ldap authz in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        assertTrue("The username-to-dn=advanced-filter should be created in ldap authz of security realm",
                ops.exists(TEST_ADVANCED_FILTER_ADDRESS));
        assertTrue("The cache=by-search-time element should be created in username-to-dn=advanced-filter",
                ops.exists(cacheAddress));

        checkAttribute(cacheAddress, "cache-failures", "false");
        checkAttribute(cacheAddress, "eviction-time", "2");
        checkAttribute(cacheAddress, "max-cache-size", "10");
    }

    @Test
    public void addWithCache_byAccessTime_usernameFilter() throws Exception {
        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .groupToPrincipal(new GroupToPrincipal.Builder()
                        .baseDn("ou=People,dc=jboss,dc=org")
                        .build())
                .usernameFilter(new UsernameFilter.Builder()
                        .attribute("uid")
                        .baseDn("ou=People,dc=jboss,dc=org")
                        .cache(new LdapCache.Builder()
                                .byAccessTime()
                                .cacheFailures(false)
                                .evictionTime(2)
                                .maxCacheSize(10)
                                .build())
                        .build())
                .build();

        Address cacheAddress = TEST_USERNAME_FILTER_ADDRESS.and("cache", "by-access-time");

        client.apply(addLdapAuthorization);
        assertTrue("The ldap authz in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        assertTrue("The username-to-dn=username-filter should be created in ldap authz of security realm",
                ops.exists(TEST_USERNAME_FILTER_ADDRESS));
        assertTrue("The cache=by-access-time element should be created in username-to-dn=username-filter",
                ops.exists(cacheAddress));

        checkAttribute(cacheAddress, "cache-failures", "false");
        checkAttribute(cacheAddress, "eviction-time", "2");
        checkAttribute(cacheAddress, "max-cache-size", "10");
    }

    @Test
    public void addWithCache_bySearchTime_usernameFilter() throws Exception {
        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .usernameFilter(new UsernameFilter.Builder()
                        .attribute("uid")
                        .baseDn("ou=People,dc=jboss,dc=org")
                        .cache(new LdapCache.Builder()
                                .bySearchTime()
                                .cacheFailures(true)
                                .evictionTime(3)
                                .maxCacheSize(5)
                                .build())
                        .build())
                .build();

        Address cacheAddress = TEST_USERNAME_FILTER_ADDRESS.and("cache", "by-search-time");

        client.apply(addLdapAuthorization);
        assertTrue("The ldap authz in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        assertTrue("The username-to-dn=username-filter should be created in ldap authz of security realm",
                ops.exists(TEST_USERNAME_FILTER_ADDRESS));
        assertTrue("The cache=by-search-time element should be created in username-to-dn=username-filter",
                ops.exists(cacheAddress));

        checkAttribute(cacheAddress, "cache-failures", "true");
        checkAttribute(cacheAddress, "eviction-time", "3");
        checkAttribute(cacheAddress, "max-cache-size", "5");
    }

    @Test
    public void addWithCache_byAccessTime_usernameIsDn() throws Exception {
        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .usernameIsDn(new UsernameIsDn.Builder()
                        .cache(new LdapCache.Builder()
                                .byAccessTime()
                                .cacheFailures(false)
                                .evictionTime(2)
                                .maxCacheSize(10)
                                .build())
                        .build())
                .build();

        Address cacheAddress = TEST_USERNAME_IS_DN_ADDRESS.and("cache", "by-access-time");

        client.apply(addLdapAuthorization);
        assertTrue("The ldap authz in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        assertTrue("The username-to-dn=username-is-dn should be created in ldap authz of security realm",
                ops.exists(TEST_USERNAME_IS_DN_ADDRESS));
        assertTrue("The cache=by-access-time element should be created in username-to-dn=username-is-dn",
                ops.exists(cacheAddress));

        checkAttribute(cacheAddress, "cache-failures", "false");
        checkAttribute(cacheAddress, "eviction-time", "2");
        checkAttribute(cacheAddress, "max-cache-size", "10");
    }

    @Test
    public void addWithCache_bySearchTime_usernameIsDn() throws Exception {
        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .groupToPrincipal(new GroupToPrincipal.Builder()
                        .baseDn("ou=People,dc=jboss,dc=org")
                        .build())
                .usernameIsDn(new UsernameIsDn.Builder()
                        .cache(new LdapCache.Builder()
                                .bySearchTime()
                                .cacheFailures(true)
                                .evictionTime(3)
                                .maxCacheSize(5)
                                .build())
                        .build())
                .build();

        Address cacheAddress = TEST_USERNAME_IS_DN_ADDRESS.and("cache", "by-search-time");

        client.apply(addLdapAuthorization);
        assertTrue("The ldap authz in security realm should be created",
                ops.exists(TEST_SECURITY_REALM_LDAP_AUTHZ_ADDRESS));
        assertTrue("The username-to-dn=username-is-dn should be created in ldap authz of security realm",
                ops.exists(TEST_USERNAME_IS_DN_ADDRESS));
        assertTrue("The cache=by-search-time element should be created in username-to-dn=username-is-dn",
                ops.exists(cacheAddress));

        checkAttribute(cacheAddress, "cache-failures", "true");
        checkAttribute(cacheAddress, "eviction-time", "3");
        checkAttribute(cacheAddress, "max-cache-size", "5");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_NullSecurityRealm() throws Exception {
        new AddLdapAuthorization.Builder(null)
                .connection(TEST_LDAP_CONNECTION)
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .build();
        fail("Creating command with null security realm should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_EmptySecurityRealm() throws Exception {
        new AddLdapAuthorization.Builder("")
                .connection(TEST_LDAP_CONNECTION)
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .build();
        fail("Creating command with empty security realm should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_NullConnection() throws Exception {
        new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(null)
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .build();
        fail("Creating command with null connection should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_EmptyConnection() throws Exception {
        new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection("")
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .build();
        fail("Creating command with empty connection should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_NullBaseDn_groupToPrincipal() throws Exception {
        new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .groupToPrincipal(new GroupToPrincipal.Builder()
                        .build())
                .build();
        fail("Creating command with null base-dn for GroupToPrincipal should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_EmptyBaseDn_groupToPrincipal() throws Exception {
        new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .groupToPrincipal(new GroupToPrincipal.Builder()
                        .baseDn("")
                        .build())
                .build();
        fail("Creating command with empty base-dn for GroupToPrincipal should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_NullBaseDn_advancedFilter() throws Exception {
        new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .advancedFilter(new AdvancedFilter.Builder()
                        .filter("testFilter")
                        .build())
                .build();
        fail("Creating command with null base-dn for AdvancedFilter should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_EmptyBaseDn_advancedFilter() throws Exception {
        new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .advancedFilter(new AdvancedFilter.Builder()
                        .baseDn("")
                        .filter("testFilter")
                        .build())
                .build();
        fail("Creating command with empty base-dn for AdvancedFilter should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_NullFilter_advancedFilter() throws Exception {
        new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .advancedFilter(new AdvancedFilter.Builder()
                        .baseDn("testBaseDn")
                        .build())
                .build();
        fail("Creating command with null filter for AdvancedFilter should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_EmptyFilter_advancedFilter() throws Exception {
        new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .advancedFilter(new AdvancedFilter.Builder()
                        .baseDn("testBaseDn")
                        .filter("")
                        .build())
                .build();
        fail("Creating command with emtpy filter for AdvancedFilter should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_NullBaseDn_usernameFilter() throws Exception {
        new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .usernameFilter(new UsernameFilter.Builder()
                        .attribute("testAttribute")
                        .build())
                .build();
        fail("Creating command with null base-dn for UsernameFilter should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_EmptyBaseDn_usernameFilter() throws Exception {
        new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .usernameFilter(new UsernameFilter.Builder()
                        .attribute("testAttribute")
                        .baseDn("")
                        .build())
                .build();
        fail("Creating command with empty base-dn for UsernameFilter should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_NullAttribute_usernameFilter() throws Exception {
        new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .usernameFilter(new UsernameFilter.Builder()
                        .baseDn("testBaseDn")
                        .build())
                .build();
        fail("Creating command with null attribute for UsernameFilter should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_EmptyAttribute_usernameFilter() throws Exception {
        new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .usernameFilter(new UsernameFilter.Builder()
                        .attribute("")
                        .baseDn("testBaseDn")
                        .build())
                .build();
        fail("Creating command with empty attribute for UsernameFilter should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_WithoutGroupSearch() throws Exception {
        new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .build();
        fail("Creating command without any group-search should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_TwoGroupSearch() throws Exception {
        new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .groupToPrincipal(new GroupToPrincipal.Builder()
                        .baseDn("testBaseDn")
                        .build())
                .build();
        fail("Creating command with two group-search should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_TwoUsernameToDn_advancedFilter_usernameFilter() throws Exception {
        new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .advancedFilter(new AdvancedFilter.Builder()
                        .baseDn("testBaseDn")
                        .filter("testFilter")
                        .build())
                .usernameFilter(new UsernameFilter.Builder()
                        .attribute("testAttribute")
                        .baseDn("testBaseDn")
                        .build())
                .build();
        fail("Creating command with two username-to-dn should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_TwoUsernameToDn_advancedFilter_usernameIsDn() throws Exception {
        new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .advancedFilter(new AdvancedFilter.Builder()
                        .baseDn("testBaseDn")
                        .filter("testFilter")
                        .build())
                .usernameIsDn(new UsernameIsDn.Builder().build())
                .build();
        fail("Creating command with two username-to-dn should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_TwoUsernameToDn_usernameFilter_usernameIsDn() throws Exception {
        new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .usernameFilter(new UsernameFilter.Builder()
                        .attribute("testAttribute")
                        .baseDn("testBaseDn")
                        .build())
                .usernameIsDn(new UsernameIsDn.Builder().build())
                .build();
        fail("Creating command with two username-to-dn should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_ThreeUsernameToDn() throws Exception {
        new AddLdapAuthorization.Builder(TEST_SECURITY_REALM_NAME)
                .connection(TEST_LDAP_CONNECTION)
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .advancedFilter(new AdvancedFilter.Builder()
                        .baseDn("testBaseDn")
                        .filter("testFilter")
                        .build())
                .usernameFilter(new UsernameFilter.Builder()
                        .attribute("testAttribute")
                        .baseDn("testBaseDn")
                        .build())
                .usernameIsDn(new UsernameIsDn.Builder().build())
                .build();
        fail("Creating command with three username-to-dn should throw exception");
    }

    private void checkAttribute(Address address, String attribute, String expectedValue) throws IOException {
        ModelNodeResult readAttribute = ops.readAttribute(address, attribute);
        readAttribute.assertSuccess("Read operation for " + attribute + " failed");
        assertEquals("Read operation for " + attribute + " return wrong value", expectedValue,
                readAttribute.stringValue());
    }

}
