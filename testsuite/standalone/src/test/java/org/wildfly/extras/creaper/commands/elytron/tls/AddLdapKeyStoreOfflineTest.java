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
import org.wildfly.extras.creaper.commands.elytron.tls.AddLdapKeyStore.NewItemAttributeBuilder;
import org.wildfly.extras.creaper.commands.elytron.tls.AddLdapKeyStore.NewItemTemplateBuilder;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;

public class AddLdapKeyStoreOfflineTest {

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
            + "                    <ldap-key-store name=\"creaperKeyStore\" dir-context=\"dirContext\">\n"
            + "                        <search path=\"searchPath\"/>\n"
            + "                    </ldap-key-store>\n"
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
            + "                    <ldap-key-store name=\"creaperKeyStore\" dir-context=\"dirContext\">\n"
            + "                        <search path=\"searchPath2\"/>\n"
            + "                    </ldap-key-store>\n"
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
            + "                    <ldap-key-store name=\"creaperKeyStore\" dir-context=\"dirContext\">\n"
            + "                        <search path=\"searchPath\"/>\n"
            + "                    </ldap-key-store>\n"
            + "                    <ldap-key-store name=\"creaperKeyStore2\" dir-context=\"dirContext\">\n"
            + "                        <search path=\"searchPath\"/>\n"
            + "                    </ldap-key-store>\n"
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
            + "                    <ldap-key-store name=\"creaperKeyStore\" dir-context=\"dirContext\">\n"
            + "                        <search path=\"searchPath\" recursive=\"true\" time-limit=\"30\" filter-alias=\"filterAlias\" "
            + "                                filter-certificate=\"filterCertificate\" filter-iterate=\"filterIterate\"/>\n"
            + "                        <attribute-mapping alias-attribute=\"aliasAttribute\" certificate-attribute=\"certificateAttribute\" "
            + "                                      certificate-type=\"certificateType\" "
            + "                                      certificate-chain-attribute=\"certificateChainAttribute\" "
            + "                                      certificate-chain-encoding=\"certificateChainEncoding\" "
            + "                                      key-attribute=\"someKeyAttribute\" "
            + "                                      key-type=\"someKeyType\" />\n"
            + "                        <new-item-template new-item-path=\"newItemPath\" new-item-rdn=\"newItemRdn\">\n"
            + "                            <attribute name=\"attrA\" value=\"valueA\"/>\n"
            + "                            <attribute name=\"attrB\" value=\"valueB1 valueB2\"/>\n"
            + "                        </new-item-template>\n"
            + "                    </ldap-key-store>\n"
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

        AddLdapKeyStore addLdapKeyStore = new AddLdapKeyStore.Builder("creaperKeyStore")
                .dirContext("dirContext")
                .searchPath("searchPath")
                .build();

        assertXmlIdentical(SUBSYSTEM_EMPTY, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLdapKeyStore);
        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addSimpleToTlsEmpty() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_TLS_EMPTY, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapKeyStore addLdapKeyStore = new AddLdapKeyStore.Builder("creaperKeyStore")
                .dirContext("dirContext")
                .searchPath("searchPath")
                .build();

        assertXmlIdentical(SUBSYSTEM_TLS_EMPTY, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLdapKeyStore);
        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addSimpleToKeyStoresEmpty() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_KEY_STORES_EMPTY, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapKeyStore addLdapKeyStore = new AddLdapKeyStore.Builder("creaperKeyStore")
                .dirContext("dirContext")
                .searchPath("searchPath")
                .build();

        assertXmlIdentical(SUBSYSTEM_KEY_STORES_EMPTY, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLdapKeyStore);
        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void existing() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapKeyStore addLdapKeyStore = new AddLdapKeyStore.Builder("creaperKeyStore")
                .dirContext("dirContext")
                .searchPath("searchPath2")
                .build();

        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLdapKeyStore);

        fail("LDAP key store creaperKeyStore already exists in configuration, exception should be thrown");
    }

    @Test
    public void overrideExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapKeyStore addLdapKeyStore = new AddLdapKeyStore.Builder("creaperKeyStore")
                .dirContext("dirContext")
                .searchPath("searchPath2")
                .replaceExisting()
                .build();

        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLdapKeyStore);
        assertXmlIdentical(SUBSYSTEM_EXPECTED_REPLACE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void overrideNonExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapKeyStore addLdapKeyStore = new AddLdapKeyStore.Builder("creaperKeyStore2")
                .dirContext("dirContext")
                .searchPath("searchPath")
                .replaceExisting()
                .build();

        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLdapKeyStore);
        assertXmlIdentical(SUBSYSTEM_SECOND_KEY_STORE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addSecondKeyStore() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapKeyStore addLdapKeyStore = new AddLdapKeyStore.Builder("creaperKeyStore2")
                .dirContext("dirContext")
                .searchPath("searchPath")
                .build();

        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLdapKeyStore);
        assertXmlIdentical(SUBSYSTEM_SECOND_KEY_STORE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addFullToEmpty() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_EMPTY, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLdapKeyStore addLdapKeyStore = new AddLdapKeyStore.Builder("creaperKeyStore")
                .dirContext("dirContext")
                .searchPath("searchPath")
                .searchRecursive(true)
                .searchTimeLimit(30)
                .filterAlias("filterAlias")
                .filterCertificate("filterCertificate")
                .filterIterate("filterIterate")
                .aliasAttribute("aliasAttribute")
                .certificateAttribute("certificateAttribute")
                .certificateType("certificateType")
                .certificateChainAttribute("certificateChainAttribute")
                .certificateChainEncoding("certificateChainEncoding")
                .keyAttribute("someKeyAttribute")
                .keyType("someKeyType")
                .newItemTemplate(new NewItemTemplateBuilder()
                        .addNewItemAttributes(
                                new NewItemAttributeBuilder().name("attrA").addValues("valueA").build(),
                                new NewItemAttributeBuilder().name("attrB").addValues("valueB1", "valueB2").build())
                        .newItemPath("newItemPath")
                        .newItemRdn("newItemRdn")
                        .build())
                .build();

        assertXmlIdentical(SUBSYSTEM_EMPTY, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLdapKeyStore);
        assertXmlIdentical(SUBSYSTEM_FULL, Files.toString(cfg, Charsets.UTF_8));
    }
}
