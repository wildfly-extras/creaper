package org.wildfly.extras.creaper.commands.security.realms;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;

import static org.junit.Assert.fail;
import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;

public class AddLdapAuthorizationOfflineTest {

    private static final String REALM_SIMPLE = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\"/>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_PRINCIPAL_TO_GROUP_SIMPLE = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authorization>\n"
            + "                    <ldap connection=\"creaperLdapConnection\">\n"
            + "                        <group-search>\n"
            + "                            <principal-to-group/>\n"
            + "                        </group-search>\n"
            + "                    </ldap>\n"
            + "                </authorization>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_GROUP_TO_PRINCIPAL_SIMPLE = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authorization>\n"
            + "                    <ldap connection=\"creaperLdapConnection\">\n"
            + "                        <group-search>\n"
            + "                            <group-to-principal base-dn=\"testBaseDnGroupToPrincipal\">\n"
            + "                                <membership-filter/>\n"
            + "                            </group-to-principal>\n"
            + "                        </group-search>\n"
            + "                    </ldap>\n"
            + "                </authorization>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_ADVANCED_FILTER_SIMPLE = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authorization>\n"
            + "                    <ldap connection=\"creaperLdapConnection\">\n"
            + "                        <username-to-dn>\n"
            + "                            <advanced-filter base-dn=\"testBaseDnAdvancedFilter\" "
            + "filter=\"testFilterAdvancedFilter\"/>\n"
            + "                        </username-to-dn>\n"
            + "                        <group-search>\n"
            + "                            <principal-to-group/>\n"
            + "                        </group-search>\n"
            + "                    </ldap>\n"
            + "                </authorization>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_USERNAME_FILTER_SIMPLE = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authorization>\n"
            + "                    <ldap connection=\"creaperLdapConnection\">\n"
            + "                        <username-to-dn>\n"
            + "                            <username-filter base-dn=\"testBaseDnUsernameFilter\" "
            + "attribute=\"testAttributeUsernameFilter\"/>\n"
            + "                        </username-to-dn>\n"
            + "                        <group-search>\n"
            + "                            <principal-to-group/>\n"
            + "                        </group-search>\n"
            + "                    </ldap>\n"
            + "                </authorization>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_USERNAME_IS_DN_SIMPLE = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authorization>\n"
            + "                    <ldap connection=\"creaperLdapConnection\">\n"
            + "                        <username-to-dn>\n"
            + "                            <username-is-dn/>\n"
            + "                        </username-to-dn>\n"
            + "                        <group-search>\n"
            + "                            <principal-to-group/>\n"
            + "                        </group-search>\n"
            + "                    </ldap>\n"
            + "                </authorization>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_GROUP_TO_PRINCIPAL_SIMPLE_REPLACED = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authorization>\n"
            + "                    <ldap connection=\"creaperLdapConnection\">\n"
            + "                        <group-search>\n"
            + "                            <group-to-principal base-dn=\"testBaseDnGroupToPrincipalReplaced\">\n"
            + "                                <membership-filter/>\n"
            + "                            </group-to-principal>\n"
            + "                        </group-search>\n"
            + "                    </ldap>\n"
            + "                </authorization>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_PRINCIPAL_TO_GROUP_FULL = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authorization>\n"
            + "                    <ldap connection=\"creaperLdapConnection\">\n"
            + "                        <group-search group-dn-attribute=\"testGroupDnAttributeP2G\" "
            + "group-name=\"testGroupNameP2G\" group-name-attribute=\"testGroupNameAttributeP2G\" iterative=\"true\">\n"
            + "                            <principal-to-group group-attribute=\"testGroupAttributeP2G\" "
            + "prefer-original-connection=\"false\" skip-missing-groups=\"true\"/>\n"
            + "                        </group-search>\n"
            + "                    </ldap>\n"
            + "                </authorization>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_GROUP_TO_PRINCIPAL_FULL = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authorization>\n"
            + "                    <ldap connection=\"creaperLdapConnection\">\n"
            + "                        <group-search group-dn-attribute=\"testGroupDnAttributeG2P\" "
            + "group-name=\"testGroupNameG2P\" group-name-attribute=\"testGroupNameAttributeG2P\" iterative=\"false\">\n"
            + "                            <group-to-principal base-dn=\"testBaseDnG2P\" "
            + "prefer-original-connection=\"true\" recursive=\"false\" search-by=\"testSearchByG2P\">\n"
            + "                                <membership-filter principal-attribute=\"testPrincipalAttributeG2P\"/>\n"
            + "                            </group-to-principal>\n"
            + "                        </group-search>\n"
            + "                    </ldap>\n"
            + "                </authorization>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_ADVANCED_FILTER_FULL = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authorization>\n"
            + "                    <ldap connection=\"creaperLdapConnection\">\n"
            + "                        <username-to-dn force=\"true\">\n"
            + "                            <advanced-filter base-dn=\"testBaseDnAF\" filter=\"testFilterAF\" "
            + "recursive=\"false\" user-dn-attribute=\"testUserDnAttributeAF\"/>\n"
            + "                        </username-to-dn>\n"
            + "                        <group-search>\n"
            + "                            <principal-to-group/>\n"
            + "                        </group-search>\n"
            + "                    </ldap>\n"
            + "                </authorization>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_USERNAME_FILTER_FULL = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authorization>\n"
            + "                    <ldap connection=\"creaperLdapConnection\">\n"
            + "                        <username-to-dn force=\"false\">\n"
            + "                            <username-filter base-dn=\"testBaseDnUF\" attribute=\"testAttributeUF\" "
            + "recursive=\"true\" user-dn-attribute=\"testUserDnAttributeUF\"/>\n"
            + "                        </username-to-dn>\n"
            + "                        <group-search>\n"
            + "                            <principal-to-group/>\n"
            + "                        </group-search>\n"
            + "                    </ldap>\n"
            + "                </authorization>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";
    private static final String REALM_WITH_USERNAME_IS_DN_FULL = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authorization>\n"
            + "                    <ldap connection=\"creaperLdapConnection\">\n"
            + "                        <username-to-dn force=\"true\">\n"
            + "                            <username-is-dn/>\n"
            + "                        </username-to-dn>\n"
            + "                        <group-search>\n"
            + "                            <principal-to-group/>\n"
            + "                        </group-search>\n"
            + "                    </ldap>\n"
            + "                </authorization>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_PRINCIPAL_TO_GROUP_WITH_CACHE = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authorization>\n"
            + "                    <ldap connection=\"creaperLdapConnection\">\n"
            + "                        <group-search>\n"
            + "                            <cache type=\"by-access-time\" cache-failures=\"true\" eviction-time=\"3\" "
            + "max-cache-size=\"5\"/>\n"
            + "                            <principal-to-group/>\n"
            + "                        </group-search>\n"
            + "                    </ldap>\n"
            + "                </authorization>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_GROUP_TO_PRINCIPAL_WITH_CACHE = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authorization>\n"
            + "                    <ldap connection=\"creaperLdapConnection\">\n"
            + "                        <group-search>\n"
            + "                            <cache type=\"by-search-time\" cache-failures=\"false\" eviction-time=\"2\" "
            + "max-cache-size=\"10\"/>\n"
            + "                            <group-to-principal base-dn=\"testBaseDnGroupToPrincipal\">\n"
            + "                                <membership-filter/>\n"
            + "                            </group-to-principal>\n"
            + "                        </group-search>\n"
            + "                    </ldap>\n"
            + "                </authorization>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_ADVANCED_FILTER_WITH_CACHE = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authorization>\n"
            + "                    <ldap connection=\"creaperLdapConnection\">\n"
            + "                        <username-to-dn>\n"
            + "                            <cache type=\"by-access-time\" cache-failures=\"true\" eviction-time=\"2\" "
            + "max-cache-size=\"10\"/>\n"
            + "                            <advanced-filter base-dn=\"testBaseDnAdvancedFilter\" "
            + "filter=\"testFilterAdvancedFilter\"/>\n"
            + "                        </username-to-dn>\n"
            + "                        <group-search>\n"
            + "                            <principal-to-group/>\n"
            + "                        </group-search>\n"
            + "                    </ldap>\n"
            + "                </authorization>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_USERNAME_FILTER_WITH_CACHE = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authorization>\n"
            + "                    <ldap connection=\"creaperLdapConnection\">\n"
            + "                        <username-to-dn>\n"
            + "                            <cache type=\"by-search-time\" cache-failures=\"false\" eviction-time=\"3\" "
            + "max-cache-size=\"5\"/>\n"
            + "                            <username-filter base-dn=\"testBaseDnUsernameFilter\" "
            + "attribute=\"testAttributeUsernameFilter\"/>\n"
            + "                        </username-to-dn>\n"
            + "                        <group-search>\n"
            + "                            <principal-to-group/>\n"
            + "                        </group-search>\n"
            + "                    </ldap>\n"
            + "                </authorization>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";
    private static final String REALM_WITH_USERNAME_IS_DN_WITH_CACHE = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authorization>\n"
            + "                    <ldap connection=\"creaperLdapConnection\">\n"
            + "                        <username-to-dn>\n"
            + "                            <cache type=\"by-access-time\" cache-failures=\"true\" eviction-time=\"3\" "
            + "max-cache-size=\"5\"/>\n"
            + "                            <username-is-dn/>\n"
            + "                        </username-to-dn>\n"
            + "                        <group-search>\n"
            + "                            <principal-to-group/>\n"
            + "                        </group-search>\n"
            + "                    </ldap>\n"
            + "                </authorization>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_PROPERTIES_AUTHZ = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authorization>\n"
            + "                    <properties path=\"users.properties\"/>\n"
            + "                </authorization>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_MAP_GROUPS_TO_ROLES = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authorization map-groups-to-roles=\"true\"/>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_MAP_GROUPS_TO_ROLES_EXPECTED = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authorization map-groups-to-roles=\"true\">\n"
            + "                    <ldap connection=\"creaperLdapConnection\">\n"
            + "                        <group-search>\n"
            + "                            <principal-to-group/>\n"
            + "                        </group-search>\n"
            + "                    </ldap>\n"
            + "                </authorization>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Before
    public void setUp() {
        XMLUnit.setNormalizeWhitespace(true);
    }

    @Test
    public void addSimple_principalToGroup() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder("creaperSecRealm")
                .connection("creaperLdapConnection")
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .build();

        assertXmlIdentical(REALM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLdapAuthorization);
        assertXmlIdentical(REALM_WITH_PRINCIPAL_TO_GROUP_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addSimple_groupToPrincipal() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder("creaperSecRealm")
                .connection("creaperLdapConnection")
                .groupToPrincipal(new GroupToPrincipal.Builder()
                        .baseDn("testBaseDnGroupToPrincipal")
                        .build())
                .build();

        assertXmlIdentical(REALM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLdapAuthorization);
        assertXmlIdentical(REALM_WITH_GROUP_TO_PRINCIPAL_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addSimple_advancedFilter() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder("creaperSecRealm")
                .connection("creaperLdapConnection")
                .advancedFilter(new AdvancedFilter.Builder()
                        .baseDn("testBaseDnAdvancedFilter")
                        .filter("testFilterAdvancedFilter")
                        .build())
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .build();

        assertXmlIdentical(REALM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLdapAuthorization);
        assertXmlIdentical(REALM_WITH_ADVANCED_FILTER_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addSimple_usernameFilter() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder("creaperSecRealm")
                .connection("creaperLdapConnection")
                .usernameFilter(new UsernameFilter.Builder()
                        .baseDn("testBaseDnUsernameFilter")
                        .attribute("testAttributeUsernameFilter")
                        .build())
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .build();

        assertXmlIdentical(REALM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLdapAuthorization);
        assertXmlIdentical(REALM_WITH_USERNAME_FILTER_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addSimple_usernameIsDn() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder("creaperSecRealm")
                .connection("creaperLdapConnection")
                .usernameIsDn(new UsernameIsDn.Builder().build())
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .build();

        assertXmlIdentical(REALM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLdapAuthorization);
        assertXmlIdentical(REALM_WITH_USERNAME_IS_DN_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void existing() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_WITH_GROUP_TO_PRINCIPAL_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder("creaperSecRealm")
                .connection("creaperLdapConnection")
                .groupToPrincipal(new GroupToPrincipal.Builder()
                        .baseDn("testBaseDnGroupToPrincipalReplaced")
                        .build())
                .build();

        assertXmlIdentical(REALM_WITH_GROUP_TO_PRINCIPAL_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLdapAuthorization);

        fail("Ldap authorization already exists in configuration of creaperSecRealm, exception should be thrown");
    }

    @Test
    public void overrideExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_WITH_GROUP_TO_PRINCIPAL_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder("creaperSecRealm")
                .connection("creaperLdapConnection")
                .groupToPrincipal(new GroupToPrincipal.Builder()
                        .baseDn("testBaseDnGroupToPrincipalReplaced")
                        .build())
                .replaceExisting()
                .build();

        assertXmlIdentical(REALM_WITH_GROUP_TO_PRINCIPAL_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLdapAuthorization);
        assertXmlIdentical(REALM_WITH_GROUP_TO_PRINCIPAL_SIMPLE_REPLACED, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addFull_principalToGroup() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder("creaperSecRealm")
                .connection("creaperLdapConnection")
                .principalToGroup(new PrincipalToGroup.Builder()
                        .groupAttribute("testGroupAttributeP2G")
                        .groupDnAttribute("testGroupDnAttributeP2G")
                        .groupName("testGroupNameP2G")
                        .groupNameAttribute("testGroupNameAttributeP2G")
                        .iterative(true)
                        .preferOriginalConnection(false)
                        .skipMissingGroups(true)
                        .build())
                .build();

        assertXmlIdentical(REALM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLdapAuthorization);
        assertXmlIdentical(REALM_WITH_PRINCIPAL_TO_GROUP_FULL, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addFull_groupToPrincipal() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder("creaperSecRealm")
                .connection("creaperLdapConnection")
                .groupToPrincipal(new GroupToPrincipal.Builder()
                        .baseDn("testBaseDnG2P")
                        .groupDnAttribute("testGroupDnAttributeG2P")
                        .groupName("testGroupNameG2P")
                        .groupNameAttribute("testGroupNameAttributeG2P")
                        .iterative(false)
                        .preferOriginalConnection(true)
                        .principalAttribute("testPrincipalAttributeG2P")
                        .recursive(false)
                        .searchBy("testSearchByG2P")
                        .build())
                .build();

        assertXmlIdentical(REALM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLdapAuthorization);
        assertXmlIdentical(REALM_WITH_GROUP_TO_PRINCIPAL_FULL, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addFull_advancedFilter() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder("creaperSecRealm")
                .connection("creaperLdapConnection")
                .advancedFilter(new AdvancedFilter.Builder()
                        .baseDn("testBaseDnAF")
                        .filter("testFilterAF")
                        .force(true)
                        .recursive(false)
                        .userDnAttribute("testUserDnAttributeAF")
                        .build())
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .build();

        assertXmlIdentical(REALM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLdapAuthorization);
        assertXmlIdentical(REALM_WITH_ADVANCED_FILTER_FULL, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addFull_usernameFilter() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder("creaperSecRealm")
                .connection("creaperLdapConnection")
                .usernameFilter(new UsernameFilter.Builder()
                        .baseDn("testBaseDnUF")
                        .attribute("testAttributeUF")
                        .force(false)
                        .recursive(true)
                        .userDnAttribute("testUserDnAttributeUF")
                        .build())
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .build();

        assertXmlIdentical(REALM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLdapAuthorization);
        assertXmlIdentical(REALM_WITH_USERNAME_FILTER_FULL, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addFull_usernameIsDn() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder("creaperSecRealm")
                .connection("creaperLdapConnection")
                .usernameIsDn(new UsernameIsDn.Builder()
                        .force(true)
                        .build())
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .build();

        assertXmlIdentical(REALM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLdapAuthorization);
        assertXmlIdentical(REALM_WITH_USERNAME_IS_DN_FULL, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void add_principalToGroup_withCache() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder("creaperSecRealm")
                .connection("creaperLdapConnection")
                .principalToGroup(new PrincipalToGroup.Builder()
                        .cache(new LdapCache.Builder()
                                .byAccessTime()
                                .cacheFailures(true)
                                .evictionTime(3)
                                .maxCacheSize(5)
                                .build())
                        .build())
                .build();

        assertXmlIdentical(REALM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLdapAuthorization);
        assertXmlIdentical(REALM_WITH_PRINCIPAL_TO_GROUP_WITH_CACHE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void add_groupToPrincipal_withCache() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder("creaperSecRealm")
                .connection("creaperLdapConnection")
                .groupToPrincipal(new GroupToPrincipal.Builder()
                        .baseDn("testBaseDnGroupToPrincipal")
                        .cache(new LdapCache.Builder()
                                .bySearchTime()
                                .cacheFailures(false)
                                .evictionTime(2)
                                .maxCacheSize(10)
                                .build())
                        .build())
                .build();

        assertXmlIdentical(REALM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLdapAuthorization);
        assertXmlIdentical(REALM_WITH_GROUP_TO_PRINCIPAL_WITH_CACHE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void add_advancedFilter_withCache() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder("creaperSecRealm")
                .connection("creaperLdapConnection")
                .advancedFilter(new AdvancedFilter.Builder()
                        .baseDn("testBaseDnAdvancedFilter")
                        .filter("testFilterAdvancedFilter")
                        .cache(new LdapCache.Builder()
                                .byAccessTime()
                                .cacheFailures(true)
                                .evictionTime(2)
                                .maxCacheSize(10)
                                .build())
                        .build())
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .build();

        assertXmlIdentical(REALM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLdapAuthorization);
        assertXmlIdentical(REALM_WITH_ADVANCED_FILTER_WITH_CACHE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void add_usernameFilter_withCache() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder("creaperSecRealm")
                .connection("creaperLdapConnection")
                .usernameFilter(new UsernameFilter.Builder()
                        .baseDn("testBaseDnUsernameFilter")
                        .attribute("testAttributeUsernameFilter")
                        .cache(new LdapCache.Builder()
                                .bySearchTime()
                                .cacheFailures(false)
                                .evictionTime(3)
                                .maxCacheSize(5)
                                .build())
                        .build())
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .build();

        assertXmlIdentical(REALM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLdapAuthorization);
        assertXmlIdentical(REALM_WITH_USERNAME_FILTER_WITH_CACHE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void add_usernameIsDn_withCache() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder("creaperSecRealm")
                .connection("creaperLdapConnection")
                .usernameIsDn(new UsernameIsDn.Builder()
                        .cache(new LdapCache.Builder()
                                .byAccessTime()
                                .cacheFailures(true)
                                .evictionTime(3)
                                .maxCacheSize(5)
                                .build())
                        .build())
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .build();

        assertXmlIdentical(REALM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLdapAuthorization);
        assertXmlIdentical(REALM_WITH_USERNAME_IS_DN_WITH_CACHE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void addAsSecondAuthz() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_WITH_PROPERTIES_AUTHZ, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder("creaperSecRealm")
                .connection("creaperLdapConnection")
                .groupToPrincipal(new GroupToPrincipal.Builder()
                        .baseDn("testBaseDnGroupToPrincipalReplaced")
                        .build())
                .replaceExisting()
                .build();

        assertXmlIdentical(REALM_WITH_PROPERTIES_AUTHZ, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLdapAuthorization);

        fail("Authorization configuration already exists in configuration of creaperSecRealm, exception should be thrown");
    }

    @Test
    public void add_toMapGroupsToRoles() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_WITH_MAP_GROUPS_TO_ROLES, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapAuthorization addLdapAuthorization = new AddLdapAuthorization.Builder("creaperSecRealm")
                .connection("creaperLdapConnection")
                .principalToGroup(new PrincipalToGroup.Builder().build())
                .build();

        assertXmlIdentical(REALM_WITH_MAP_GROUPS_TO_ROLES, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLdapAuthorization);
        assertXmlIdentical(REALM_WITH_MAP_GROUPS_TO_ROLES_EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }

}
