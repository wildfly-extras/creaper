package org.wildfly.extras.creaper.commands.security;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wildfly.extras.creaper.XmlAssert;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;
import org.wildfly.extras.creaper.core.CommandFailedException;

import static org.junit.Assert.fail;
import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;

public class AddSecurityDomainOfflineTest {

    private static final String SUBSYSTEM_SIMPLE = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:security:1.2\">\n"
            + "            <security-domains>\n"
            + "                <security-domain name=\"creaperSecDomain\"/>\n"
            + "            </security-domains>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_EMPTY = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:security:1.2\">\n"
            + "            <security-domains/>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_EXPECTED_REPLACE = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:security:1.2\">\n"
            + "            <security-domains>\n"
            + "                <security-domain name=\"creaperSecDomain\" cache-type=\"default\"/>\n"
            + "            </security-domains>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_SECOND_DOMAIN = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:security:1.2\">\n"
            + "            <security-domains>\n"
            + "                <security-domain name=\"creaperSecDomain\"/>\n"
            + "                <security-domain name=\"creaperSecDomain2\"/>\n"
            + "            </security-domains>\n"
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
    public void addSimple() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_EMPTY, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddSecurityDomain addSecurityDomain = new AddSecurityDomain.Builder("creaperSecDomain").build();

        assertXmlIdentical(SUBSYSTEM_EMPTY, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addSecurityDomain);
        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void existing() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddSecurityDomain addSecurityDomain = new AddSecurityDomain.Builder("creaperSecDomain").build();

        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addSecurityDomain);

        fail("Security domain creaperSecDomain already exists in configuration, exception should be thrown");
    }

    @Test
    public void overrideExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddSecurityDomain addSecurityDomain = new AddSecurityDomain.Builder("creaperSecDomain")
                .cacheType("default")
                .replaceExisting()
                .build();

        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addSecurityDomain);
        assertXmlIdentical(SUBSYSTEM_EXPECTED_REPLACE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void overrideNonExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddSecurityDomain addSecurityDomain = new AddSecurityDomain.Builder("creaperSecDomain2")
                .replaceExisting()
                .build();

        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addSecurityDomain);
        assertXmlIdentical(SUBSYSTEM_SECOND_DOMAIN, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addSecondSecurityDomain() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddSecurityDomain addSecurityDomain = new AddSecurityDomain.Builder("creaperSecDomain2")
                .build();

        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addSecurityDomain);
        assertXmlIdentical(SUBSYSTEM_SECOND_DOMAIN, Files.toString(cfg, Charsets.UTF_8));
    }
}
