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

public class AddMappingModuleOfflineTest {

    private static final String TEST_SECURITY_DOMAIN_NAME = "creaperSecDomain";

    private static final String SUBSYSTEM_ORIGINAL = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:security:1.2\">\n"
            + "            <security-domains>\n"
            + "                <security-domain name=\"creaperSecDomain\"/>\n"
            + "            </security-domains>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_WITH_ONE_MAPPING_MODULE = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:security:1.2\">\n"
            + "            <security-domains>\n"
            + "                <security-domain name=\"creaperSecDomain\">\n"
            + "                    <mapping>\n"
            + "                        <mapping-module name=\"PropertiesRoles\" code=\"PropertiesRoles\" "
            + "type=\"role\"/>\n"
            + "                    </mapping>\n"
            + "                </security-domain>\n"
            + "            </security-domains>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_WITH_ONE_MAPPING_MODULE_WITHOUT_NAME = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:security:1.2\">\n"
            + "            <security-domains>\n"
            + "                <security-domain name=\"creaperSecDomain\">\n"
            + "                    <mapping>\n"
            + "                        <mapping-module code=\"PropertiesRoles\" type=\"role\"/>\n"
            + "                    </mapping>\n"
            + "                </security-domain>\n"
            + "            </security-domains>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_WITH_TWO_MAPPING_MODULES = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:security:1.2\">\n"
            + "            <security-domains>\n"
            + "                <security-domain name=\"creaperSecDomain\">\n"
            + "                    <mapping>\n"
            + "                        <mapping-module name=\"PropertiesRoles\" code=\"PropertiesRoles\" "
            + "type=\"role\"/>\n"
            + "                        <mapping-module name=\"CreaperModule\" code=\"PropertiesRoles\" "
            + "type=\"role\"/>\n"
            + "                    </mapping>\n"
            + "                </security-domain>\n"
            + "            </security-domains>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_WITH_TWO_MAPPING_MODULES_UNNAMED = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:security:1.2\">\n"
            + "            <security-domains>\n"
            + "                <security-domain name=\"creaperSecDomain\">\n"
            + "                    <mapping>\n"
            + "                        <mapping-module name=\"CreaperModule\" code=\"PropertiesRoles\" "
            + "type=\"role\"/>\n"
            + "                        <mapping-module code=\"PropertiesRoles\" type=\"role\"/>\n"
            + "                    </mapping>\n"
            + "                </security-domain>\n"
            + "            </security-domains>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_REPLACED_EXPECTED = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:security:1.2\">\n"
            + "            <security-domains>\n"
            + "                <security-domain name=\"creaperSecDomain\">\n"
            + "                    <mapping>\n"
            + "                        <mapping-module name=\"PropertiesRoles\" code=\"PropertiesRoles\" "
            + "type=\"attribute\"/>\n"
            + "                    </mapping>\n"
            + "                </security-domain>\n"
            + "            </security-domains>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_FULL_EXPECTED = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:security:1.2\">\n"
            + "            <security-domains>\n"
            + "                <security-domain name=\"creaperSecDomain\">\n"
            + "                    <mapping>\n"
            + "                        <mapping-module name=\"CreaperModule\" code=\"PropertiesRoles\" type=\"role\" "
            + "module=\"org.picketbox\">\n"
            + "                            <module-option name=\"rolesProperties\" value=\"roles.properties\"/>\n"
            + "                        </mapping-module>\n"
            + "                    </mapping>\n"
            + "                </security-domain>\n"
            + "            </security-domains>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_WITH_TWO_MAPPING_MODULES_UNNAMED_REPLACE_EXPECTED = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:security:1.2\">\n"
            + "            <security-domains>\n"
            + "                <security-domain name=\"creaperSecDomain\">\n"
            + "                    <mapping>\n"
            + "                        <mapping-module name=\"CreaperModule\" code=\"PropertiesRoles\" "
            + "type=\"role\"/>\n"
            + "                        <mapping-module name=\"PropertiesRoles\" code=\"PropertiesRoles\" "
            + "type=\"attribute\"/>\n"
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
        XmlAssert.setNormalizeWhitespace(true);
    }

    @Test
    public void addSimple() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_ORIGINAL, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddMappingModule addMappingModule = new AddMappingModule.Builder("PropertiesRoles")
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .type("role")
                .build();

        assertXmlIdentical(SUBSYSTEM_ORIGINAL, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addMappingModule);
        assertXmlIdentical(SUBSYSTEM_WITH_ONE_MAPPING_MODULE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addSecondSimple() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_WITH_ONE_MAPPING_MODULE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddMappingModule addMappingModule = new AddMappingModule.Builder("PropertiesRoles", "CreaperModule")
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .type("role")
                .build();

        assertXmlIdentical(SUBSYSTEM_WITH_ONE_MAPPING_MODULE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addMappingModule);
        assertXmlIdentical(SUBSYSTEM_WITH_TWO_MAPPING_MODULES, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void existing() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_WITH_ONE_MAPPING_MODULE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddMappingModule addMappingModule = new AddMappingModule.Builder("PropertiesRoles")
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .type("attribute")
                .build();

        assertXmlIdentical(SUBSYSTEM_WITH_ONE_MAPPING_MODULE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addMappingModule);

        fail("Login module already exists in configuration, exception should be thrown");
    }

    @Test
    public void overrideExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_WITH_ONE_MAPPING_MODULE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddMappingModule addMappingModule = new AddMappingModule.Builder("PropertiesRoles")
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .type("attribute")
                .replaceExisting()
                .build();

        assertXmlIdentical(SUBSYSTEM_WITH_ONE_MAPPING_MODULE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addMappingModule);
        assertXmlIdentical(SUBSYSTEM_REPLACED_EXPECTED, Files.toString(cfg, Charsets.UTF_8));

    }

    @Test(expected = CommandFailedException.class)
    public void existingWithoutName() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_WITH_ONE_MAPPING_MODULE_WITHOUT_NAME, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddMappingModule addMappingModule = new AddMappingModule.Builder("PropertiesRoles")
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .type("attribute")
                .build();

        assertXmlIdentical(SUBSYSTEM_WITH_ONE_MAPPING_MODULE_WITHOUT_NAME, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addMappingModule);

        fail("Login module already exists in configuration, exception should be thrown");

    }

    @Test
    public void overrideExistingWithoutName() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_WITH_ONE_MAPPING_MODULE_WITHOUT_NAME, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddMappingModule addMappingModule = new AddMappingModule.Builder("PropertiesRoles")
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .type("attribute")
                .replaceExisting()
                .build();

        assertXmlIdentical(SUBSYSTEM_WITH_ONE_MAPPING_MODULE_WITHOUT_NAME, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addMappingModule);
        assertXmlIdentical(SUBSYSTEM_REPLACED_EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void existingWithoutNameTwoLoginModules() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_WITH_TWO_MAPPING_MODULES_UNNAMED, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddMappingModule addMappingModule = new AddMappingModule.Builder("PropertiesRoles")
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .type("attribute")
                .build();

        assertXmlIdentical(SUBSYSTEM_WITH_TWO_MAPPING_MODULES_UNNAMED, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addMappingModule);

        fail("Login module already exists in configuration, exception should be thrown");

    }

    @Test
    public void overrideExistingWithoutNameTwoLoginModules() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_WITH_TWO_MAPPING_MODULES_UNNAMED, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddMappingModule addMappingModule = new AddMappingModule.Builder("PropertiesRoles")
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .type("attribute")
                .replaceExisting()
                .build();

        assertXmlIdentical(SUBSYSTEM_WITH_TWO_MAPPING_MODULES_UNNAMED, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addMappingModule);
        assertXmlIdentical(SUBSYSTEM_WITH_TWO_MAPPING_MODULES_UNNAMED_REPLACE_EXPECTED, Files.toString(cfg,
                Charsets.UTF_8));
    }

    @Test
    public void addFullLoginModule() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_ORIGINAL, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddMappingModule addMappingModule = new AddMappingModule.Builder("PropertiesRoles", "CreaperModule")
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .type("role")
                .module("org.picketbox")
                .addModuleOption("rolesProperties", "roles.properties")
                .build();

        assertXmlIdentical(SUBSYSTEM_ORIGINAL, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addMappingModule);
        assertXmlIdentical(SUBSYSTEM_FULL_EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }
}
