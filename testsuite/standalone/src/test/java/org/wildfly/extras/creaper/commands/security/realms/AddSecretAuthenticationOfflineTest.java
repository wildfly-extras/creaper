package org.wildfly.extras.creaper.commands.security.realms;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wildfly.extras.creaper.XmlAssert;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;

import java.io.File;

import static org.junit.Assert.fail;
import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;

public class AddSecretAuthenticationOfflineTest {

    private static final String REALM_EMPTY = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"realmName\"/>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REALM_WITH_SECRET = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <security-realms>\n"
            + "            <security-realm name=\"realmName\">\n"
            + "                <server-identities>\n"
            + "                    <secret value=\"cGFzc3dvcmQx\"/>\n"
            + "                </server-identities>\n"
            + "            </security-realm>\n"
            + "        </security-realms>\n"
            + "    </management>\n"
            + "</server>";

    private static final String PASSWORD = "password1";

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Before
    public void setUp() {
        XmlAssert.setNormalizeWhitespace(true);
    }

    @Test
    public void addSimple() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_EMPTY, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddSecretAuthentication cmd = new AddSecretAuthentication.Builder("realmName")
                .password("password1")
                .build();

        assertXmlIdentical(REALM_EMPTY, Files.toString(cfg, Charsets.UTF_8));
        client.apply(cmd);
        assertXmlIdentical(REALM_WITH_SECRET, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void addExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(REALM_EMPTY, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddSecretAuthentication cmd1 = new AddSecretAuthentication.Builder("realmName")
                .password("password1")
                .build();

        AddSecretAuthentication cmd2 = new AddSecretAuthentication.Builder("realmName")
                .password("password2")
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


        AddSecretAuthentication cmd1 = new AddSecretAuthentication.Builder("realmName")
                .password("password2")
                .build();

        AddSecretAuthentication cmd2 = new AddSecretAuthentication.Builder("realmName")
                .password("password1")
                .replaceExisting()
                .build();

        assertXmlIdentical(REALM_EMPTY, Files.toString(cfg, Charsets.UTF_8));
        client.apply(cmd1);
        client.apply(cmd2);
        assertXmlIdentical(REALM_WITH_SECRET, Files.toString(cfg, Charsets.UTF_8));
    }
}
