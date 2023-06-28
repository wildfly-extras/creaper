package org.wildfly.extras.creaper.commands.orb;

import static org.junit.Assert.assertEquals;

import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.dmr.ModelNode;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.foundation.offline.ConfigurationFileBackup;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.ReadResourceOption;
import org.wildfly.extras.creaper.test.ManualTests;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Category(ManualTests.class)
@RunWith(Arquillian.class)
public class ChangeOrbOnlineTest {

    private static final String SERVER_CONFIG_FILE = "standalone-full.xml";
    private static final ConfigurationFileBackup CONFIGURATION_BACKUP = new ConfigurationFileBackup();
    private static OfflineManagementClient offlineClient;

    private OnlineManagementClient onlineClient = ManagementClient.onlineLazy(
            OnlineOptions.standalone().localDefault().build());
    private Operations ops = new Operations(onlineClient);

    @ArquillianResource
    private ContainerController controller;

    @Test
    @InSequence(1)
    public void startServer() throws IOException, CommandFailedException {
        offlineClient = ManagementClient.offline(
                OfflineOptions.standalone()
                        .rootDirectory(new File(System.getProperty("user.dir"), "target/jboss-as"))
                        .configurationFile(SERVER_CONFIG_FILE)
                        .build());
        offlineClient.apply(CONFIGURATION_BACKUP.backup());
        Map<String, String> configMap = ImmutableMap.of("serverConfig", SERVER_CONFIG_FILE);
        controller.start(ManualTests.ARQUILLIAN_CONTAINER, configMap);
    }

    @Test
    @InSequence(2)
    public void changeAll() throws Exception {
        boolean isIiop = !onlineClient.version().lessThan(ServerVersion.VERSION_3_0_0);
        Address address = isIiop ? Address.subsystem("iiop-openjdk") : Address.subsystem("jacorb");

        ChangeOrb.Builder cmdBuilder = Orb.attributes()
                .property("propname", "supported")
                .giopVersion(isIiop ? "1.1" : "1")
                .socketBinding("jacorb")
                .sslSocketBinding("jacorb-ssl")
                .persistentServerId("42")
                .security(SecurityValues.NONE)
                .supportSsl(true)
                .transactions(TransactionValues.ON)
                .rootContext("supported")
                .exportCorbaloc(false)
                .addComponentViaInterceptor(false)
                .clientSupports(AuthValues.NONE)
                .clientRequires(AuthValues.CLIENT_AUTH)
                .serverSupports(AuthValues.NONE)
                .serverRequires(AuthValues.CLIENT_AUTH);

        if (onlineClient.version().lessThanOrEqualTo(ServerVersion.VERSION_18_0_0)) {
            // legacy security was removed in WF25
            cmdBuilder.securityDomain("other");
        }

        if (isIiop) {
            cmdBuilder
                .integrity(SupportedValues.SUPPORTED)
                .confidentiality(SupportedValues.SUPPORTED)
                .trustInTarget(SupportedValues.SUPPORTED)
                .trustInClient(SupportedValues.SUPPORTED)
                .detectReplay(SupportedValues.SUPPORTED)
                .detectMisordering(SupportedValues.SUPPORTED)
                .authRequired(true)
                .realm("supported")
                .authMethodNone()
                .callerPropagation(SupportedValues.SUPPORTED);
        }

        onlineClient.apply(cmdBuilder.build());


        Map<String, String> attributes = new HashMap<String, String>();
        Map<String, String> properties = new HashMap<String, String>();
        readAttributes(address, attributes, properties);

        assertEquals(isIiop ? "full" : "on", attributes.get("transactions"));
        assertEquals(isIiop ? "true" : "on", attributes.get("support-ssl"));
        assertEquals("supported", attributes.get("root-context"));
        assertEquals("jacorb", attributes.get("socket-binding"));
        assertEquals("jacorb-ssl", attributes.get("ssl-socket-binding"));
        if (onlineClient.version().lessThanOrEqualTo(ServerVersion.VERSION_18_0_0)) {
            assertEquals("other", attributes.get("security-domain"));
        }
        assertEquals("ClientAuth", attributes.get("client-requires"));
        assertEquals("None", attributes.get("server-supports"));
        assertEquals("ClientAuth", attributes.get("server-requires"));
        assertEquals("None", attributes.get("client-supports"));
        assertEquals(isIiop ? SecurityValues.NONE.openjdk
            : SecurityValues.NONE.jacorb, attributes.get("security"));
        assertEquals(isIiop ? "false" : "off", attributes.get("export-corbaloc"));
        assertEquals(isIiop ? "false" : "off", attributes.get("add-component-via-interceptor"));
        assertEquals(isIiop ? "1.1" : "1", attributes.get(isIiop ? "giop-version" : "giop-minor-version"));
        assertEquals("iiop properties should have only one item", 1, properties.size());
        assertEquals("supported", properties.get("propname"));

        if (isIiop) {
            assertEquals("42", attributes.get("persistent-server-id"));
            // attributes from ior-settings tag can't be set - jacorb model domain issue
            assertEquals("supported", attributes.get("integrity"));
            assertEquals("supported", attributes.get("confidentiality"));
            assertEquals("supported", attributes.get("trust-in-target"));
            assertEquals("supported", attributes.get("trust-in-client"));
            assertEquals("supported", attributes.get("detect-replay"));
            assertEquals("supported", attributes.get("detect-misordering"));
            assertEquals("none", attributes.get("auth-method"));
            assertEquals("supported", attributes.get("realm"));
            assertEquals(isIiop ? "true" : "on", attributes.get("required"));
            assertEquals("supported", attributes.get("caller-propagation"));
        }

        cmdBuilder = Orb.attributes()
                .undefineProperty("propname")
                .undefineGiopVersion()
                .undefineSocketBinding()
                .undefineSslSocketBinding()
                .undefinePersistentServerId()
                .undefineSecurity()
                .undefineSupportSsl()
                .undefineTransactions()
                .undefineRootContext()
                .undefineExportCorbaloc()
                .undefineSecurityDomain()
                .undefineAddComponentViaInterceptor()
                .undefineClientSupports()
                .undefineClientRequires()
                .undefineServerSupports()
                .undefineServerRequires();
        if (isIiop) {
            cmdBuilder
                .undefineIntegrity()
                .undefineConfidentiality()
                .undefineTrustInTarget()
                .undefineTrustInClient()
                .undefineDetectReplay()
                .undefineDetectMisordering()
                .undefineAuthMethodNone()
                .undefineRealm()
                .undefineAuthRequired()
                .undefineCallerPropagation();
        }

        onlineClient.apply(cmdBuilder.build());

        attributes = new HashMap<String, String>();
        properties = new HashMap<String, String>();
        readAttributes(address, attributes, properties);

        assertEquals("undefined", attributes.get("transactions"));
        assertEquals("undefined", attributes.get("support-ssl"));
        assertEquals("undefined", attributes.get("root-context"));
        assertEquals("undefined", attributes.get("socket-binding"));
        assertEquals("undefined", attributes.get("ssl-socket-binding"));
        assertEquals("undefined", attributes.get("security-domain"));
        assertEquals("undefined", attributes.get("server-supports"));
        assertEquals("undefined", attributes.get("server-requires"));
        assertEquals("undefined", attributes.get("client-supports"));
        assertEquals("undefined", attributes.get("client-requires"));
        assertEquals("undefined", attributes.get("security"));
        assertEquals("undefined", attributes.get("export-corbaloc"));
        assertEquals("undefined", attributes.get("add-component-via-interceptor"));
        assertEquals("undefined", attributes.get(isIiop ? "giop-version" : "giop-minor-version"));
        assertEquals("no iiop property expected as were undefined", 0, properties.size());

        if (isIiop) {
            assertEquals("undefined", attributes.get("persistent-server-id"));
            // attributes from ior-settings tag can't be set - jacorb model domain issue
            assertEquals("undefined", attributes.get("integrity"));
            assertEquals("undefined", attributes.get("confidentiality"));
            assertEquals("undefined", attributes.get("trust-in-target"));
            assertEquals("undefined", attributes.get("trust-in-client"));
            assertEquals("undefined", attributes.get("detect-replay"));
            assertEquals("undefined", attributes.get("detect-misordering"));
            assertEquals("undefined", attributes.get("realm"));
            assertEquals("undefined", attributes.get("required"));
            assertEquals("undefined", attributes.get("auth-method"));
            assertEquals("undefined", attributes.get("caller-propagation"));
        }
    }

    @Test
    @InSequence(3)
    public void stopServer() throws CommandFailedException, IOException {
        controller.stop(ManualTests.ARQUILLIAN_CONTAINER);
        offlineClient.apply(CONFIGURATION_BACKUP.restore());
    }

    private void readAttributes(Address address, Map<String, String> attributes, Map<String, String> properties)
            throws IOException {
        ModelNodeResult readResourceResult = ops.readResource(address, ReadResourceOption.NOT_INCLUDE_DEFAULTS);
        readResourceResult.assertSuccess();
        for (ModelNode property : readResourceResult.value().asList()) {
            attributes.put(property.asProperty().getName(), property.asProperty().getValue().asString());
            if (property.asProperty().getName().equals("properties")) {
                if (property.asProperty().getValue().isDefined()) {
                    for (ModelNode iiopProperty : property.asProperty().getValue().asList()) {
                        properties.put(iiopProperty.asProperty().getName(),
                                iiopProperty.asProperty().getValue().asString());
                    }
                }
            }
        }
    }
}
