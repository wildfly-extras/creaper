package org.wildfly.extras.creaper.commands.security.realms;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;

import java.io.File;

import static org.junit.Assert.fail;
import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;

public class AddSslServerIdentityOfflineTest {

    private static final String REALM_EMPTY = ""
            + "<server xmlns=\"urn:jboss:domain:4.2\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"realmName\"/>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_SSL = realmWithSsl("");
    private static final String REALM_WITH_SSL_AND_GENERATE_SELF_SIGNED_CERT =
            realmWithSsl("generate-self-signed-certificate-host=\"localhost\"");


    private static String realmWithSsl(String sslKeystoreAdditionalParams) {
        return ""
                + "<server xmlns=\"urn:jboss:domain:4.2\">\n"
                + "    <management>\n"
                + "        <security-realms>\n"
                + "            <security-realm name=\"realmName\">\n"
                + "                <server-identities>\n"
                + "                    <ssl>\n"
                + "                       <engine enabled-cipher-suites=\"DEFAULT ALL\" enabled-protocols=\"SSL TLS\"/>\n"
                + "                       <keystore path=\"server.keystore\" relative-to=\"jboss.server.config.dir\" "
                + "                                 keystore-password=\"password\" key-password=\"password\""
                + "                                 alias=\"alias\" provider=\"JKS\" protocol=\"TLS\" "
                + sslKeystoreAdditionalParams
                + "                                 />\n"
                + "                    </ssl>\n"
                + "                </server-identities>\n"
                + "            </security-realm>\n"
                + "        </security-realms>\n"
                + "    </management>\n"
                + "</server>";
    }


    private static final String PASSWORD = "password";

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

        AddSslServerIdentity cmd = new AddSslServerIdentity.Builder("realmName")
                .alias("alias")
                .keystorePath("server.keystore")
                .keystorePassword("password")
                .keyPassword("password")
                .keystoreProvider("JKS")
                .protocol("TLS")
                .keystoreRelativeTo("jboss.server.config.dir")
                .cipherSuitesToEnable("DEFAULT", "ALL")
                .protocolsToEnable("SSL", "TLS")
                .build();

        assertXmlIdentical(REALM_EMPTY, Files.toString(cfg, Charsets.UTF_8));
        client.apply(cmd);
        System.out.println(Files.toString(cfg, Charsets.UTF_8));
        assertXmlIdentical(REALM_WITH_SSL, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void addExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_EMPTY, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddSslServerIdentity cmd1 = new AddSslServerIdentity.Builder("realmName")
                .alias("alias")
                .keystorePath("server.keystore")
                .keystorePassword("password")
                .keyPassword("password")
                .keystoreProvider("JKS")
                .protocol("TLS")
                .keystoreRelativeTo("jboss.server.config.dir")
                .cipherSuitesToEnable("DEFAULT", "ALL")
                .protocolsToEnable("SSL", "TLS")
                .build();

        AddSslServerIdentity cmd2 = new AddSslServerIdentity.Builder("realmName")
                .alias("alias")
                .keystorePath("server2.keystore")
                .keystorePassword("password")
                .keyPassword("password")
                .build();

        client.apply(cmd1);
        client.apply(cmd2);
        fail("Secret authentication already exists in configuration, exception should be thrown");
    }

    @Test
    public void replaceExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_EMPTY, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());


        AddSslServerIdentity cmd1 = new AddSslServerIdentity.Builder("realmName")
                .alias("alias")
                .keystorePath("server2.keystore")
                .keystorePassword("password")
                .keyPassword("password")
                .build();

        AddSslServerIdentity cmd2 = new AddSslServerIdentity.Builder("realmName")
                .alias("alias")
                .keystorePath("server.keystore")
                .keystorePassword("password")
                .keyPassword("password")
                .keystoreProvider("JKS")
                .protocol("TLS")
                .keystoreRelativeTo("jboss.server.config.dir")
                .cipherSuitesToEnable("DEFAULT", "ALL")
                .protocolsToEnable("SSL", "TLS")
                .replaceExisting()
                .build();

        assertXmlIdentical(REALM_EMPTY, Files.toString(cfg, Charsets.UTF_8));
        client.apply(cmd1);
        client.apply(cmd2);
        assertXmlIdentical(REALM_WITH_SSL, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addWithGenerateSelfSignedCert() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_EMPTY, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddSslServerIdentity cmd = new AddSslServerIdentity.Builder("realmName")
                .alias("alias")
                .keystorePath("server.keystore")
                .keystorePassword("password")
                .keyPassword("password")
                .keystoreProvider("JKS")
                .protocol("TLS")
                .keystoreRelativeTo("jboss.server.config.dir")
                .cipherSuitesToEnable("DEFAULT", "ALL")
                .protocolsToEnable("SSL", "TLS")
                .generateSelfSignedCertHost("localhost")
                .build();

        assertXmlIdentical(REALM_EMPTY, Files.toString(cfg, Charsets.UTF_8));
        client.apply(cmd);
        System.out.println(Files.toString(cfg, Charsets.UTF_8));
        assertXmlIdentical(REALM_WITH_SSL_AND_GENERATE_SELF_SIGNED_CERT, Files.toString(cfg, Charsets.UTF_8));
    }
}
