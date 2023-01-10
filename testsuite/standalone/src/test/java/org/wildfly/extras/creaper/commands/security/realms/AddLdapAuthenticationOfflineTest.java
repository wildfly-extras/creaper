package org.wildfly.extras.creaper.commands.security.realms;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wildfly.extras.creaper.XmlAssert;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;

import static org.junit.Assert.fail;
import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;

public class AddLdapAuthenticationOfflineTest {

    private static final String REALM_SIMPLE = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\"/>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_LDAP_USERNAME_ATTRIBUTE = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authentication>\n"
            + "                    <ldap connection=\"creaperLdapConnection\" base-dn=\"ou=People,dc=jboss,dc=org\">\n"
            + "                        <username-filter attribute=\"uid\"/>\n"
            + "                    </ldap>\n"
            + "                </authentication>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_LDAP_ADVANCED_FILTER = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authentication>\n"
            + "                    <ldap connection=\"creaperLdapConnection\" base-dn=\"ou=Person,dc=jboss,dc=org\">\n"
            + "                        <advanced-filter filter=\"(uid={0})\"/>\n"
            + "                    </ldap>\n"
            + "                </authentication>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_LDAP_USERNAME_ATTRIBUTE_REPLACED = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authentication>\n"
            + "                    <ldap connection=\"anotherLdapConnection\" base-dn=\"ou=Person,dc=jboss,dc=org\">\n"
            + "                        <username-filter attribute=\"cn\"/>\n"
            + "                    </ldap>\n"
            + "                </authentication>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_LDAP_USERNAME_ATTRIBUTE_FULL = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authentication>\n"
            + "                    <ldap connection=\"creaperLdapConnection\" base-dn=\"ou=People,dc=jboss,dc=org\" "
            + "allow-empty-passwords=\"true\" recursive=\"true\" user-dn=\"cn\" username-load=\"description\">\n"
            + "                        <username-filter attribute=\"uid\"/>\n"
            + "                    </ldap>\n"
            + "                </authentication>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_LDAP_ADVANCED_FILTER_FULL = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authentication>\n"
            + "                    <ldap connection=\"creaperLdapConnection\" base-dn=\"ou=Person,dc=jboss,dc=org\" "
            + "allow-empty-passwords=\"false\" recursive=\"false\" user-dn=\"uid\" username-load=\"sn\">\n"
            + "                        <advanced-filter filter=\"(uid={0})\"/>\n"
            + "                    </ldap>\n"
            + "                </authentication>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_LDAP_CACHE_BY_ACCESS_TIME = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authentication>\n"
            + "                    <ldap connection=\"creaperLdapConnection\" base-dn=\"ou=People,dc=jboss,dc=org\">\n"
            + "                        <cache type=\"by-access-time\" cache-failures=\"true\" eviction-time=\"3\" "
            + "max-cache-size=\"10\"/>\n"
            + "                        <username-filter attribute=\"uid\"/>\n"
            + "                    </ldap>\n"
            + "                </authentication>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_LDAP_CACHE_BY_SEARCH_TIME = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authentication>\n"
            + "                    <ldap connection=\"creaperLdapConnection\" base-dn=\"ou=People,dc=jboss,dc=org\">\n"
            + "                        <cache type=\"by-search-time\" cache-failures=\"false\" eviction-time=\"1\" "
            + "max-cache-size=\"5\"/>\n"
            + "                        <username-filter attribute=\"uid\"/>\n"
            + "                    </ldap>\n"
            + "                </authentication>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_LOCAL = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authentication>\n"
            + "                    <local default-user=\"$local\" skip-group-loading=\"true\"/>\n"
            + "                </authentication>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_LOCAL_EXPECTED = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authentication>\n"
            + "                    <local default-user=\"$local\" skip-group-loading=\"true\"/>\n"
            + "                    <ldap connection=\"creaperLdapConnection\" base-dn=\"ou=People,dc=jboss,dc=org\">\n"
            + "                        <username-filter attribute=\"uid\"/>\n"
            + "                    </ldap>\n"
            + "                </authentication>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_PROPERTIES = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authentication>\n"
            + "                    <properties path=\"users.properties\" relative-to=\"jboss.server.config.dir\"/>\n"
            + "                </authentication>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_JAAS = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authentication>\n"
            + "                    <jaas name=\"other\"/>\n"
            + "                </authentication>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Before
    public void setUp() {
        XmlAssert.setNormalizeWhitespace(true);
    }

    @Test
    public void addSimple_usernameAttribute() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapAuthentication addSecurityRealmLdapAuthentication
                = new AddLdapAuthentication.Builder("creaperSecRealm")
                .usernameAttribute("uid")
                .connection("creaperLdapConnection")
                .baseDn("ou=People,dc=jboss,dc=org")
                .build();

        assertXmlIdentical(REALM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addSecurityRealmLdapAuthentication);
        assertXmlIdentical(REALM_WITH_LDAP_USERNAME_ATTRIBUTE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addSimple_advancedFilter() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapAuthentication addSecurityRealmLdapAuthentication
                = new AddLdapAuthentication.Builder("creaperSecRealm")
                .advancedFilter("(uid={0})")
                .connection("creaperLdapConnection")
                .baseDn("ou=Person,dc=jboss,dc=org")
                .build();

        assertXmlIdentical(REALM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addSecurityRealmLdapAuthentication);
        assertXmlIdentical(REALM_WITH_LDAP_ADVANCED_FILTER, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void existing() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_WITH_LDAP_USERNAME_ATTRIBUTE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapAuthentication addSecurityRealmLdapAuthentication
                = new AddLdapAuthentication.Builder("creaperSecRealm")
                .usernameAttribute("cn")
                .connection("anotherLdapConnection")
                .baseDn("ou=Person,dc=jboss,dc=org")
                .build();

        assertXmlIdentical(REALM_WITH_LDAP_USERNAME_ATTRIBUTE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addSecurityRealmLdapAuthentication);

        fail("Ldap authentication already exists in configuration of creaperSecRealm, exception should be thrown");
    }

    @Test
    public void overrideExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_WITH_LDAP_USERNAME_ATTRIBUTE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapAuthentication addSecurityRealmLdapAuthentication
                = new AddLdapAuthentication.Builder("creaperSecRealm")
                .usernameAttribute("cn")
                .connection("anotherLdapConnection")
                .baseDn("ou=Person,dc=jboss,dc=org")
                .replaceExisting()
                .build();

        assertXmlIdentical(REALM_WITH_LDAP_USERNAME_ATTRIBUTE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addSecurityRealmLdapAuthentication);
        assertXmlIdentical(REALM_WITH_LDAP_USERNAME_ATTRIBUTE_REPLACED, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addFull_usernameAttribute() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapAuthentication addSecurityRealmLdapAuthentication
                = new AddLdapAuthentication.Builder("creaperSecRealm")
                .usernameAttribute("uid")
                .connection("creaperLdapConnection")
                .baseDn("ou=People,dc=jboss,dc=org")
                .allowEmptyPasswords(true)
                .recursive(true)
                .userDn("cn")
                .usernameLoad("description")
                .build();

        assertXmlIdentical(REALM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addSecurityRealmLdapAuthentication);
        assertXmlIdentical(REALM_WITH_LDAP_USERNAME_ATTRIBUTE_FULL, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addFull_advancedFilter() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapAuthentication addSecurityRealmLdapAuthentication
                = new AddLdapAuthentication.Builder("creaperSecRealm")
                .advancedFilter("(uid={0})")
                .connection("creaperLdapConnection")
                .baseDn("ou=Person,dc=jboss,dc=org")
                .allowEmptyPasswords(false)
                .recursive(false)
                .userDn("uid")
                .usernameLoad("sn")
                .build();

        assertXmlIdentical(REALM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addSecurityRealmLdapAuthentication);
        assertXmlIdentical(REALM_WITH_LDAP_ADVANCED_FILTER_FULL, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addWithCache_byAccessTime() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapAuthentication addSecurityRealmLdapAuthentication
                = new AddLdapAuthentication.Builder("creaperSecRealm")
                .usernameAttribute("uid")
                .connection("creaperLdapConnection")
                .baseDn("ou=People,dc=jboss,dc=org")
                .cache(new LdapCache.Builder()
                        .byAccessTime()
                        .cacheFailures(true)
                        .evictionTime(3)
                        .maxCacheSize(10)
                        .build())
                .build();

        assertXmlIdentical(REALM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addSecurityRealmLdapAuthentication);
        assertXmlIdentical(REALM_WITH_LDAP_CACHE_BY_ACCESS_TIME, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addWithCache_bySearchTime() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapAuthentication addSecurityRealmLdapAuthentication
                = new AddLdapAuthentication.Builder("creaperSecRealm")
                .usernameAttribute("uid")
                .connection("creaperLdapConnection")
                .baseDn("ou=People,dc=jboss,dc=org")
                .cache(new LdapCache.Builder()
                        .bySearchTime()
                        .cacheFailures(false)
                        .evictionTime(1)
                        .maxCacheSize(5)
                        .build())
                .build();

        assertXmlIdentical(REALM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addSecurityRealmLdapAuthentication);
        assertXmlIdentical(REALM_WITH_LDAP_CACHE_BY_SEARCH_TIME, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addAsSecondAuthnType() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_WITH_LOCAL, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapAuthentication addSecurityRealmLdapAuthentication
                = new AddLdapAuthentication.Builder("creaperSecRealm")
                .usernameAttribute("uid")
                .connection("creaperLdapConnection")
                .baseDn("ou=People,dc=jboss,dc=org")
                .build();

        assertXmlIdentical(REALM_WITH_LOCAL, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addSecurityRealmLdapAuthentication);
        assertXmlIdentical(REALM_WITH_LOCAL_EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void addSameTypeOfAuthentication_properties() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_WITH_PROPERTIES, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapAuthentication addSecurityRealmLdapAuthentication
                = new AddLdapAuthentication.Builder("creaperSecRealm")
                .usernameAttribute("uid")
                .connection("creaperLdapConnection")
                .baseDn("ou=People,dc=jboss,dc=org")
                .build();

        assertXmlIdentical(REALM_WITH_PROPERTIES, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addSecurityRealmLdapAuthentication);
        fail("Username/password based authentication already exists in configuration of creaperSecRealm, exception should be thrown");
    }

    @Test(expected = CommandFailedException.class)
    public void addSameTypeOfAuthentication_jaas() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_WITH_JAAS, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapAuthentication addSecurityRealmLdapAuthentication
                = new AddLdapAuthentication.Builder("creaperSecRealm")
                .usernameAttribute("uid")
                .connection("creaperLdapConnection")
                .baseDn("ou=People,dc=jboss,dc=org")
                .build();

        assertXmlIdentical(REALM_WITH_JAAS, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addSecurityRealmLdapAuthentication);
        fail("Username/password based authentication already exists in configuration of creaperSecRealm, exception should be thrown");
    }

}
