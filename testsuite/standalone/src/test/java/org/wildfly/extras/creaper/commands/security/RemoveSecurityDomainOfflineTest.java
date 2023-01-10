package org.wildfly.extras.creaper.commands.security;

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

public class RemoveSecurityDomainOfflineTest {

    private static final String SUBSYSTEM_ORIGINAL = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:security:1.2\">\n"
            + "            <security-domains>\n"
            + "                <security-domain name=\"creaperSecDomain\">\n"
            + "                    <authentication>\n"
            + "                        <login-module code=\"RealmDirect\" flag=\"required\">\n"
            + "                            <module-option name=\"password-stacking\" value=\"useFirstPass\"/>\n"
            + "                        </login-module>\n"
            + "                    </authentication>\n"
            + "                </security-domain>\n"
            + "            </security-domains>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";
    private static final String SUBSYSTEM_EXPECTED = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:security:1.2\">\n"
            + "            <security-domains/>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Before
    public void setUp() {
        XmlAssert.setNormalizeWhitespace(true);
    }

    @Test
    public void removeExistingSecurityDomain() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_ORIGINAL, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        RemoveSecurityDomain removeSecurityDomain = new RemoveSecurityDomain("creaperSecDomain");

        assertXmlIdentical(SUBSYSTEM_ORIGINAL, Files.toString(cfg, Charsets.UTF_8));
        client.apply(removeSecurityDomain);
        assertXmlIdentical(SUBSYSTEM_EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void removeNonExistingSecurityDomain() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_ORIGINAL, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        RemoveSecurityDomain removeSecurityDomain = new RemoveSecurityDomain("wrongCreaperSecDomain");

        assertXmlIdentical(SUBSYSTEM_ORIGINAL, Files.toString(cfg, Charsets.UTF_8));
        client.apply(removeSecurityDomain);

        fail("Security domain wrongCreaperSecDomain does not exist in configuration, exception should be thrown");
    }
}
