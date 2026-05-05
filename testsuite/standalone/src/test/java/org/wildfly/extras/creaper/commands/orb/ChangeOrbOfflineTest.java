package org.wildfly.extras.creaper.commands.orb;

import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;
import static org.wildfly.extras.creaper.XmlAssert.assertXmlSimilar;

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
import java.io.IOException;


public class ChangeOrbOfflineTest {

    private static final String EMPTY = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <profile>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String DEFAULT = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:iiop-openjdk:1.0\">\n"
            + "            <orb socket-binding=\"jacorb\" ssl-socket-binding=\"jacorb-ssl\"/>\n"
            + "            <initializers transactions=\"spec\" security=\"identity\"/>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String FULL = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:iiop-openjdk:1.0\">\n"
            + "            <properties>\n"
            + "                <property name=\"propname\" value=\"supported\" />\n"
            + "            </properties>\n"
            + "            <orb giop-version=\"1.1\" socket-binding=\"jacorb\" ssl-socket-binding=\"jacorb-ssl\" persistent-server-id=\"42\" />\n"
            + "            <initializers security=\"none\" transactions=\"full\" />\n"
            + "            <naming root-context=\"supported\" export-corbaloc=\"false\" />\n"
            + "            <security support-ssl=\"true\" security-domain=\"other\" add-component-via-interceptor=\"false\" client-supports=\"None\""
            + "               client-requires=\"ClientAuth\" server-supports=\"None\" server-requires=\"ClientAuth\" />\n"
            + "            <transport-config integrity=\"supported\" confidentiality=\"supported\" trust-in-target=\"supported\""
            + "               trust-in-client=\"supported\" detect-replay=\"supported\" detect-misordering=\"supported\" />\n"
            + "            <as-context auth-method=\"none\" realm=\"supported\" required=\"true\" />\n"
            + "            <sas-context caller-propagation=\"supported\" />\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        XmlAssert.setNormalizeWhitespace(true);
    }

    @Test
    public void empty() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(EMPTY, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        ChangeOrb cmd = Orb.attributes().build();
        assertXmlIdentical(EMPTY, Files.toString(cfg, Charsets.UTF_8));

        try {
            client.apply(cmd);
        } catch (CommandFailedException cfe) {
            if (!(cfe.getCause() instanceof IllegalStateException)) {
                throw cfe;
            }
        }
    }

    @Test
    public void identity() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(DEFAULT, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        ChangeOrb cmd = Orb.attributes().build();

        assertXmlIdentical(DEFAULT, Files.toString(cfg, Charsets.UTF_8));

        client.apply(cmd);

        assertXmlIdentical(DEFAULT, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void fromDefaultToFull() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(DEFAULT, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        ChangeOrb cmd = Orb.attributes()
            .property("propname", "supported")
            .giopVersion("1.1")
            .socketBinding("jacorb")
            .sslSocketBinding("jacorb-ssl")
            .persistentServerId("42")
            .security(SecurityValues.NONE)
            .supportSsl(true)
            .transactions(TransactionValues.ON)
            .rootContext("supported")
            .exportCorbaloc(false)
            .securityDomain("other")
            .addComponentViaInterceptor(false)
            .clientSupports(AuthValues.NONE)
            .clientRequires(AuthValues.CLIENT_AUTH)
            .serverSupports(AuthValues.NONE)
            .serverRequires(AuthValues.CLIENT_AUTH)
            .integrity(SupportedValues.SUPPORTED)
            .confidentiality(SupportedValues.SUPPORTED)
            .trustInTarget(SupportedValues.SUPPORTED)
            .trustInClient(SupportedValues.SUPPORTED)
            .detectReplay(SupportedValues.SUPPORTED)
            .detectMisordering(SupportedValues.SUPPORTED)
            .authMethodNone()
            .realm("supported")
            .authRequired(true)
            .callerPropagation(SupportedValues.SUPPORTED)
            .build();
        assertXmlIdentical(DEFAULT, Files.toString(cfg, Charsets.UTF_8));

        client.apply(cmd);

        assertXmlSimilar(FULL, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void fromFullToDefault() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(FULL, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        ChangeOrb cmd = Orb.attributes()
            .security(SecurityValues.IDENTITY)
            .transactions(TransactionValues.SPEC)
            .undefineProperty("propname")
            .undefineGiopVersion()
            .undefinePersistentServerId()
            .undefineSupportSsl()
            .undefineRootContext()
            .undefineExportCorbaloc()
            .undefineSecurityDomain()
            .undefineAddComponentViaInterceptor()
            .undefineClientSupports()
            .undefineClientRequires()
            .undefineServerSupports()
            .undefineServerRequires()
            .undefineIntegrity()
            .undefineConfidentiality()
            .undefineTrustInTarget()
            .undefineTrustInClient()
            .undefineDetectReplay()
            .undefineDetectMisordering()
            .undefineAuthMethodNone()
            .undefineRealm()
            .undefineAuthRequired()
            .undefineCallerPropagation()
            .build();

        assertXmlIdentical(FULL, Files.toString(cfg, Charsets.UTF_8));

        client.apply(cmd);

        assertXmlSimilar(DEFAULT, Files.toString(cfg, Charsets.UTF_8));
    }
}
