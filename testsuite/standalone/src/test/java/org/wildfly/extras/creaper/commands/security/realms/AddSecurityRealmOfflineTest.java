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

public class AddSecurityRealmOfflineTest {

    private static final String REALM_SIMPLE = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\"/>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_EMPTY = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <security-realms/>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_EXPECTED_REPLACE = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authorization map-groups-to-roles=\"true\"/>"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_SECOND_REALM = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\"/>\n"
            + "            <security-realm name=\"creaperSecRealm2\"/>\n"
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
        Files.write(REALM_EMPTY, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddSecurityRealm addSecurityRealm = new AddSecurityRealm.Builder("creaperSecRealm").build();

        assertXmlIdentical(REALM_EMPTY, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addSecurityRealm);
        assertXmlIdentical(REALM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void existing() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddSecurityRealm addSecurityRealm = new AddSecurityRealm.Builder("creaperSecRealm").build();

        assertXmlIdentical(REALM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addSecurityRealm);

        fail("Security realm creaperSecRealm already exists in configuration, exception should be thrown");
    }

    @Test
    public void overrideExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddSecurityRealm addSecurityRealm = new AddSecurityRealm.Builder("creaperSecRealm")
                .mapGroupsToRoles(true)
                .replaceExisting()
                .build();

        assertXmlIdentical(REALM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addSecurityRealm);
        assertXmlIdentical(REALM_EXPECTED_REPLACE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addSecond() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddSecurityRealm addSecurityRealm = new AddSecurityRealm.Builder("creaperSecRealm2")
                .build();

        assertXmlIdentical(REALM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addSecurityRealm);
        assertXmlIdentical(REALM_SECOND_REALM, Files.toString(cfg, Charsets.UTF_8));
    }
}
