package org.wildfly.extras.creaper.commands.security;

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

public class RemoveMappingModuleOfflineTest {

    private static final String SECURITY_DOMAIN_WITH_TWO_MAPPING_MODULES = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:security:1.2\">\n"
            + "            <security-domains>\n"
            + "                <security-domain name=\"creaperSecDomain\">\n"
            + "                    <mapping>\n"
            + "                        <mapping-module code=\"SimpleRoles\" type=\"role\">\n"
            + "                            <module-option name=\"joe\" value=\"Users\"/>\n"
            + "                        </mapping-module>\n"
            + "                        <mapping-module code=\"PropertiesRoles\" type=\"role\">\n"
            + "                            <module-option name=\"rolesProperties\" value=\"roles.properties\"/>\n"
            + "                        </mapping-module>\n"
            + "                    </mapping>\n"
            + "                </security-domain>\n"
            + "            </security-domains>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";
    private static final String SECURITY_DOMAIN_WITH_ONE_MAPPING_MODULE = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:security:1.2\">\n"
            + "            <security-domains>\n"
            + "                <security-domain name=\"creaperSecDomain\">\n"
            + "                    <mapping>\n"
            + "                        <mapping-module code=\"SimpleRoles\" type=\"role\">\n"
            + "                            <module-option name=\"joe\" value=\"Users\"/>\n"
            + "                        </mapping-module>\n"
            + "                    </mapping>\n"
            + "                </security-domain>\n"
            + "            </security-domains>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";
    private static final String SECURITY_DOMAIN_WITHOUT_MAPPING_MODULE = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:security:1.2\">\n"
            + "            <security-domains>\n"
            + "                <security-domain name=\"creaperSecDomain\"/>\n"
            + "            </security-domains>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SECURITY_DOMAIN_WITH_TWO_MAPPING_MODULE_NAMED = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:security:1.2\">\n"
            + "            <security-domains>\n"
            + "                <security-domain name=\"creaperSecDomain\">\n"
            + "                    <mapping>\n"
            + "                        <mapping-module code=\"SimpleRoles\" type=\"role\">\n"
            + "                            <module-option name=\"joe\" value=\"Users\"/>\n"
            + "                        </mapping-module>\n"
            + "                        <mapping-module name=\"NamedMappingModule\" code=\"PropertiesRoles\" "
            + "type=\"role\">\n"
            + "                            <module-option name=\"rolesProperties\" value=\"roles.properties\"/>\n"
            + "                        </mapping-module>\n"
            + "                    </mapping>\n"
            + "                </security-domain>\n"
            + "            </security-domains>\n"
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
    public void removeOneOfMoreMappingModules() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SECURITY_DOMAIN_WITH_TWO_MAPPING_MODULES, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        RemoveMappingModule removeSecurityDomain = new RemoveMappingModule("creaperSecDomain", "PropertiesRoles");

        assertXmlIdentical(SECURITY_DOMAIN_WITH_TWO_MAPPING_MODULES, Files.toString(cfg, Charsets.UTF_8));
        client.apply(removeSecurityDomain);
        assertXmlIdentical(SECURITY_DOMAIN_WITH_ONE_MAPPING_MODULE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void removeLastMappingModule() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SECURITY_DOMAIN_WITH_ONE_MAPPING_MODULE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        RemoveMappingModule removeSecurityDomain = new RemoveMappingModule("creaperSecDomain", "SimpleRoles");

        assertXmlIdentical(SECURITY_DOMAIN_WITH_ONE_MAPPING_MODULE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(removeSecurityDomain);
        assertXmlIdentical(SECURITY_DOMAIN_WITHOUT_MAPPING_MODULE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void removeNamedMappingModule() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SECURITY_DOMAIN_WITH_TWO_MAPPING_MODULE_NAMED, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        RemoveMappingModule removeSecurityDomain = new RemoveMappingModule("creaperSecDomain", "NamedMappingModule");

        assertXmlIdentical(SECURITY_DOMAIN_WITH_TWO_MAPPING_MODULE_NAMED, Files.toString(cfg, Charsets.UTF_8));
        client.apply(removeSecurityDomain);
        assertXmlIdentical(SECURITY_DOMAIN_WITH_ONE_MAPPING_MODULE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void removeNonExistingMappingModule() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SECURITY_DOMAIN_WITH_ONE_MAPPING_MODULE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        RemoveMappingModule removeSecurityDomain = new RemoveMappingModule("creaperSecDomain", "PropertiesRoles");

        assertXmlIdentical(SECURITY_DOMAIN_WITH_ONE_MAPPING_MODULE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(removeSecurityDomain);

        fail("Mapping module PropertiesRoles does not exist in configuration, exception should be thrown");
    }

    @Test(expected = CommandFailedException.class)
    public void doNotRemoveNamedMappingModuleByCodeReference() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SECURITY_DOMAIN_WITH_TWO_MAPPING_MODULE_NAMED, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        RemoveMappingModule removeSecurityDomain = new RemoveMappingModule("creaperSecDomain", "PropertiesRoles");

        assertXmlIdentical(SECURITY_DOMAIN_WITH_TWO_MAPPING_MODULE_NAMED, Files.toString(cfg, Charsets.UTF_8));
        client.apply(removeSecurityDomain);

        fail("Mapping module PropertiesRoles does not exist in configuration, exception should be thrown");
    }
}
