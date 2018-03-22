package org.wildfly.extras.creaper.commands.elytron.tls;

import static org.junit.Assert.fail;
import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;

import java.io.File;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wildfly.extras.creaper.commands.elytron.CredentialRef.CredentialRefBuilder;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class AddKeyManagerOfflineTest {

    private static final String SUBSYSTEM_EMPTY = ""
            + "<server xmlns=\"urn:jboss:domain:5.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:wildfly:elytron:1.0\">\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_TLS_EMPTY = ""
            + "<server xmlns=\"urn:jboss:domain:5.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:wildfly:elytron:1.0\">\n"
            + "            <tls>\n"
            + "            </tls>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_KEY_MANAGERS_EMPTY = ""
            + "<server xmlns=\"urn:jboss:domain:5.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:wildfly:elytron:1.0\">\n"
            + "            <tls>\n"
            + "                <key-managers>\n"
            + "                </key-managers>\n"
            + "            </tls>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_SIMPLE = ""
            + "<server xmlns=\"urn:jboss:domain:5.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:wildfly:elytron:1.0\">\n"
            + "            <tls>\n"
            + "                <key-managers>\n"
            + "                    <key-manager name=\"creaperKeyManager\" key-store=\"someKeyStore\">\n"
            + "                        <credential-reference clear-text=\"secret\"/>\n"
            + "                    </key-manager>\n"
            + "                </key-managers>\n"
            + "            </tls>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_EXPECTED_REPLACE = ""
            + "<server xmlns=\"urn:jboss:domain:5.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:wildfly:elytron:1.0\">\n"
            + "            <tls>\n"
            + "                <key-managers>\n"
            + "                    <key-manager name=\"creaperKeyManager\" key-store=\"someKeyStore\">\n"
            + "                        <credential-reference clear-text=\"secret2\"/>\n"
            + "                    </key-manager>\n"
            + "                </key-managers>\n"
            + "            </tls>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_SECOND_KEY_MANAGER = ""
            + "<server xmlns=\"urn:jboss:domain:5.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:wildfly:elytron:1.0\">\n"
            + "            <tls>\n"
            + "                <key-managers>\n"
            + "                    <key-manager name=\"creaperKeyManager\" key-store=\"someKeyStore\">\n"
            + "                        <credential-reference clear-text=\"secret\"/>\n"
            + "                    </key-manager>\n"
            + "                    <key-manager name=\"creaperKeyManager2\" key-store=\"someKeyStore\">\n"
            + "                        <credential-reference clear-text=\"secret\"/>\n"
            + "                    </key-manager>\n"
            + "                </key-managers>\n"
            + "            </tls>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_FULL = ""
            + "<server xmlns=\"urn:jboss:domain:5.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:wildfly:elytron:1.0\">\n"
            + "            <tls>\n"
            + "                <key-managers>\n"
            + "                    <key-manager name=\"creaperKeyManager\" algorithm=\"SunX509\" key-store=\"someKeyStore\" "
            + "                                provider-name=\"ksProvider\" providers=\"ksProviderLoader\" alias-filter=\"aliasInFilter\">\n"
            + "                        <credential-reference alias=\"crAlias\" type=\"crType\" store=\"crStore\" clear-text=\"secret\" />\n"
            + "                    </key-manager>\n"
            + "                </key-managers>\n"
            + "            </tls>\n"
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
    public void addSimpleToEmpty() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_EMPTY, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddKeyManager addKeyManager = new AddKeyManager.Builder("creaperKeyManager")
                .keyStore("someKeyStore")
                .credentialReference(new CredentialRefBuilder().clearText("secret").build())
                .build();

        assertXmlIdentical(SUBSYSTEM_EMPTY, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addKeyManager);
        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addSimpleToTlsEmpty() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_TLS_EMPTY, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddKeyManager addKeyManager = new AddKeyManager.Builder("creaperKeyManager")
                .keyStore("someKeyStore")
                .credentialReference(new CredentialRefBuilder().clearText("secret").build())
                .build();

        assertXmlIdentical(SUBSYSTEM_TLS_EMPTY, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addKeyManager);
        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addSimpleToKeyManagerEmpty() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_KEY_MANAGERS_EMPTY, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddKeyManager addKeyManager = new AddKeyManager.Builder("creaperKeyManager")
                .keyStore("someKeyStore")
                .credentialReference(new CredentialRefBuilder().clearText("secret").build())
                .build();

        assertXmlIdentical(SUBSYSTEM_KEY_MANAGERS_EMPTY, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addKeyManager);
        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void existing() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddKeyManager addKeyManager = new AddKeyManager.Builder("creaperKeyManager")
                .keyStore("someKeyStore")
                .credentialReference(new CredentialRefBuilder().clearText("secret").build())
                .build();

        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addKeyManager);

        fail("Key manager creaperKeyManager already exists in configuration, exception should be thrown");
    }

    @Test
    public void overrideExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddKeyManager addKeyManager = new AddKeyManager.Builder("creaperKeyManager")
                .keyStore("someKeyStore")
                .credentialReference(new CredentialRefBuilder().clearText("secret2").build())
                .replaceExisting()
                .build();

        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addKeyManager);
        assertXmlIdentical(SUBSYSTEM_EXPECTED_REPLACE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void overrideNonExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddKeyManager addKeyManager = new AddKeyManager.Builder("creaperKeyManager2")
                .keyStore("someKeyStore")
                .credentialReference(new CredentialRefBuilder().clearText("secret").build())
                .replaceExisting()
                .build();

        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addKeyManager);
        assertXmlIdentical(SUBSYSTEM_SECOND_KEY_MANAGER, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addSecondKeyManager() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddKeyManager addKeyManager = new AddKeyManager.Builder("creaperKeyManager2")
                .keyStore("someKeyStore")
                .credentialReference(new CredentialRefBuilder().clearText("secret").build())
                .build();

        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addKeyManager);
        assertXmlIdentical(SUBSYSTEM_SECOND_KEY_MANAGER, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addFullToEmpty() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_EMPTY, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddKeyManager addKeyManager = new AddKeyManager.Builder("creaperKeyManager")
                .algorithm("SunX509")
                .aliasFilter("aliasInFilter")
                .keyStore("someKeyStore")
                .providerName("ksProvider")
                .providers("ksProviderLoader")
                .credentialReference(new CredentialRefBuilder()
                        .alias("crAlias")
                        .type("crType")
                        .store("crStore")
                        .clearText("secret")
                        .build())
                .build();

        assertXmlIdentical(SUBSYSTEM_EMPTY, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addKeyManager);
        assertXmlIdentical(SUBSYSTEM_FULL, Files.toString(cfg, Charsets.UTF_8));
    }
}
