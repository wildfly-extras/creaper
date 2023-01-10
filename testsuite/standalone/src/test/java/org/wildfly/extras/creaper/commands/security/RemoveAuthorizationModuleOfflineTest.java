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

public class RemoveAuthorizationModuleOfflineTest {

    private static final String SECURITY_DOMAIN_WITH_TWO_AUTHORIZATION_MODULES = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:security:1.2\">\n"
            + "            <security-domains>\n"
            + "                <security-domain name=\"creaperSecDomain\">\n"
            + "                    <authorization>\n"
            + "                        <policy-module code=\"Web\" flag=\"required\">\n"
            + "                            <module-option name=\"delegateMap\" value=\"delegateMapValue\"/>\n"
            + "                        </policy-module>\n"
            + "                        <policy-module code=\"Delegating\" flag=\"required\">\n"
            + "                            <module-option name=\"delegateMap\" value=\"delegateMapValue\"/>\n"
            + "                        </policy-module>\n"
            + "                    </authorization>\n"
            + "                </security-domain>\n"
            + "            </security-domains>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";
    private static final String SECURITY_DOMAIN_WITH_ONE_AUTHORIZATION_MODULE = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:security:1.2\">\n"
            + "            <security-domains>\n"
            + "                <security-domain name=\"creaperSecDomain\">\n"
            + "                    <authorization>\n"
            + "                        <policy-module code=\"Web\" flag=\"required\">\n"
            + "                            <module-option name=\"delegateMap\" value=\"delegateMapValue\"/>\n"
            + "                        </policy-module>\n"
            + "                    </authorization>\n"
            + "                </security-domain>\n"
            + "            </security-domains>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";
    private static final String SECURITY_DOMAIN_WITHOUT_AUTHORIZATION_MODULE = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:security:1.2\">\n"
            + "            <security-domains>\n"
            + "                <security-domain name=\"creaperSecDomain\"/>\n"
            + "            </security-domains>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SECURITY_DOMAIN_WITH_TWO_AUTHORIZATION_MODULE_NAMED = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:security:1.2\">\n"
            + "            <security-domains>\n"
            + "                <security-domain name=\"creaperSecDomain\">\n"
            + "                    <authorization>\n"
            + "                        <policy-module code=\"Web\" flag=\"required\">\n"
            + "                            <module-option name=\"delegateMap\" value=\"delegateMapValue\"/>\n"
            + "                        </policy-module>\n"
            + "                        <policy-module name=\"NamedAuthorizationModule\" code=\"Delegating\" "
            + "flag=\"required\">\n"
            + "                            <module-option name=\"delegateMap\" value=\"delegateMapValue\"/>\n"
            + "                        </policy-module>\n"
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
        XmlAssert.setNormalizeWhitespace(true);
    }

    @Test
    public void removeOneOfMoreAuthorizationModules() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SECURITY_DOMAIN_WITH_TWO_AUTHORIZATION_MODULES, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        RemoveAuthorizationModule removeSecurityDomain = new RemoveAuthorizationModule("creaperSecDomain",
                "Delegating");

        assertXmlIdentical(SECURITY_DOMAIN_WITH_TWO_AUTHORIZATION_MODULES, Files.toString(cfg, Charsets.UTF_8));
        client.apply(removeSecurityDomain);
        assertXmlIdentical(SECURITY_DOMAIN_WITH_ONE_AUTHORIZATION_MODULE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void removeLastAuthorizationModule() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SECURITY_DOMAIN_WITH_ONE_AUTHORIZATION_MODULE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        RemoveAuthorizationModule removeSecurityDomain = new RemoveAuthorizationModule("creaperSecDomain", "Web");

        assertXmlIdentical(SECURITY_DOMAIN_WITH_ONE_AUTHORIZATION_MODULE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(removeSecurityDomain);
        assertXmlIdentical(SECURITY_DOMAIN_WITHOUT_AUTHORIZATION_MODULE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void removeNamedAuthorizationModule() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SECURITY_DOMAIN_WITH_TWO_AUTHORIZATION_MODULE_NAMED, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        RemoveAuthorizationModule removeSecurityDomain = new RemoveAuthorizationModule("creaperSecDomain",
                "NamedAuthorizationModule");

        assertXmlIdentical(SECURITY_DOMAIN_WITH_TWO_AUTHORIZATION_MODULE_NAMED, Files.toString(cfg, Charsets.UTF_8));
        client.apply(removeSecurityDomain);
        assertXmlIdentical(SECURITY_DOMAIN_WITH_ONE_AUTHORIZATION_MODULE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void removeNonExistingAuthorizationModule() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SECURITY_DOMAIN_WITH_ONE_AUTHORIZATION_MODULE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        RemoveAuthorizationModule removeSecurityDomain = new RemoveAuthorizationModule("creaperSecDomain",
                "Delegating");

        assertXmlIdentical(SECURITY_DOMAIN_WITH_ONE_AUTHORIZATION_MODULE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(removeSecurityDomain);

        fail("Authorization module Delegating does not exist in configuration, exception should be thrown");
    }

    @Test(expected = CommandFailedException.class)
    public void doNotRemoveNamedAuthorizationModuleByCodeReference() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SECURITY_DOMAIN_WITH_TWO_AUTHORIZATION_MODULE_NAMED, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        RemoveAuthorizationModule removeSecurityDomain = new RemoveAuthorizationModule("creaperSecDomain",
                "Delegating");

        assertXmlIdentical(SECURITY_DOMAIN_WITH_TWO_AUTHORIZATION_MODULE_NAMED, Files.toString(cfg, Charsets.UTF_8));
        client.apply(removeSecurityDomain);

        fail("Authorization module Delegating does not exist in configuration, exception should be thrown");
    }
}
