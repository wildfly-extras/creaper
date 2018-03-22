package org.wildfly.extras.creaper.commands.elytron.tls;

import static org.junit.Assert.fail;
import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;

import java.io.File;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class AddClientSSLContextOfflineTest {

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

    private static final String SUBSYSTEM_CLIENT_SSL_CONTEXTS_EMPTY = ""
            + "<server xmlns=\"urn:jboss:domain:5.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:wildfly:elytron:1.0\">\n"
            + "            <tls>\n"
            + "                <client-ssl-contexts>\n"
            + "                </client-ssl-contexts>\n"
            + "            </tls>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_SIMPLE = ""
            + "<server xmlns=\"urn:jboss:domain:5.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:wildfly:elytron:1.0\">\n"
            + "            <tls>\n"
            + "                <client-ssl-contexts>\n"
            + "                    <client-ssl-context name=\"clientSslContext\"/>\n"
            + "                </client-ssl-contexts>\n"
            + "            </tls>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_EXPECTED_REPLACE = ""
            + "<server xmlns=\"urn:jboss:domain:5.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:wildfly:elytron:1.0\">\n"
            + "            <tls>\n"
            + "                <client-ssl-contexts>\n"
            + "                    <client-ssl-context name=\"clientSslContext\" cipher-suite-filter=\"ALL\"/>\n"
            + "                </client-ssl-contexts>\n"
            + "            </tls>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_SECOND_CLIENT_SSL_CONTEXT = ""
            + "<server xmlns=\"urn:jboss:domain:5.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:wildfly:elytron:1.0\">\n"
            + "            <tls>\n"
            + "                <client-ssl-contexts>\n"
            + "                    <client-ssl-context name=\"clientSslContext\"/>\n"
            + "                    <client-ssl-context name=\"clientSslContext2\"/>\n"
            + "                </client-ssl-contexts>\n"
            + "            </tls>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_FULL = ""
            + "<server xmlns=\"urn:jboss:domain:5.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:wildfly:elytron:1.0\">\n"
            + "            <tls>\n"
            + "                <client-ssl-contexts>\n"
            + "                    <client-ssl-context name=\"clientSslContext\" cipher-suite-filter=\"ALL\" "
            + "                                        key-manager=\"keyManager\" trust-manager=\"trustManager\""
            + "                                        protocols=\"TLSv1.2 TLSv1.1\" provider-name=\"ksProvider\" providers=\"ksProviderLoader\"/>\n"
            + "                </client-ssl-contexts>\n"
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

        AddClientSSLContext addClientSslContext = new AddClientSSLContext.Builder("clientSslContext").build();

        assertXmlIdentical(SUBSYSTEM_EMPTY, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addClientSslContext);
        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addSimpleToTlsEmpty() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_TLS_EMPTY, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddClientSSLContext addClientSslContext = new AddClientSSLContext.Builder("clientSslContext").build();

        assertXmlIdentical(SUBSYSTEM_TLS_EMPTY, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addClientSslContext);
        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addSimpleToClientSslContextEmpty() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_CLIENT_SSL_CONTEXTS_EMPTY, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddClientSSLContext addClientSslContext = new AddClientSSLContext.Builder("clientSslContext").build();

        assertXmlIdentical(SUBSYSTEM_CLIENT_SSL_CONTEXTS_EMPTY, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addClientSslContext);
        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void existing() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddClientSSLContext addClientSslContext = new AddClientSSLContext.Builder("clientSslContext").build();

        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addClientSslContext);

        fail("Client SSL context clientSslContext already exists in configuration, exception should be thrown");
    }

    @Test
    public void overrideExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddClientSSLContext addClientSslContext = new AddClientSSLContext.Builder("clientSslContext")
                .cipherSuiteFilter("ALL")
                .replaceExisting()
                .build();

        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addClientSslContext);
        assertXmlIdentical(SUBSYSTEM_EXPECTED_REPLACE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void overrideNonExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddClientSSLContext addClientSslContext = new AddClientSSLContext.Builder("clientSslContext2")
                .replaceExisting()
                .build();

        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addClientSslContext);
        assertXmlIdentical(SUBSYSTEM_SECOND_CLIENT_SSL_CONTEXT, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addSecondContext() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_SIMPLE, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddClientSSLContext addClientSslContext = new AddClientSSLContext.Builder("clientSslContext2")
                .replaceExisting()
                .build();

        assertXmlIdentical(SUBSYSTEM_SIMPLE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addClientSslContext);
        assertXmlIdentical(SUBSYSTEM_SECOND_CLIENT_SSL_CONTEXT, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addFullToEmpty() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_EMPTY, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddClientSSLContext addClientSslContext = new AddClientSSLContext.Builder("clientSslContext")
                .cipherSuiteFilter("ALL")
                .keyManager("keyManager")
                .trustManager("trustManager")
                .protocols("TLSv1.2", "TLSv1.1")
                .providerName("ksProvider")
                .providers("ksProviderLoader")
                .build();

        assertXmlIdentical(SUBSYSTEM_EMPTY, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addClientSslContext);
        assertXmlIdentical(SUBSYSTEM_FULL, Files.toString(cfg, Charsets.UTF_8));
    }
}
