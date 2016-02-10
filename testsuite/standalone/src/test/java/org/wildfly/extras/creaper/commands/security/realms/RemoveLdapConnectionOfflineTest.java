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

public class RemoveLdapConnectionOfflineTest {

    private static final String ONE_LDAP_CONNECTION = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <outbound-connections>\n"
            + "            <ldap name=\"creaperLdapConnection\" url=\"ldap://localhost:389\"/>\n"
            + "        </outbound-connections>\n"
            + "    </management>\n"
            + "</server>";
    private static final String TWO_LDAP_CONNECTION = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <outbound-connections>\n"
            + "            <ldap name=\"creaperLdapConnection\" url=\"ldap://localhost:389\"/>\n"
            + "            <ldap name=\"creaperLdapConnection2\" url=\"ldap://localhost:10389\"/>\n"
            + "        </outbound-connections>\n"
            + "    </management>\n"
            + "</server>";
    private static final String NO_LDAP_CONNECTION = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management/>\n"
            + "</server>";

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Before
    public void setUp() {
        XMLUnit.setNormalizeWhitespace(true);
    }

    @Test
    public void removeExistingLdapConnection() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(TWO_LDAP_CONNECTION, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        RemoveLdapConnection removeLdapConnection = new RemoveLdapConnection("creaperLdapConnection2");

        assertXmlIdentical(TWO_LDAP_CONNECTION, Files.toString(cfg, Charsets.UTF_8));
        client.apply(removeLdapConnection);
        assertXmlIdentical(ONE_LDAP_CONNECTION, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void removeLastExistingLdapConnection() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(ONE_LDAP_CONNECTION, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        RemoveLdapConnection removeLdapConnection = new RemoveLdapConnection("creaperLdapConnection");

        assertXmlIdentical(ONE_LDAP_CONNECTION, Files.toString(cfg, Charsets.UTF_8));
        client.apply(removeLdapConnection);
        assertXmlIdentical(NO_LDAP_CONNECTION, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void removeNonExistingLdapConnection() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(ONE_LDAP_CONNECTION, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        RemoveLdapConnection removeLdapConnection = new RemoveLdapConnection("wrongCreaperLdapConnection");

        assertXmlIdentical(ONE_LDAP_CONNECTION, Files.toString(cfg, Charsets.UTF_8));
        client.apply(removeLdapConnection);

        fail("Ldap connection wrongCreaperLdapConnection does not exist in configuration, exception should be thrown");
    }

}
