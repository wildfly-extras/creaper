package org.wildfly.extras.creaper.commands.security.realms;

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

public class AddJaasAuthenticationOfflineTest {

    private static final String REALM_SIMPLE = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\"/>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_JAAS = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authentication>\n"
            + "                    <jaas name=\"other\"/>\n"
            + "                </authentication>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_JAAS_FULL = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authentication>\n"
            + "                    <jaas name=\"other\" assign-groups=\"true\"/>\n"
            + "                </authentication>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_LOCAL = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authentication>\n"
            + "                    <local default-user=\"$local\" skip-group-loading=\"true\"/>\n"
            + "                </authentication>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_LOCAL_EXPECTED = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authentication>\n"
            + "                    <local default-user=\"$local\" skip-group-loading=\"true\"/>\n"
            + "                    <jaas name=\"other\"/>\n"
            + "                </authentication>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_PROPERTIES = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authentication>\n"
            + "                    <properties path=\"users.properties\" relative-to=\"jboss.server.config.dir\"/>\n"
            + "                </authentication>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_LDAP = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <authentication>\n"
            + "                    <ldap connection=\"ldap-connection\" base-dn=\"ou=People,dc=jboss,dc=org\"/>\n"
            + "                </authentication>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
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
        Files.write(REALM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddJaasAuthentication addJaasAuthentication
                = new AddJaasAuthentication.Builder("creaperSecRealm")
                .name("other")
                .build();

        assertXmlIdentical(REALM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addJaasAuthentication);
        assertXmlIdentical(REALM_WITH_JAAS, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void addSecondSimple() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_WITH_JAAS, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddJaasAuthentication addJaasAuthentication
                = new AddJaasAuthentication.Builder("creaperSecRealm")
                .name("jboss-web-policy")
                .build();

        assertXmlIdentical(REALM_WITH_JAAS, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addJaasAuthentication);

        fail("Jaas authentication already exists in configuration of creaperSecRealm, exception should be thrown");
    }

    @Test(expected = CommandFailedException.class)
    public void existing() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_WITH_JAAS, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddJaasAuthentication addJaasAuthentication
                = new AddJaasAuthentication.Builder("creaperSecRealm")
                .name("other")
                .assignGroups(true)
                .build();

        assertXmlIdentical(REALM_WITH_JAAS, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addJaasAuthentication);

        fail("Jaas authentication already exists in configuration of creaperSecRealm, exception should be thrown");
    }

    @Test
    public void overrideExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_WITH_JAAS, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddJaasAuthentication addJaasAuthentication
                = new AddJaasAuthentication.Builder("creaperSecRealm")
                .name("other")
                .assignGroups(true)
                .replaceExisting()
                .build();

        assertXmlIdentical(REALM_WITH_JAAS, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addJaasAuthentication);
        assertXmlIdentical(REALM_WITH_JAAS_FULL, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addFull() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddJaasAuthentication addJaasAuthentication
                = new AddJaasAuthentication.Builder("creaperSecRealm")
                .name("other")
                .assignGroups(true)
                .build();

        assertXmlIdentical(REALM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addJaasAuthentication);
        assertXmlIdentical(REALM_WITH_JAAS_FULL, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addAsSecondAuthnType() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_WITH_LOCAL, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddJaasAuthentication addJaasAuthentication
                = new AddJaasAuthentication.Builder("creaperSecRealm")
                .name("other")
                .build();

        assertXmlIdentical(REALM_WITH_LOCAL, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addJaasAuthentication);
        assertXmlIdentical(REALM_WITH_LOCAL_EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void addSameTypeOfAuthentication_properties() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_WITH_PROPERTIES, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddJaasAuthentication addJaasAuthentication
                = new AddJaasAuthentication.Builder("creaperSecRealm")
                .name("other")
                .build();

        assertXmlIdentical(REALM_WITH_PROPERTIES, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addJaasAuthentication);

        fail("Username/password based authentication already exists in configuration of creaperSecRealm, exception should be thrown");
    }

    @Test(expected = CommandFailedException.class)
    public void addSameTypeOfAuthentication_ldap() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_WITH_LDAP, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddJaasAuthentication addJaasAuthentication
                = new AddJaasAuthentication.Builder("creaperSecRealm")
                .name("other")
                .build();

        assertXmlIdentical(REALM_WITH_LDAP, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addJaasAuthentication);

        fail("Username/password based authentication already exists in configuration of creaperSecRealm, exception should be thrown");
    }

}
