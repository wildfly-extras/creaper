package org.wildfly.extras.creaper.commands.elytron.tls;

import static org.junit.Assert.fail;
import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wildfly.extras.creaper.XmlAssert;
import org.wildfly.extras.creaper.commands.elytron.CredentialRef.CredentialRefBuilder;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;

public class AddKeyStoreOfflineTest {

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

    private static final String SUBSYSTEM_KEY_STORES_EMPTY = ""
            + "<server xmlns=\"urn:jboss:domain:5.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:wildfly:elytron:1.0\">\n"
            + "            <tls>\n"
            + "                <key-stores>\n"
            + "                </key-stores>\n"
            + "            </tls>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_SIMPLE = ""
            + "<server xmlns=\"urn:jboss:domain:5.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:wildfly:elytron:1.0\">\n"
            + "            <tls>\n"
            + "                <key-stores>\n"
            + "                    <key-store name=\"creaperKeyStore\">\n"
            + "                        <credential-reference clear-text=\"secret\"/>\n"
            + "                        <implementation type=\"jks\"/>\n"
            + "                    </key-store>\n"
            + "                </key-stores>\n"
            + "            </tls>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_EXPECTED_REPLACE = ""
            + "<server xmlns=\"urn:jboss:domain:5.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:wildfly:elytron:1.0\">\n"
            + "            <tls>\n"
            + "                <key-stores>\n"
            + "                    <key-store name=\"creaperKeyStore\">\n"
            + "                        <credential-reference clear-text=\"secret\"/>\n"
            + "                        <implementation type=\"jks\"/>\n"
            + "                        <file path=\"/tmp/keystore.jks\"/>\n"
            + "                    </key-store>\n"
            + "                </key-stores>\n"
            + "            </tls>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_SECOND_KEY_STORE = ""
            + "<server xmlns=\"urn:jboss:domain:5.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:wildfly:elytron:1.0\">\n"
            + "            <tls>\n"
            + "                <key-stores>\n"
            + "                    <key-store name=\"creaperKeyStore\">\n"
            + "                        <credential-reference clear-text=\"secret\"/>\n"
            + "                        <implementation type=\"jks\"/>\n"
            + "                    </key-store>\n"
            + "                    <key-store name=\"creaperKeyStore2\">\n"
            + "                        <credential-reference clear-text=\"secret\"/>\n"
            + "                        <implementation type=\"jks\"/>\n"
            + "                    </key-store>\n"
            + "                </key-stores>\n"
            + "            </tls>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_FULL = ""
            + "<server xmlns=\"urn:jboss:domain:5.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:wildfly:elytron:1.0\">\n"
            + "            <tls>\n"
            + "                <key-stores>\n"
            + "                    <key-store name=\"creaperKeyStore\" alias-filter=\"aliasInFilter\">\n"
            + "                        <credential-reference alias=\"crAlias\" type=\"crType\" store=\"crStore\" clear-text=\"secret\" />\n"
            + "                        <implementation type=\"jks\" provider-name=\"ksProvider\" providers=\"ksProviderLoader\"/>\n"
            + "                        <file path=\"/tmp/keystore.jks\" relative-to=\"relativeToDir\" required=\"true\"/>\n"
            + "                    </key-store>\n"
            + "                </key-stores>\n"
            + "            </tls>\n"
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
    public void addSimpleToEmpty() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_EMPTY, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddKeyStore addKeyStore = new AddKeyStore.Builder("creaperKeyStore")
                .type("jks")
                .credentialReference(new CredentialRefBuilder().clearText("secret").build())
                .build();

        assertXmlIdentical(SUBSYSTEM_EMPTY, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addKeyStore);
        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addSimpleToTlsEmpty() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_TLS_EMPTY, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddKeyStore addKeyStore = new AddKeyStore.Builder("creaperKeyStore")
                .type("jks")
                .credentialReference(new CredentialRefBuilder().clearText("secret").build())
                .build();

        assertXmlIdentical(SUBSYSTEM_TLS_EMPTY, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addKeyStore);
        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addSimpleToKeyStoresEmpty() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_KEY_STORES_EMPTY, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddKeyStore addKeyStore = new AddKeyStore.Builder("creaperKeyStore")
                .type("jks")
                .credentialReference(new CredentialRefBuilder().clearText("secret").build())
                .build();

        assertXmlIdentical(SUBSYSTEM_KEY_STORES_EMPTY, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addKeyStore);
        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void existing() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddKeyStore addKeyStore = new AddKeyStore.Builder("creaperKeyStore")
                .type("jks")
                .credentialReference(new CredentialRefBuilder().clearText("secret").build())
                .build();

        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addKeyStore);

        fail("Key store creaperKeyStore already exists in configuration, exception should be thrown");
    }

    @Test
    public void overrideExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddKeyStore addKeyStore = new AddKeyStore.Builder("creaperKeyStore")
                .type("jks")
                .path("/tmp/keystore.jks")
                .credentialReference(new CredentialRefBuilder().clearText("secret").build())
                .replaceExisting()
                .build();

        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addKeyStore);
        assertXmlIdentical(SUBSYSTEM_EXPECTED_REPLACE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void overrideNonExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddKeyStore addKeyStore = new AddKeyStore.Builder("creaperKeyStore2")
                .type("jks")
                .credentialReference(new CredentialRefBuilder().clearText("secret").build())
                .replaceExisting()
                .build();

        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addKeyStore);
        assertXmlIdentical(SUBSYSTEM_SECOND_KEY_STORE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addSecondKeyStore() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddKeyStore addKeyStore = new AddKeyStore.Builder("creaperKeyStore2")
                .type("jks")
                .credentialReference(new CredentialRefBuilder().clearText("secret").build())
                .build();

        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addKeyStore);
        assertXmlIdentical(SUBSYSTEM_SECOND_KEY_STORE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addFullToEmpty() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_EMPTY, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddKeyStore addKeyStore = new AddKeyStore.Builder("creaperKeyStore")
                .type("jks")
                .providerName("ksProvider")
                .providers("ksProviderLoader")
                .aliasFilter("aliasInFilter")
                .path("/tmp/keystore.jks")
                .relativeTo("relativeToDir")
                .required(true)
                .credentialReference(new CredentialRefBuilder()
                        .alias("crAlias")
                        .type("crType")
                        .store("crStore")
                        .clearText("secret")
                        .build())
                .build();

        assertXmlIdentical(SUBSYSTEM_EMPTY, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addKeyStore);
        assertXmlIdentical(SUBSYSTEM_FULL, Files.toString(cfg, Charsets.UTF_8));
    }
}
