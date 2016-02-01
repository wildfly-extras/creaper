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

public class AddAuthorizationModuleOfflineTest {

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

    private static final String SUBSYSTEM_WITH_ONE_AUTHORIZATION_MODULE = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:security:1.2\">\n"
            + "            <security-domains>\n"
            + "                <security-domain name=\"creaperSecDomain\">\n"
            + "                    <authorization>\n"
            + "                        <policy-module name=\"Delegating\" code=\"Delegating\" flag=\"required\"/>\n"
            + "                    </authorization>\n"
            + "                </security-domain>\n"
            + "            </security-domains>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_WITH_ONE_AUTHORIZATION_MODULE_WITHOUT_NAME = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:security:1.2\">\n"
            + "            <security-domains>\n"
            + "                <security-domain name=\"creaperSecDomain\">\n"
            + "                    <authorization>\n"
            + "                        <policy-module code=\"Delegating\" flag=\"required\"/>\n"
            + "                    </authorization>\n"
            + "                </security-domain>\n"
            + "            </security-domains>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_WITH_TWO_AUTHORIZATION_MODULES = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:security:1.2\">\n"
            + "            <security-domains>\n"
            + "                <security-domain name=\"creaperSecDomain\">\n"
            + "                    <authorization>\n"
            + "                        <policy-module name=\"Delegating\" code=\"Delegating\" flag=\"required\"/>\n"
            + "                        <policy-module name=\"CreaperModule\" code=\"Delegating\" flag=\"required\"/>\n"
            + "                    </authorization>\n"
            + "                </security-domain>\n"
            + "            </security-domains>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_WITH_TWO_AUTHORIZATION_MODULES_UNNAMED = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:security:1.2\">\n"
            + "            <security-domains>\n"
            + "                <security-domain name=\"creaperSecDomain\">\n"
            + "                    <authorization>\n"
            + "                        <policy-module name=\"CreaperModule\" code=\"Delegating\" flag=\"required\"/>\n"
            + "                        <policy-module code=\"Delegating\" flag=\"required\"/>\n"
            + "                    </authorization>\n"
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
            + "                    <authorization>\n"
            + "                        <policy-module name=\"Delegating\" code=\"Delegating\" flag=\"sufficient\"/>\n"
            + "                    </authorization>\n"
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
            + "                    <authorization>\n"
            + "                        <policy-module name=\"CreaperModule\" code=\"Delegating\" flag=\"required\" "
            + "module=\"org.picketbox\">\n"
            + "                            <module-option name=\"delegateMap\" value=\"delegateMapValue\"/>\n"
            + "                        </policy-module>\n"
            + "                    </authorization>\n"
            + "                </security-domain>\n"
            + "            </security-domains>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_WITH_TWO_AUTHORIZATION_MODULES_UNNAMED_REPLACE_EXPECTED = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:security:1.2\">\n"
            + "            <security-domains>\n"
            + "                <security-domain name=\"creaperSecDomain\">\n"
            + "                    <authorization>\n"
            + "                        <policy-module name=\"CreaperModule\" code=\"Delegating\" flag=\"required\"/>\n"
            + "                        <policy-module name=\"Delegating\" code=\"Delegating\" flag=\"sufficient\"/>\n"
            + "                    </authorization>\n"
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
    public void addSimple() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_ORIGINAL, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuthorizationModule addAuthorizationModule = new AddAuthorizationModule.Builder("Delegating")
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .flag("required")
                .build();

        assertXmlIdentical(SUBSYSTEM_ORIGINAL, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addAuthorizationModule);
        assertXmlIdentical(SUBSYSTEM_WITH_ONE_AUTHORIZATION_MODULE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addSecondSimple() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_WITH_ONE_AUTHORIZATION_MODULE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuthorizationModule addAuthorizationModule = new AddAuthorizationModule.Builder("Delegating",
                "CreaperModule")
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .flag("required")
                .build();

        assertXmlIdentical(SUBSYSTEM_WITH_ONE_AUTHORIZATION_MODULE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addAuthorizationModule);
        assertXmlIdentical(SUBSYSTEM_WITH_TWO_AUTHORIZATION_MODULES, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void existing() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_WITH_ONE_AUTHORIZATION_MODULE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuthorizationModule addAuthorizationModule = new AddAuthorizationModule.Builder("Delegating")
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .flag("sufficient")
                .build();

        assertXmlIdentical(SUBSYSTEM_WITH_ONE_AUTHORIZATION_MODULE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addAuthorizationModule);

        fail("Authorization module already exists in configuration, exception should be thrown");
    }

    @Test
    public void overrideExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_WITH_ONE_AUTHORIZATION_MODULE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuthorizationModule addAuthorizationModule = new AddAuthorizationModule.Builder("Delegating")
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .flag("sufficient")
                .replaceExisting()
                .build();

        assertXmlIdentical(SUBSYSTEM_WITH_ONE_AUTHORIZATION_MODULE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addAuthorizationModule);
        assertXmlIdentical(SUBSYSTEM_REPLACED_EXPECTED, Files.toString(cfg, Charsets.UTF_8));

    }

    @Test(expected = CommandFailedException.class)
    public void existingWithoutName() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_WITH_ONE_AUTHORIZATION_MODULE_WITHOUT_NAME, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuthorizationModule addAuthorizationModule = new AddAuthorizationModule.Builder("Delegating")
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .flag("sufficient")
                .build();

        assertXmlIdentical(SUBSYSTEM_WITH_ONE_AUTHORIZATION_MODULE_WITHOUT_NAME, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addAuthorizationModule);

        fail("Authorization module already exists in configuration, exception should be thrown");

    }

    @Test
    public void overrideExistingWithoutName() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_WITH_ONE_AUTHORIZATION_MODULE_WITHOUT_NAME, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuthorizationModule addAuthorizationModule = new AddAuthorizationModule.Builder("Delegating")
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .flag("sufficient")
                .replaceExisting()
                .build();

        assertXmlIdentical(SUBSYSTEM_WITH_ONE_AUTHORIZATION_MODULE_WITHOUT_NAME, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addAuthorizationModule);
        assertXmlIdentical(SUBSYSTEM_REPLACED_EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void existingWithoutNameTwoAuthorizationModules() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_WITH_TWO_AUTHORIZATION_MODULES_UNNAMED, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuthorizationModule addAuthorizationModule = new AddAuthorizationModule.Builder("Delegating")
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .flag("sufficient")
                .build();

        assertXmlIdentical(SUBSYSTEM_WITH_TWO_AUTHORIZATION_MODULES_UNNAMED, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addAuthorizationModule);

        fail("Authorization module already exists in configuration, exception should be thrown");

    }

    @Test
    public void overrideExistingWithoutNameTwoAuthorizationModules() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_WITH_TWO_AUTHORIZATION_MODULES_UNNAMED, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuthorizationModule addAuthorizationModule = new AddAuthorizationModule.Builder("Delegating")
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .flag("sufficient")
                .replaceExisting()
                .build();

        assertXmlIdentical(SUBSYSTEM_WITH_TWO_AUTHORIZATION_MODULES_UNNAMED, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addAuthorizationModule);
        assertXmlIdentical(SUBSYSTEM_WITH_TWO_AUTHORIZATION_MODULES_UNNAMED_REPLACE_EXPECTED, Files.toString(cfg,
                Charsets.UTF_8));
    }

    @Test
    public void addFullAuthorizationModule() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_ORIGINAL, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuthorizationModule addAuthorizationModule = new AddAuthorizationModule.Builder("Delegating",
                "CreaperModule")
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .flag("required")
                .module("org.picketbox")
                .addModuleOption("delegateMap", "delegateMapValue")
                .build();

        assertXmlIdentical(SUBSYSTEM_ORIGINAL, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addAuthorizationModule);
        assertXmlIdentical(SUBSYSTEM_FULL_EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }
}
