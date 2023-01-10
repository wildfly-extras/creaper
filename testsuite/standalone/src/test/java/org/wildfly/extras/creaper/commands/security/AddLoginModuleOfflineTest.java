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

public class AddLoginModuleOfflineTest {

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

    private static final String SUBSYSTEM_WITH_ONE_LOGIN_MODULE = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:security:1.2\">\n"
            + "            <security-domains>\n"
            + "                <security-domain name=\"creaperSecDomain\">\n"
            + "                    <authentication>\n"
            + "                        <login-module name=\"UsersRoles\" code=\"UsersRoles\" flag=\"required\"/>\n"
            + "                    </authentication>\n"
            + "                </security-domain>\n"
            + "            </security-domains>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_WITH_ONE_LOGIN_MODULE_WITHOUT_NAME = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:security:1.2\">\n"
            + "            <security-domains>\n"
            + "                <security-domain name=\"creaperSecDomain\">\n"
            + "                    <authentication>\n"
            + "                        <login-module code=\"UsersRoles\" flag=\"required\"/>\n"
            + "                    </authentication>\n"
            + "                </security-domain>\n"
            + "            </security-domains>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_WITH_TWO_LOGIN_MODULES = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:security:1.2\">\n"
            + "            <security-domains>\n"
            + "                <security-domain name=\"creaperSecDomain\">\n"
            + "                    <authentication>\n"
            + "                        <login-module name=\"UsersRoles\" code=\"UsersRoles\" flag=\"required\"/>\n"
            + "                        <login-module name=\"CreaperModule\" code=\"UsersRoles\" flag=\"required\"/>\n"
            + "                    </authentication>\n"
            + "                </security-domain>\n"
            + "            </security-domains>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_WITH_TWO_LOGIN_MODULES_UNNAMED = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:security:1.2\">\n"
            + "            <security-domains>\n"
            + "                <security-domain name=\"creaperSecDomain\">\n"
            + "                    <authentication>\n"
            + "                        <login-module name=\"CreaperModule\" code=\"UsersRoles\" flag=\"required\"/>\n"
            + "                        <login-module code=\"UsersRoles\" flag=\"required\"/>\n"
            + "                    </authentication>\n"
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
            + "                    <authentication>\n"
            + "                        <login-module name=\"UsersRoles\" code=\"UsersRoles\" flag=\"sufficient\"/>\n"
            + "                    </authentication>\n"
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
            + "                    <authentication>\n"
            + "                        <login-module name=\"CreaperModule\" code=\"UsersRoles\" flag=\"required\" "
            + "module=\"org.picketbox\">\n"
            + "                            <module-option name=\"usersProperties\" value=\"user.properties\"/>\n"
            + "                            <module-option name=\"rolesProperties\" value=\"roles.properties\"/>\n"
            + "                        </login-module>\n"
            + "                    </authentication>\n"
            + "                </security-domain>\n"
            + "            </security-domains>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_WITH_TWO_LOGIN_MODULES_UNNAMED_REPLACE_EXPECTED = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:security:1.2\">\n"
            + "            <security-domains>\n"
            + "                <security-domain name=\"creaperSecDomain\">\n"
            + "                    <authentication>\n"
            + "                        <login-module name=\"CreaperModule\" code=\"UsersRoles\" flag=\"required\"/>\n"
            + "                        <login-module name=\"UsersRoles\" code=\"UsersRoles\" flag=\"sufficient\"/>\n"
            + "                    </authentication>\n"
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

        AddLoginModule addLoginModule = new AddLoginModule.Builder("UsersRoles")
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .flag("required")
                .build();

        assertXmlIdentical(SUBSYSTEM_ORIGINAL, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLoginModule);
        assertXmlIdentical(SUBSYSTEM_WITH_ONE_LOGIN_MODULE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addSecondSimple() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_WITH_ONE_LOGIN_MODULE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLoginModule addLoginModule = new AddLoginModule.Builder("UsersRoles", "CreaperModule")
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .flag("required")
                .build();

        assertXmlIdentical(SUBSYSTEM_WITH_ONE_LOGIN_MODULE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLoginModule);
        assertXmlIdentical(SUBSYSTEM_WITH_TWO_LOGIN_MODULES, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void existing() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_WITH_ONE_LOGIN_MODULE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLoginModule addLoginModule = new AddLoginModule.Builder("UsersRoles")
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .flag("sufficient")
                .build();

        assertXmlIdentical(SUBSYSTEM_WITH_ONE_LOGIN_MODULE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLoginModule);

        fail("Login module already exists in configuration, exception should be thrown");
    }

    @Test
    public void overrideExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_WITH_ONE_LOGIN_MODULE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLoginModule addLoginModule = new AddLoginModule.Builder("UsersRoles")
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .flag("sufficient")
                .replaceExisting()
                .build();

        assertXmlIdentical(SUBSYSTEM_WITH_ONE_LOGIN_MODULE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLoginModule);
        assertXmlIdentical(SUBSYSTEM_REPLACED_EXPECTED, Files.toString(cfg, Charsets.UTF_8));

    }

    @Test(expected = CommandFailedException.class)
    public void existingWithoutName() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_WITH_ONE_LOGIN_MODULE_WITHOUT_NAME, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLoginModule addLoginModule = new AddLoginModule.Builder("UsersRoles")
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .flag("sufficient")
                .build();

        assertXmlIdentical(SUBSYSTEM_WITH_ONE_LOGIN_MODULE_WITHOUT_NAME, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLoginModule);

        fail("Login module already exists in configuration, exception should be thrown");

    }

    @Test
    public void overrideExistingWithoutName() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_WITH_ONE_LOGIN_MODULE_WITHOUT_NAME, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLoginModule addLoginModule = new AddLoginModule.Builder("UsersRoles")
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .flag("sufficient")
                .replaceExisting()
                .build();

        assertXmlIdentical(SUBSYSTEM_WITH_ONE_LOGIN_MODULE_WITHOUT_NAME, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLoginModule);
        assertXmlIdentical(SUBSYSTEM_REPLACED_EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void existingWithoutNameTwoLoginModules() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_WITH_TWO_LOGIN_MODULES_UNNAMED, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLoginModule addLoginModule = new AddLoginModule.Builder("UsersRoles")
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .flag("sufficient")
                .build();

        assertXmlIdentical(SUBSYSTEM_WITH_TWO_LOGIN_MODULES_UNNAMED, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLoginModule);

        fail("Login module already exists in configuration, exception should be thrown");

    }

    @Test
    public void overrideExistingWithoutNameTwoLoginModules() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_WITH_TWO_LOGIN_MODULES_UNNAMED, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLoginModule addLoginModule = new AddLoginModule.Builder("UsersRoles")
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .flag("sufficient")
                .replaceExisting()
                .build();

        assertXmlIdentical(SUBSYSTEM_WITH_TWO_LOGIN_MODULES_UNNAMED, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLoginModule);
        assertXmlIdentical(SUBSYSTEM_WITH_TWO_LOGIN_MODULES_UNNAMED_REPLACE_EXPECTED, Files.toString(cfg,
                Charsets.UTF_8));
    }

    @Test
    public void addFullLoginModule() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_ORIGINAL, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLoginModule addLoginModule = new AddLoginModule.Builder("UsersRoles", "CreaperModule")
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .flag("required")
                .module("org.picketbox")
                .addModuleOption("usersProperties", "user.properties")
                .addModuleOption("rolesProperties", "roles.properties")
                .build();

        assertXmlIdentical(SUBSYSTEM_ORIGINAL, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLoginModule);
        assertXmlIdentical(SUBSYSTEM_FULL_EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }

}
