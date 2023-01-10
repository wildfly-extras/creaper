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
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;

public class AddFilteringKeyStoreOfflineTest {

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
            + "                    <filtering-key-store name=\"creaperKeyStore\" alias-filter=\"aliasFilter\" key-store=\"referencedKeyStore\"/>\n"
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
            + "                    <filtering-key-store name=\"creaperKeyStore\" alias-filter=\"aliasFilter\" key-store=\"referencedKeyStore2\"/>\n"
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
            + "                    <filtering-key-store name=\"creaperKeyStore\" alias-filter=\"aliasFilter\" key-store=\"referencedKeyStore\"/>\n"
            + "                    <filtering-key-store name=\"creaperKeyStore2\" alias-filter=\"aliasFilter\" key-store=\"referencedKeyStore\"/>\n"
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

        AddFilteringKeyStore addKeyStore = new AddFilteringKeyStore.Builder("creaperKeyStore")
                .aliasFilter("aliasFilter")
                .keyStore("referencedKeyStore")
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

        AddFilteringKeyStore addKeyStore = new AddFilteringKeyStore.Builder("creaperKeyStore")
                .aliasFilter("aliasFilter")
                .keyStore("referencedKeyStore")
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

        AddFilteringKeyStore addKeyStore = new AddFilteringKeyStore.Builder("creaperKeyStore")
                .aliasFilter("aliasFilter")
                .keyStore("referencedKeyStore")
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

        AddFilteringKeyStore addKeyStore = new AddFilteringKeyStore.Builder("creaperKeyStore")
                .aliasFilter("aliasFilter")
                .keyStore("referencedKeyStore")
                .build();

        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addKeyStore);

        fail("Filtering key store creaperKeyStore already exists in configuration, exception should be thrown");
    }

    @Test
    public void overrideExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddFilteringKeyStore addKeyStore = new AddFilteringKeyStore.Builder("creaperKeyStore")
                .aliasFilter("aliasFilter")
                .keyStore("referencedKeyStore2")
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

        AddFilteringKeyStore addKeyStore = new AddFilteringKeyStore.Builder("creaperKeyStore2")
                .aliasFilter("aliasFilter")
                .keyStore("referencedKeyStore")
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

        AddFilteringKeyStore addKeyStore = new AddFilteringKeyStore.Builder("creaperKeyStore2")
                .aliasFilter("aliasFilter")
                .keyStore("referencedKeyStore")
                .build();

        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addKeyStore);
        assertXmlIdentical(SUBSYSTEM_SECOND_KEY_STORE, Files.toString(cfg, Charsets.UTF_8));
    }

}
