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

public class RemoveLoginModuleOfflineTest {

    private static final String SECURITY_DOMAIN_WITH_TWO_LOGIN_MODULES = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:security:1.2\">\n"
            + "            <security-domains>\n"
            + "                <security-domain name=\"creaperSecDomain\">\n"
            + "                    <authentication>\n"
            + "                        <login-module code=\"RealmDirect\" flag=\"required\">\n"
            + "                            <module-option name=\"password-stacking\" value=\"useFirstPass\"/>\n"
            + "                        </login-module>\n"
            + "                        <login-module code=\"UsersRoles\" flag=\"required\">\n"
            + "                            <module-option name=\"usersProperties\" value=\"users.properties\"/>\n"
            + "                            <module-option name=\"rolesProperties\" value=\"roles.properties\"/>\n"
            + "                        </login-module>\n"
            + "                    </authentication>\n"
            + "                </security-domain>\n"
            + "            </security-domains>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";
    private static final String SECURITY_DOMAIN_WITH_ONE_LOGIN_MODULE = ""
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
    private static final String SECURITY_DOMAIN_WITHOUT_LOGIN_MODULE = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:security:1.2\">\n"
            + "            <security-domains>\n"
            + "                <security-domain name=\"creaperSecDomain\"/>\n"
            + "            </security-domains>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SECURITY_DOMAIN_WITH_TWO_LOGIN_MODULE_NAMED = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:security:1.2\">\n"
            + "            <security-domains>\n"
            + "                <security-domain name=\"creaperSecDomain\">\n"
            + "                    <authentication>\n"
            + "                        <login-module code=\"RealmDirect\" flag=\"required\">\n"
            + "                            <module-option name=\"password-stacking\" value=\"useFirstPass\"/>\n"
            + "                        </login-module>\n"
            + "                        <login-module name=\"NamedLoginModule\" code=\"UsersRoles\" flag=\"required\">\n"
            + "                            <module-option name=\"usersProperties\" value=\"users.properties\"/>\n"
            + "                            <module-option name=\"rolesProperties\" value=\"roles.properties\"/>\n"
            + "                        </login-module>\n"
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
    public void removeOneOfMoreLoginModules() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SECURITY_DOMAIN_WITH_TWO_LOGIN_MODULES, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        RemoveLoginModule removeSecurityDomain = new RemoveLoginModule("creaperSecDomain", "UsersRoles");

        assertXmlIdentical(SECURITY_DOMAIN_WITH_TWO_LOGIN_MODULES, Files.toString(cfg, Charsets.UTF_8));
        client.apply(removeSecurityDomain);
        assertXmlIdentical(SECURITY_DOMAIN_WITH_ONE_LOGIN_MODULE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void removeLastLoginModule() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SECURITY_DOMAIN_WITH_ONE_LOGIN_MODULE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        RemoveLoginModule removeSecurityDomain = new RemoveLoginModule("creaperSecDomain", "RealmDirect");

        assertXmlIdentical(SECURITY_DOMAIN_WITH_ONE_LOGIN_MODULE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(removeSecurityDomain);
        assertXmlIdentical(SECURITY_DOMAIN_WITHOUT_LOGIN_MODULE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void removeNamedLoginModule() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SECURITY_DOMAIN_WITH_TWO_LOGIN_MODULE_NAMED, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        RemoveLoginModule removeSecurityDomain = new RemoveLoginModule("creaperSecDomain", "NamedLoginModule");

        assertXmlIdentical(SECURITY_DOMAIN_WITH_TWO_LOGIN_MODULE_NAMED, Files.toString(cfg, Charsets.UTF_8));
        client.apply(removeSecurityDomain);
        assertXmlIdentical(SECURITY_DOMAIN_WITH_ONE_LOGIN_MODULE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void removeNonExistingLoginModule() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SECURITY_DOMAIN_WITH_ONE_LOGIN_MODULE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        RemoveLoginModule removeSecurityDomain = new RemoveLoginModule("creaperSecDomain", "UsersRoles");

        assertXmlIdentical(SECURITY_DOMAIN_WITH_ONE_LOGIN_MODULE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(removeSecurityDomain);

        fail("Login module UsersRoles does not exist in configuration, exception should be thrown");
    }

    @Test(expected = CommandFailedException.class)
    public void doNotRemoveNamedLoginModuleByCodeReference() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SECURITY_DOMAIN_WITH_TWO_LOGIN_MODULE_NAMED, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        RemoveLoginModule removeSecurityDomain = new RemoveLoginModule("creaperSecDomain", "UsersRoles");

        assertXmlIdentical(SECURITY_DOMAIN_WITH_TWO_LOGIN_MODULE_NAMED, Files.toString(cfg, Charsets.UTF_8));
        client.apply(removeSecurityDomain);

        fail("Login module UsersRoles does not exist in configuration, exception should be thrown");
    }

}
