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

public class AddPropertiesAuthorizationOfflineTest {

    private static final String REALM_SIMPLE = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\"/>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_PROPERTIES = ""
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

    private static final String REALM_WITH_PROPERTIES_REPLACED = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authorization>\n"
            + "                    <properties path=\"users2.properties\"/>\n"
            + "                </authorization>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_PROPERTIES_FULL = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authorization>\n"
            + "                    <properties path=\"users.properties\" relative-to=\"jboss.server.config.dir\"/>\n"
            + "                </authorization>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_MAP_GROUP_TO_ROLES = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authorization map-groups-to-roles=\"true\"/>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_MAP_GROUP_TO_ROLES_EXPECTED = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authorization map-groups-to-roles=\"true\">\n"
            + "                    <properties path=\"users.properties\"/>\n"
            + "                </authorization>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_LDAP = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authorization>\n"
            + "                    <ldap connection=\"ldap-connection\"/>\n"
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
    public void addSimple() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddPropertiesAuthorization addPropertiesAuthorization
                = new AddPropertiesAuthorization.Builder("creaperSecRealm")
                .path("users.properties")
                .build();

        assertXmlIdentical(REALM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addPropertiesAuthorization);
        assertXmlIdentical(REALM_WITH_PROPERTIES, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void existing() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_WITH_PROPERTIES, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddPropertiesAuthorization addPropertiesAuthorization
                = new AddPropertiesAuthorization.Builder("creaperSecRealm")
                .path("users2.properties")
                .build();

        assertXmlIdentical(REALM_WITH_PROPERTIES, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addPropertiesAuthorization);

        fail("Properties authorization already exists in configuration of creaperSecRealm, exception should be thrown");
    }

    @Test
    public void overrideExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_WITH_PROPERTIES, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddPropertiesAuthorization addPropertiesAuthorization
                = new AddPropertiesAuthorization.Builder("creaperSecRealm")
                .path("users2.properties")
                .replaceExisting()
                .build();

        assertXmlIdentical(REALM_WITH_PROPERTIES, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addPropertiesAuthorization);
        assertXmlIdentical(REALM_WITH_PROPERTIES_REPLACED, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addFull() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddPropertiesAuthorization addPropertiesAuthorization
                = new AddPropertiesAuthorization.Builder("creaperSecRealm")
                .path("users.properties")
                .relativeTo("jboss.server.config.dir")
                .build();

        assertXmlIdentical(REALM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addPropertiesAuthorization);
        assertXmlIdentical(REALM_WITH_PROPERTIES_FULL, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addToMapGroupToRoles() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_MAP_GROUP_TO_ROLES, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddPropertiesAuthorization addPropertiesAuthorization
                = new AddPropertiesAuthorization.Builder("creaperSecRealm")
                .path("users.properties")
                .build();

        assertXmlIdentical(REALM_MAP_GROUP_TO_ROLES, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addPropertiesAuthorization);
        assertXmlIdentical(REALM_MAP_GROUP_TO_ROLES_EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void addAsSecondAuthorizationType() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_WITH_LDAP, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddPropertiesAuthorization addPropertiesAuthorization
                = new AddPropertiesAuthorization.Builder("creaperSecRealm")
                .path("users.properties")
                .build();

        assertXmlIdentical(REALM_WITH_LDAP, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addPropertiesAuthorization);

        fail("Authorization configuration already exists in creaperSecRealm security domain, exception should be thrown");
    }

}
