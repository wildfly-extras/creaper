package org.wildfly.extras.creaper.commands.foundation.offline.xml;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.custommonkey.xmlunit.XMLUnit;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;

@RunWith(Parameterized.class)
public class GroovyXmlTransformStandaloneTest {
    private static final String EXTENSIONS_ORIGINAL = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <extensions>\n"
            + "        <extension module=\"org.jboss.as.ee\"/>\n"
            + "    </extensions>\n"
            + "</server>";
    private static final String EXTENSIONS_EXPECTED = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <extensions>\n"
            + "        <extension module=\"org.jboss.as.ee\"/>\n"
            + "        <extension module=\"org.jboss.as.ejb3\"/>\n"
            + "        <extension module=\"org.jboss.as.jaxrs\"/>"
            + "    </extensions>\n"
            + "</server>";

    private static final String SYSTEM_PROPERTIES_ORIGINAL = "<server xmlns=\"urn:jboss:domain:1.7\"/>";
    private static final String SYSTEM_PROPERTIES_EXPECTED = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <system-properties>\n"
            + "        <property name=\"foo\" value=\"bar\"/>\n"
            + "    </system-properties>\n"
            + "</server>";

    private static final String PATHS_ORIGINAL = "<server xmlns=\"urn:jboss:domain:1.7\"/>";
    private static final String PATHS_EXPECTED = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <paths>\n"
            + "        <path name=\"foobar\" path=\"foobar\" relative-to=\"jboss.home.dir\"/>\n"
            + "    </paths>\n"
            + "</server>";

    private static final String MANAGEMENT_ORIGINAL = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <access-control provider=\"simple\"/>\n"
            + "    </management>\n"
            + "</server>";
    private static final String MANAGEMENT_EXPECTED = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <access-control provider=\"rbac\"/>\n"
            + "    </management>\n"
            + "</server>";

    private static final String PROFILE_ORIGINAL = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:logging:1.5\"/>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:datasources:1.2\"/>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:deployment-scanner:1.1\"/>\n"
            + "    </profile>\n"
            + "</server>";
    private static final String PROFILE_EXPECTED = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:logging:1.5\"/>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:datasources:1.2\"/>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:deployment-scanner:1.1\"/>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:ee:1.2\"/>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYTEM_ORIGINAL = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:datasources:1.2\">\n"
            + "            <datasources>\n"
            + "                <datasource jndi-name=\"java:jboss/datasources/ExampleDS\" pool-name=\"ExampleDS\" enabled=\"true\" use-java-context=\"true\">\n"
            + "                    <connection-url>jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE</connection-url>\n"
            + "                    <driver>h2</driver>\n"
            + "                    <security>\n"
            + "                        <user-name>sa</user-name>\n"
            + "                        <password>sa</password>\n"
            + "                    </security>\n"
            + "                </datasource>\n"
            + "            </datasources>\n"
            + "        </subsystem>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:ee:1.2\">\n"
            + "            <annotation-property-replacement>false</annotation-property-replacement>\n"
            + "        </subsystem>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:infinispan:1.5\">\n"
            + "            <cache-container name=\"web\" aliases=\"standard-session-cache\" default-cache=\"repl\" module=\"org.jboss.as.clustering.web.infinispan\">\n"
            + "                <replicated-cache name=\"repl\" mode=\"ASYNC\" batching=\"true\">\n"
            + "                    <file-store/>\n"
            + "                </replicated-cache>\n"
            + "                <replicated-cache name=\"sso\" mode=\"SYNC\" batching=\"true\"/>\n"
            + "                <distributed-cache name=\"dist\" mode=\"ASYNC\" batching=\"true\" l1-lifespan=\"0\">\n"
            + "                    <file-store/>\n"
            + "                </distributed-cache>\n"
            + "            </cache-container>\n"
            + "            <cache-container name=\"ejb\"/>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";
    private static final String SUBSYTEM_EXPECTED = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:datasources:1.2\">\n"
            + "            <datasources>\n"
            + "                <datasource jndi-name=\"java:jboss/datasources/ExampleDS\" pool-name=\"ExampleDS\" enabled=\"true\" use-java-context=\"true\">\n"
            + "                    <connection-url>jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE</connection-url>\n"
            + "                    <driver>h2</driver>\n"
            + "                    <security>\n"
            + "                        <user-name>my-user-name</user-name>\n"
            + "                        <password>my-password</password>\n"
            + "                    </security>\n"
            + "                </datasource>\n"
            + "            </datasources>\n"
            + "        </subsystem>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:ee:1.2\">\n"
            + "            <annotation-property-replacement>true</annotation-property-replacement>\n"
            + "        </subsystem>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:infinispan:1.5\">\n"
            + "            <cache-container name=\"web\" aliases=\"standard-session-cache\" default-cache=\"dist\" module=\"org.jboss.as.clustering.web.infinispan\">\n"
            + "                <replicated-cache name=\"repl\" mode=\"SYNC\" batching=\"false\">\n"
            + "                    <file-store/>\n"
            + "                </replicated-cache>\n"
            + "                <replicated-cache name=\"sso\" mode=\"SYNC\" batching=\"false\"/>\n"
            + "                <distributed-cache name=\"dist\" mode=\"SYNC\" batching=\"false\" l1-lifespan=\"0\">\n"
            + "                    <file-store/>\n"
            + "                </distributed-cache>\n"
            + "            </cache-container>\n"
            + "            <cache-container name=\"ejb\"/>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String INTERFACES_ORIGINAL = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <interfaces>\n"
            + "        <interface name=\"management\">\n"
            + "            <inet-address value=\"${jboss.bind.address.management:127.0.0.1}\"/>\n"
            + "        </interface>\n"
            + "        <interface name=\"public\">\n"
            + "            <inet-address value=\"${jboss.bind.address:127.0.0.1}\"/>\n"
            + "        </interface>\n"
            + "    </interfaces>\n"
            + "</server>";
    private static final String INTERFACES_EXPECTED = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <interfaces>\n"
            + "        <interface name=\"management\">\n"
            + "            <inet-address value=\"10.1.2.3\"/>\n"
            + "        </interface>\n"
            + "        <interface name=\"public\">\n"
            + "            <inet-address value=\"10.1.2.3\"/>\n"
            + "        </interface>\n"
            + "    </interfaces>\n"
            + "</server>";

    private static final String SOCKET_BINDING_GROUP_ORIGINAL = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <socket-binding-group name=\"standard-sockets\" default-interface=\"public\" port-offset=\"${jboss.socket.binding.port-offset:0}\">\n"
            + "        <socket-binding name=\"http\" port=\"8080\"/>\n"
            + "        <outbound-socket-binding name=\"mail-smtp\">\n"
            + "            <remote-destination host=\"localhost\" port=\"25\"/>\n"
            + "        </outbound-socket-binding>\n"
            + "    </socket-binding-group>\n"
            + "</server>";
    private static final String SOCKET_BINDING_GROUP_EXPECTED = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <socket-binding-group name=\"standard-sockets\" default-interface=\"public\" port-offset=\"${jboss.socket.binding.port-offset:0}\">\n"
            + "        <socket-binding name=\"http\" port=\"10080\"/>\n"
            + "        <socket-binding name=\"https\" port=\"10443\"/>\n"
            + "        <outbound-socket-binding name=\"mail-smtp\">\n"
            + "            <remote-destination host=\"localhost\" port=\"587\"/>\n"
            + "        </outbound-socket-binding>\n"
            + "    </socket-binding-group>\n"
            + "</server>";

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][]{
                {"extensions", EXTENSIONS_ORIGINAL, EXTENSIONS_EXPECTED, ExampleSubtreeExtensionsTransformation.class},
                {"systemProperties", SYSTEM_PROPERTIES_ORIGINAL, SYSTEM_PROPERTIES_EXPECTED,
                        ExampleSubtreeSystemPropertiesTransformation.class},
                {"paths", PATHS_ORIGINAL, PATHS_EXPECTED, ExampleSubtreePathsTransformation.class},
                {"management", MANAGEMENT_ORIGINAL, MANAGEMENT_EXPECTED, ExampleSubtreeManagementTransformation.class},
                {"profile", PROFILE_ORIGINAL, PROFILE_EXPECTED, ExampleSubtreeProfileTransformation.class},
                {"subsystem", SUBSYTEM_ORIGINAL, SUBSYTEM_EXPECTED, ExampleSubtreeSubsystemTransformation.class},
                {"interfaces", INTERFACES_ORIGINAL, INTERFACES_EXPECTED, ExampleSubtreeInterfacesTransformation.class},
                {"socketBindingGroup", SOCKET_BINDING_GROUP_ORIGINAL, SOCKET_BINDING_GROUP_EXPECTED,
                        ExampleSubtreeSocketBindingGroupTransformation.class},
        });
    }

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Parameterized.Parameter(0)
    public String testName; // unused, but JUnit insists on that

    @Parameterized.Parameter(1)
    public String originalXml;

    @Parameterized.Parameter(2)
    public String expectedXml;

    @Parameterized.Parameter(3)
    public Class<? extends OfflineCommand> transformationCommand;

    @BeforeClass
    public static void setUpXmlUnit() {
        XMLUnit.setIgnoreWhitespace(true);
    }

    @Test
    public void transform() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(originalXml, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(originalXml, Files.toString(cfg, Charsets.UTF_8));
        client.apply(transformationCommand.newInstance());
        assertXmlIdentical(expectedXml, Files.toString(cfg, Charsets.UTF_8));
    }
}
