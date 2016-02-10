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

public class AddLdapConnectionOfflineTest {

    private static final String CONNECTION_SIMPLE = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <outbound-connections>\n"
            + "            <ldap name=\"creaperLdapConnection\" url=\"ldap://localhost:389\"/>\n"
            + "        </outbound-connections>\n"
            + "    </management>\n"
            + "</server>";

    private static final String CONNECTION_EMPTY = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management/>\n"
            + "</server>";

    private static final String CONNECTION_EXPECTED_REPLACE = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <outbound-connections>\n"
            + "            <ldap name=\"creaperLdapConnection\" url=\"ldap://localhost:10389\"/>\n"
            + "        </outbound-connections>\n"
            + "    </management>\n"
            + "</server>";

    private static final String CONNECTION_SECOND = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <outbound-connections>\n"
            + "            <ldap name=\"creaperLdapConnection\" url=\"ldap://localhost:389\"/>\n"
            + "            <ldap name=\"creaperLdapConnection2\" url=\"ldap://localhost:10389\"/>\n"
            + "        </outbound-connections>\n"
            + "    </management>\n"
            + "</server>";

    private static final String CONNECTION_FULL = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <outbound-connections>\n"
            + "            <ldap name=\"creaperLdapConnection\" initial-context-factory=\"ContextFactory\" "
            + "referrals=\"THROW\" search-credential=\"secret\" search-dn=\"uid=admin,ou=system\" "
            + "security-realm=\"ManagementRealm\" url=\"ldap://localhost:389\" "
            + "handles-referrals-for=\"localhost 127.0.0.1\">\n"
            + "                <properties>\n"
            + "                    <property name=\"propertyA\" value=\"valueA\"/>\n"
            + "                    <property name=\"propertyB\" value=\"valueB\"/>\n"
            + "                </properties>\n"
            + "            </ldap>\n"
            + "        </outbound-connections>\n"
            + "    </management>\n"
            + "</server>";

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Before
    public void setUp() {
        XMLUnit.setNormalizeWhitespace(true);
    }

    @Test
    public void addSimple() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(CONNECTION_EMPTY, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapConnection addLdapConnection = new AddLdapConnection.Builder("creaperLdapConnection")
                .url("ldap://localhost:389")
                .build();

        assertXmlIdentical(CONNECTION_EMPTY, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLdapConnection);
        assertXmlIdentical(CONNECTION_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void existing() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(CONNECTION_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapConnection addLdapConnection = new AddLdapConnection.Builder("creaperLdapConnection")
                .url("ldap://localhost:10389")
                .build();

        assertXmlIdentical(CONNECTION_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLdapConnection);

        fail("Ldap outbound connection creaperLdapConnection already exists in configuration, exception should be thrown");
    }

    @Test
    public void overrideExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(CONNECTION_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapConnection addLdapConnection = new AddLdapConnection.Builder("creaperLdapConnection")
                .url("ldap://localhost:10389")
                .replaceExisting()
                .build();

        assertXmlIdentical(CONNECTION_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLdapConnection);
        assertXmlIdentical(CONNECTION_EXPECTED_REPLACE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addSecond() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(CONNECTION_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapConnection addLdapConnection = new AddLdapConnection.Builder("creaperLdapConnection2")
                .url("ldap://localhost:10389")
                .build();

        assertXmlIdentical(CONNECTION_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLdapConnection);
        assertXmlIdentical(CONNECTION_SECOND, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addFull() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(CONNECTION_EMPTY, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapConnection addLdapConnection = new AddLdapConnection.Builder("creaperLdapConnection")
                .url("ldap://localhost:389")
                .searchDn("uid=admin,ou=system")
                .searchCredential("secret")
                .initialContextFactory("ContextFactory")
                .referrals("THROW")
                .securityRealm("ManagementRealm")
                .addHandlesReferralsFor("localhost")
                .addHandlesReferralsFor("127.0.0.1")
                .addProperty("propertyA", "valueA")
                .addProperty("propertyB", "valueB")
                .build();

        assertXmlIdentical(CONNECTION_EMPTY, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLdapConnection);
        assertXmlIdentical(CONNECTION_FULL, Files.toString(cfg, Charsets.UTF_8));
    }
}
