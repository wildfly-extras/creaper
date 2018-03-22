package org.wildfly.extras.creaper.commands.undertow;

import static org.junit.Assert.fail;
import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;
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

public class AddApplicationSecurityDomainOfflineTest {

    private static final String SUBSYSTEM_EMPTY = ""
            + "<server xmlns=\"urn:jboss:domain:5.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:undertow:4.0\">\n"
            + "            <server name=\"default-server\">\n"
            + "                <host name=\"default-host\"/>\n"
            + "            </server>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_DOMAINS_EMPTY = ""
            + "<server xmlns=\"urn:jboss:domain:5.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:undertow:4.0\">\n"
            + "            <server name=\"default-server\">\n"
            + "                <host name=\"default-host\"/>\n"
            + "            </server>\n"
            + "            <application-security-domains>\n"
            + "            </application-security-domains>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_SIMPLE = ""
            + "<server xmlns=\"urn:jboss:domain:5.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:undertow:4.0\">\n"
            + "            <server name=\"default-server\">\n"
            + "                <host name=\"default-host\"/>\n"
            + "            </server>\n"
            + "            <application-security-domains>\n"
            + "                <application-security-domain name=\"appSecDomain\" http-authentication-factory=\"httpAuthFactory\"/>\n"
            + "            </application-security-domains>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_EXPECTED_REPLACE = ""
            + "<server xmlns=\"urn:jboss:domain:5.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:undertow:4.0\">\n"
            + "            <server name=\"default-server\">\n"
            + "                <host name=\"default-host\"/>\n"
            + "            </server>\n"
            + "            <application-security-domains>\n"
            + "                <application-security-domain name=\"appSecDomain\" http-authentication-factory=\"httpAuthFactory2\"/>\n"
            + "            </application-security-domains>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_SECOND_DOMAIN = ""
            + "<server xmlns=\"urn:jboss:domain:5.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:undertow:4.0\">\n"
            + "            <server name=\"default-server\">\n"
            + "                <host name=\"default-host\"/>\n"
            + "            </server>\n"
            + "            <application-security-domains>\n"
            + "                <application-security-domain name=\"appSecDomain\" http-authentication-factory=\"httpAuthFactory\"/>\n"
            + "                <application-security-domain name=\"appSecDomain2\" http-authentication-factory=\"httpAuthFactory\"/>\n"
            + "            </application-security-domains>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_FULL = ""
            + "<server xmlns=\"urn:jboss:domain:5.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:undertow:4.0\">\n"
            + "            <server name=\"default-server\">\n"
            + "                <host name=\"default-host\"/>\n"
            + "            </server>\n"
            + "            <application-security-domains>\n"
            + "                <application-security-domain name=\"appSecDomain\""
            + "                                             http-authentication-factory=\"httpAuthFactory\""
            + "                                             override-deployment-config=\"true\"/>\n"
            + "            </application-security-domains>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Before
    public void setUp() {
        XMLUnit.setNormalizeWhitespace(true);
    }

    @Test
    public void addSimpleToEmpty() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_EMPTY, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddApplicationSecurityDomain addApplicationSecurityDomain = new AddApplicationSecurityDomain
                .Builder("appSecDomain")
                .httpAuthenticationFactory("httpAuthFactory")
                .build();

        assertXmlIdentical(SUBSYSTEM_EMPTY, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addApplicationSecurityDomain);
        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addSimpleToAppSecDomainsEmpty() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_DOMAINS_EMPTY, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddApplicationSecurityDomain addApplicationSecurityDomain = new AddApplicationSecurityDomain
                .Builder("appSecDomain")
                .httpAuthenticationFactory("httpAuthFactory")
                .build();

        assertXmlIdentical(SUBSYSTEM_DOMAINS_EMPTY, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addApplicationSecurityDomain);
        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void existing() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddApplicationSecurityDomain addApplicationSecurityDomain = new AddApplicationSecurityDomain
                .Builder("appSecDomain")
                .httpAuthenticationFactory("httpAuthFactory")
                .build();

        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addApplicationSecurityDomain);

        fail("Application security domain appSecDomain already exists in configuration, exception should be thrown");
    }

    @Test
    public void overrideExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddApplicationSecurityDomain addApplicationSecurityDomain = new AddApplicationSecurityDomain
                .Builder("appSecDomain")
                .httpAuthenticationFactory("httpAuthFactory2")
                .replaceExisting()
                .build();

        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addApplicationSecurityDomain);
        assertXmlIdentical(SUBSYSTEM_EXPECTED_REPLACE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void overrideNonExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddApplicationSecurityDomain addApplicationSecurityDomain = new AddApplicationSecurityDomain
                .Builder("appSecDomain2")
                .httpAuthenticationFactory("httpAuthFactory")
                .replaceExisting()
                .build();

        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addApplicationSecurityDomain);
        assertXmlIdentical(SUBSYSTEM_SECOND_DOMAIN, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addSecondKeyStore() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddApplicationSecurityDomain addApplicationSecurityDomain = new AddApplicationSecurityDomain
                .Builder("appSecDomain2")
                .httpAuthenticationFactory("httpAuthFactory")
                .build();

        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addApplicationSecurityDomain);
        assertXmlIdentical(SUBSYSTEM_SECOND_DOMAIN, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addFullToEmpty() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_EMPTY, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddApplicationSecurityDomain addApplicationSecurityDomain = new AddApplicationSecurityDomain
                .Builder("appSecDomain")
                .httpAuthenticationFactory("httpAuthFactory")
                .overrideDeploymentConfig(true)
                .build();

        assertXmlIdentical(SUBSYSTEM_EMPTY, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addApplicationSecurityDomain);
        assertXmlIdentical(SUBSYSTEM_FULL, Files.toString(cfg, Charsets.UTF_8));
    }
}
