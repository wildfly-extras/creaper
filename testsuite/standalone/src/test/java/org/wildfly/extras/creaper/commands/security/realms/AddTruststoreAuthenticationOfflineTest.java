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

public class AddTruststoreAuthenticationOfflineTest {

    private static final String REALM_EMPTY = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"realmName\"/>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";


    private static final String REALM_WITH_TRUSTSTORE = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"realmName\">\n"
            + "                <authentication>\n"
            + "                   <truststore path=\"server.truststore\" relative-to=\"jboss.server.config.dir\" "
            + "                               keystore-password=\"password\" provider=\"JKS\"/>\n"
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

        AddTruststoreAuthentication cmd = new AddTruststoreAuthentication.Builder("realmName")
                .truststorePassword("password")
                .truststorePath("server.truststore")
                .truststoreRelativeTo("jboss.server.config.dir")
                .truststoreProvider("JKS")
                .build();

        assertXmlIdentical(REALM_EMPTY, Files.toString(cfg, Charsets.UTF_8));
        client.apply(cmd);
        assertXmlIdentical(REALM_WITH_TRUSTSTORE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void addExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_EMPTY, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddTruststoreAuthentication cmd1 = new AddTruststoreAuthentication.Builder("realmName")
                .truststorePassword("password")
                .truststorePath("server.truststore")
                .truststoreRelativeTo("jboss.server.config.dir")
                .truststoreProvider("JKS")
                .build();

        AddTruststoreAuthentication cmd2 = new AddTruststoreAuthentication.Builder("realmName")
                .truststorePassword("password")
                .truststorePath("server2.truststore")
                .truststoreRelativeTo("jboss.server.config.dir")
                .truststoreProvider("JKS")
                .build();

        client.apply(cmd1);
        client.apply(cmd2);
        fail("Truststore authentication already exists in configuration, exception should be thrown");
    }

    @Test
    public void replaceExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_EMPTY, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());


        AddTruststoreAuthentication cmd1 = new AddTruststoreAuthentication.Builder("realmName")
                .truststorePassword("password")
                .truststorePath("server2.truststore")
                .truststoreRelativeTo("jboss.server.config.dir")
                .truststoreProvider("JKS")
                .build();

        AddTruststoreAuthentication cmd2 = new AddTruststoreAuthentication.Builder("realmName")
                .truststorePassword("password")
                .truststorePath("server.truststore")
                .truststoreRelativeTo("jboss.server.config.dir")
                .truststoreProvider("JKS")
                .replaceExisting()
                .build();

        assertXmlIdentical(REALM_EMPTY, Files.toString(cfg, Charsets.UTF_8));
        client.apply(cmd1);
        client.apply(cmd2);
        assertXmlIdentical(REALM_WITH_TRUSTSTORE, Files.toString(cfg, Charsets.UTF_8));
    }
}
