package org.wildfly.extras.creaper.commands.security.realms;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;
import org.wildfly.extras.creaper.core.CommandFailedException;

import static org.junit.Assert.fail;
import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;

public class AddKerberosAuthenticationOfflineTest {

    private static final String REALM_EMPTY = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\"/>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_SIMPLE_KERBEROS = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <server-identities>\n"
            + "                    <kerberos>\n"
            + "                        <keytab principal=\"HTTP/localhost@JBOSS.ORG\"/>\n"
            + "                    </kerberos>\n"
            + "                </server-identities>\n"
            + "                <authentication>\n"
            + "                    <kerberos/>\n"
            + "                </authentication>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_KERBEROS_REPLACE = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <server-identities>\n"
            + "                    <kerberos>\n"
            + "                        <keytab principal=\"HTTP/localhost@JBOSS.ORG\" path=\"a.keytab\"/>\n"
            + "                    </kerberos>\n"
            + "                </server-identities>\n"
            + "                <authentication>\n"
            + "                    <kerberos remove-realm=\"true\"/>\n"
            + "                </authentication>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_KERBEROS_REPLACE_EXPECTED = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <server-identities>\n"
            + "                    <kerberos>\n"
            + "                        <keytab principal=\"HTTP/localhost@JBOSS.ORG\" path=\"b.keytab\"/>\n"
            + "                    </kerberos>\n"
            + "                </server-identities>\n"
            + "                <authentication>\n"
            + "                    <kerberos remove-realm=\"false\"/>\n"
            + "                </authentication>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_FULL_KERBEROS = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <server-identities>\n"
            + "                    <kerberos>\n"
            + "                        <keytab principal=\"HTTP/localhost@JBOSS.ORG\" path=\"a.keytab\" debug=\"true\" "
            + "relative-to=\"jboss.server.config.dir\" for-hosts=\"localhost 127.0.0.1\"/>\n"
            + "                    </kerberos>\n"
            + "                </server-identities>\n"
            + "                <authentication>\n"
            + "                    <kerberos remove-realm=\"true\"/>\n"
            + "                </authentication>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_FULL_KERBEROS_TWO_KEYTABS = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <server-identities>\n"
            + "                    <kerberos>\n"
            + "                        <keytab principal=\"HTTP/localhost@JBOSS.ORG\" path=\"a.keytab\" debug=\"true\" "
            + "relative-to=\"jboss.server.config.dir\" for-hosts=\"localhost\"/>\n"
            + "                        <keytab principal=\"HTTP/localhost@JBOSS.COM\" path=\"b.keytab\" "
            + "debug=\"false\" relative-to=\"jboss.server.log.dir\" for-hosts=\"*\"/>\n"
            + "                    </kerberos>\n"
            + "                </server-identities>\n"
            + "                <authentication>\n"
            + "                    <kerberos remove-realm=\"false\"/>\n"
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
            + "                    <local/>\n"
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
            + "                    <local/>\n"
            + "                    <kerberos/>\n"
            + "                </authentication>\n"
            + "                <server-identities>\n"
            + "                    <kerberos>\n"
            + "                        <keytab principal=\"HTTP/localhost@JBOSS.ORG\"/>\n"
            + "                    </kerberos>\n"
            + "                </server-identities>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_SSL = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <server-identities>\n"
            + "                    <ssl/>\n"
            + "                </server-identities>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_SSL_EXPECTED = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"creaperSecRealm\">\n"
            + "                <server-identities>\n"
            + "                    <ssl/>\n"
            + "                    <kerberos>\n"
            + "                        <keytab principal=\"HTTP/localhost@JBOSS.ORG\"/>\n"
            + "                    </kerberos>\n"
            + "                </server-identities>\n"
            + "                <authentication>\n"
            + "                    <kerberos/>\n"
            + "                </authentication>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
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
        Files.write(REALM_EMPTY, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddKerberosAuthentication addKerberosAuthentication
                = new AddKerberosAuthentication.Builder("creaperSecRealm")
                .addKeytab(new KerberosKeytab.Builder()
                        .principal("HTTP/localhost@JBOSS.ORG")
                        .build())
                .build();

        assertXmlIdentical(REALM_EMPTY, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addKerberosAuthentication);
        assertXmlIdentical(REALM_WITH_SIMPLE_KERBEROS, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void existing() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_WITH_KERBEROS_REPLACE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddKerberosAuthentication addKerberosAuthentication
                = new AddKerberosAuthentication.Builder("creaperSecRealm")
                .addKeytab(new KerberosKeytab.Builder()
                        .principal("HTTP/localhost@JBOSS.ORG")
                        .path("b.keytab")
                        .build())
                .removeRealm(false)
                .build();

        assertXmlIdentical(REALM_WITH_KERBEROS_REPLACE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addKerberosAuthentication);

        fail("Jaas authentication already exists in configuration of creaperSecRealm, exception should be thrown");
    }

    @Test
    public void overrideExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_WITH_KERBEROS_REPLACE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddKerberosAuthentication addKerberosAuthentication
                = new AddKerberosAuthentication.Builder("creaperSecRealm")
                .addKeytab(new KerberosKeytab.Builder()
                        .principal("HTTP/localhost@JBOSS.ORG")
                        .path("b.keytab")
                        .build())
                .removeRealm(false)
                .replaceExisting()
                .build();

        assertXmlIdentical(REALM_WITH_KERBEROS_REPLACE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addKerberosAuthentication);
        assertXmlIdentical(REALM_WITH_KERBEROS_REPLACE_EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addFull_oneKeytab() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_EMPTY, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddKerberosAuthentication addKerberosAuthentication
                = new AddKerberosAuthentication.Builder("creaperSecRealm")
                .addKeytab(new KerberosKeytab.Builder()
                        .principal("HTTP/localhost@JBOSS.ORG")
                        .path("a.keytab")
                        .relativeTo("jboss.server.config.dir")
                        .validForHosts("localhost", "127.0.0.1")
                        .debug(true)
                        .build())
                .removeRealm(true)
                .build();

        assertXmlIdentical(REALM_EMPTY, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addKerberosAuthentication);
        assertXmlIdentical(REALM_WITH_FULL_KERBEROS, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addFull_twoKeytabs() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_EMPTY, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddKerberosAuthentication addKerberosAuthentication
                = new AddKerberosAuthentication.Builder("creaperSecRealm")
                .addKeytab(new KerberosKeytab.Builder()
                        .principal("HTTP/localhost@JBOSS.ORG")
                        .path("a.keytab")
                        .relativeTo("jboss.server.config.dir")
                        .validForHosts("localhost")
                        .debug(true)
                        .build())
                .addKeytab(new KerberosKeytab.Builder()
                        .principal("HTTP/localhost@JBOSS.COM")
                        .path("b.keytab")
                        .relativeTo("jboss.server.log.dir")
                        .validForAllHosts()
                        .debug(false)
                        .build())
                .removeRealm(false)
                .build();

        assertXmlIdentical(REALM_EMPTY, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addKerberosAuthentication);
        assertXmlIdentical(REALM_WITH_FULL_KERBEROS_TWO_KEYTABS, Files.toString(cfg, Charsets.UTF_8));

    }

    @Test
    public void addAsSecondAuthnType() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_WITH_LOCAL, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddKerberosAuthentication addKerberosAuthentication
                = new AddKerberosAuthentication.Builder("creaperSecRealm")
                .addKeytab(new KerberosKeytab.Builder()
                        .principal("HTTP/localhost@JBOSS.ORG")
                        .build())
                .build();

        assertXmlIdentical(REALM_WITH_LOCAL, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addKerberosAuthentication);
        assertXmlIdentical(REALM_WITH_LOCAL_EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void doNotOverrideDifferentServerIdentity() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_WITH_SSL, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddKerberosAuthentication addKerberosAuthentication
                = new AddKerberosAuthentication.Builder("creaperSecRealm")
                .addKeytab(new KerberosKeytab.Builder()
                        .principal("HTTP/localhost@JBOSS.ORG")
                        .build())
                .build();

        assertXmlIdentical(REALM_WITH_SSL, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addKerberosAuthentication);
        assertXmlIdentical(REALM_WITH_SSL_EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }
}
