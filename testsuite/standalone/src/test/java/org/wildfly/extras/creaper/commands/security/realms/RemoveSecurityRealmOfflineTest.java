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

public class RemoveSecurityRealmOfflineTest {

    private static final String ONE_REALM = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authentication>\n"
            + "                    <local/>\n"
            + "                </authentication>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";
    private static final String TWO_REALMS = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authentication>\n"
            + "                    <local/>\n"
            + "                </authentication>\n"
            + "            </security-realm>\n"
            + "            <security-realm name=\"creaperSecRealm2\">\n"
            + "                <authentication>\n"
            + "                    <local/>\n"
            + "                </authentication>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";
    private static final String REALM_EMPTY = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <security-realms/>\n"
            + "    </management>\n"
            + "</server>";

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Before
    public void setUp() {
        XMLUnit.setNormalizeWhitespace(true);
    }

    @Test
    public void removeExistingSecurityRealm() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(TWO_REALMS, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        RemoveSecurityRealm removeSecurityRealm = new RemoveSecurityRealm("creaperSecRealm2");

        assertXmlIdentical(TWO_REALMS, Files.toString(cfg, Charsets.UTF_8));
        client.apply(removeSecurityRealm);
        assertXmlIdentical(ONE_REALM, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void removeLastExistingSecurityRealm() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(ONE_REALM, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        RemoveSecurityRealm removeSecurityRealm = new RemoveSecurityRealm("creaperSecRealm");

        assertXmlIdentical(ONE_REALM, Files.toString(cfg, Charsets.UTF_8));
        client.apply(removeSecurityRealm);
        assertXmlIdentical(REALM_EMPTY, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void removeNonExistingSecurityRealm() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(ONE_REALM, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        RemoveSecurityRealm removeSecurityRealm = new RemoveSecurityRealm("wrongCreaperSecRealm");

        assertXmlIdentical(ONE_REALM, Files.toString(cfg, Charsets.UTF_8));
        client.apply(removeSecurityRealm);

        fail("Security realm wrongCreaperSecRealm does not exist in configuration, exception should be thrown");
    }
}
