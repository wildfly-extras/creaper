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
public class GroovyXmlTransformDomainTest {
    // universal (both domain.xml and host.xml)
    // most of it is actually shared with GroovyXmlTransformStandaloneTest, which proves that the Subtree concept works

    private static final String EXTENSIONS_ORIGINAL = ""
            + "<domain xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <extensions>\n"
            + "        <extension module=\"org.jboss.as.ee\"/>\n"
            + "    </extensions>\n"
            + "</domain>";
    private static final String EXTENSIONS_EXPECTED = ""
            + "<domain xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <extensions>\n"
            + "        <extension module=\"org.jboss.as.ee\"/>\n"
            + "        <extension module=\"org.jboss.as.ejb3\"/>\n"
            + "        <extension module=\"org.jboss.as.jaxrs\"/>"
            + "    </extensions>\n"
            + "</domain>";

    private static final String SYSTEM_PROPERTIES_HOST_ORIGINAL = "<host xmlns=\"urn:jboss:domain:1.7\"/>";
    private static final String SYSTEM_PROPERTIES_HOST_EXPECTED = ""
            + "<host xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <system-properties>\n"
            + "        <property name=\"foo\" value=\"bar\"/>\n"
            + "    </system-properties>\n"
            + "</host>";

    private static final String SYSTEM_PROPERTIES_DOMAIN_ORIGINAL = ""
            + "<domain xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <system-properties>\n"
            + "        <property name=\"java.net.preferIPv4Stack\" value=\"true\"/>\n"
            + "    </system-properties>\n"
            + "</domain>";
    private static final String SYSTEM_PROPERTIES_DOMAIN_EXPECTED = ""
            + "<domain xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <system-properties>\n"
            + "        <property name=\"java.net.preferIPv4Stack\" value=\"true\"/>\n"
            + "        <property name=\"foo\" value=\"bar\"/>\n"
            + "    </system-properties>\n"
            + "</domain>";

    private static final String PATHS_HOST_ORIGINAL = "<host xmlns=\"urn:jboss:domain:1.7\"/>";
    private static final String PATHS_HOST_EXPECTED = ""
            + "<host xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <paths>\n"
            + "        <path name=\"foobar\" path=\"foobar\" relative-to=\"jboss.home.dir\"/>\n"
            + "    </paths>\n"
            + "</host>";

    private static final String PATHS_DOMAIN_ORIGINAL = "<domain xmlns=\"urn:jboss:domain:1.7\"/>";
    private static final String PATHS_DOMAIN_EXPECTED = ""
            + "<domain xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <paths>\n"
            + "        <path name=\"foobar\" path=\"foobar\" relative-to=\"jboss.home.dir\"/>\n"
            + "    </paths>\n"
            + "</domain>";

    private static final String MANAGEMENT_HOST_ORIGINAL = ""
            + "<host name=\"master\" xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <management-interfaces>\n"
            + "            <native-interface security-realm=\"ManagementRealm\">\n"
            + "                <socket interface=\"management\" port=\"${jboss.management.native.port:9999}\"/>\n"
            + "            </native-interface>\n"
            + "        </management-interfaces>\n"
            + "    </management>\n"
            + "</host>";
    private static final String MANAGEMENT_HOST_EXPECTED = ""
            + "<host name=\"master\" xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <management-interfaces>\n"
            + "            <native-interface security-realm=\"ManagementRealm\">\n"
            + "                <socket interface=\"management\" port=\"${jboss.management.native.port:9999}\"/>\n"
            + "            </native-interface>\n"
            + "            <http-interface security-realm=\"ManagementRealm\">\n"
            + "                <socket interface=\"management\" port=\"${jboss.management.http.port:9990}\"/>\n"
            + "            </http-interface>\n"
            + "        </management-interfaces>\n"
            + "    </management>\n"
            + "</host>";

    private static final String MANAGEMENT_DOMAIN_ORIGINAL = ""
            + "<domain xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <access-control provider=\"simple\"/>\n"
            + "    </management>\n"
            + "</domain>";
    private static final String MANAGEMENT_DOMAIN_EXPECTED = ""
            + "<domain xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <access-control provider=\"rbac\"/>\n"
            + "    </management>\n"
            + "</domain>";

    private static final String PROFILE_ORIGINAL = ""
            + "<domain xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profiles>\n"
            + "        <profile name=\"default\">\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:logging:1.5\"/>\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:datasources:1.2\"/>\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:deployment-scanner:1.1\"/>\n"
            + "        </profile>\n"
            + "        <profile name=\"whatever\">\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:logging:1.5\"/>\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:datasources:1.2\"/>\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:deployment-scanner:1.1\"/>\n"
            + "        </profile>\n"
            + "    </profiles>\n"
            + "</domain>";
    private static final String PROFILE_EXPECTED = ""
            + "<domain xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profiles>\n"
            + "        <profile name=\"default\">\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:logging:1.5\"/>\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:datasources:1.2\"/>\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:deployment-scanner:1.1\"/>\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:ee:1.2\"/>\n"
            + "        </profile>\n"
            + "        <profile name=\"whatever\">\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:logging:1.5\"/>\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:datasources:1.2\"/>\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:deployment-scanner:1.1\"/>\n"
            + "        </profile>\n"
            + "    </profiles>\n"
            + "</domain>";

    private static final String SUBSYTEM_ORIGINAL = ""
            + "<domain xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profiles>\n"
            + "        <profile name=\"default\">\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:datasources:1.2\">\n"
            + "                <datasources>\n"
            + "                    <datasource jndi-name=\"java:jboss/datasources/ExampleDS\" pool-name=\"ExampleDS\" enabled=\"true\" use-java-context=\"true\">\n"
            + "                        <connection-url>jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE</connection-url>\n"
            + "                        <driver>h2</driver>\n"
            + "                        <security>\n"
            + "                            <user-name>sa</user-name>\n"
            + "                            <password>sa</password>\n"
            + "                        </security>\n"
            + "                    </datasource>\n"
            + "                </datasources>\n"
            + "            </subsystem>\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:ee:1.2\">\n"
            + "                <annotation-property-replacement>false</annotation-property-replacement>\n"
            + "            </subsystem>\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:infinispan:1.5\">\n"
            + "                <cache-container name=\"web\" aliases=\"standard-session-cache\" default-cache=\"repl\" module=\"org.jboss.as.clustering.web.infinispan\">\n"
            + "                    <replicated-cache name=\"repl\" mode=\"ASYNC\" batching=\"true\">\n"
            + "                        <file-store/>\n"
            + "                    </replicated-cache>\n"
            + "                    <replicated-cache name=\"sso\" mode=\"SYNC\" batching=\"true\"/>\n"
            + "                    <distributed-cache name=\"dist\" mode=\"ASYNC\" batching=\"true\" l1-lifespan=\"0\">\n"
            + "                        <file-store/>\n"
            + "                    </distributed-cache>\n"
            + "                </cache-container>\n"
            + "                <cache-container name=\"ejb\"/>\n"
            + "            </subsystem>\n"
            + "        </profile>\n"
            + "        <profile name=\"whatever\">\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:logging:1.5\"/>\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:datasources:1.2\"/>\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:deployment-scanner:1.1\"/>\n"
            + "        </profile>\n"
            + "    </profiles>\n"
            + "</domain>";
    private static final String SUBSYTEM_EXPECTED = ""
            + "<domain xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profiles>\n"
            + "        <profile name=\"default\">\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:datasources:1.2\">\n"
            + "                <datasources>\n"
            + "                    <datasource jndi-name=\"java:jboss/datasources/ExampleDS\" pool-name=\"ExampleDS\" enabled=\"true\" use-java-context=\"true\">\n"
            + "                        <connection-url>jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE</connection-url>\n"
            + "                        <driver>h2</driver>\n"
            + "                        <security>\n"
            + "                            <user-name>my-user-name</user-name>\n"
            + "                            <password>my-password</password>\n"
            + "                        </security>\n"
            + "                    </datasource>\n"
            + "                </datasources>\n"
            + "            </subsystem>\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:ee:1.2\">\n"
            + "                <annotation-property-replacement>true</annotation-property-replacement>\n"
            + "            </subsystem>\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:infinispan:1.5\">\n"
            + "                <cache-container name=\"web\" aliases=\"standard-session-cache\" default-cache=\"dist\" module=\"org.jboss.as.clustering.web.infinispan\">\n"
            + "                    <replicated-cache name=\"repl\" mode=\"SYNC\" batching=\"false\">\n"
            + "                        <file-store/>\n"
            + "                    </replicated-cache>\n"
            + "                    <replicated-cache name=\"sso\" mode=\"SYNC\" batching=\"false\"/>\n"
            + "                    <distributed-cache name=\"dist\" mode=\"SYNC\" batching=\"false\" l1-lifespan=\"0\">\n"
            + "                        <file-store/>\n"
            + "                    </distributed-cache>\n"
            + "                </cache-container>\n"
            + "                <cache-container name=\"ejb\"/>\n"
            + "            </subsystem>\n"
            + "        </profile>\n"
            + "        <profile name=\"whatever\">\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:logging:1.5\"/>\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:datasources:1.2\"/>\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:deployment-scanner:1.1\"/>\n"
            + "        </profile>\n"
            + "    </profiles>\n"
            + "</domain>";

    private static final String INTERFACES_HOST_ORIGINAL = ""
            + "<host name=\"master\" xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <interfaces>\n"
            + "        <interface name=\"management\">\n"
            + "            <inet-address value=\"${jboss.bind.address.management:127.0.0.1}\"/>\n"
            + "        </interface>\n"
            + "        <interface name=\"public\">\n"
            + "            <inet-address value=\"${jboss.bind.address:127.0.0.1}\"/>\n"
            + "        </interface>\n"
            + "    </interfaces>\n"
            + "</host>";
    private static final String INTERFACES_HOST_EXPECTED = ""
            + "<host name=\"master\" xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <interfaces>\n"
            + "        <interface name=\"management\">\n"
            + "            <inet-address value=\"10.1.2.3\"/>\n"
            + "        </interface>\n"
            + "        <interface name=\"public\">\n"
            + "            <inet-address value=\"10.1.2.3\"/>\n"
            + "        </interface>\n"
            + "    </interfaces>\n"
            + "</host>";

    private static final String INTERFACES_DOMAIN_ORIGINAL = ""
            + "<domain xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <interfaces>\n"
            + "        <interface name=\"management\"/>\n"
            + "        <interface name=\"public\"/>\n"
            + "    </interfaces>\n"
            + "</domain>";
    private static final String INTERFACES_DOMAIN_EXPECTED = ""
            + "<domain xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <interfaces>\n"
            + "        <interface name=\"management\"/>\n"
            + "        <interface name=\"public\"/>\n"
            + "        <interface name=\"foobar\"/>\n"
            + "    </interfaces>\n"
            + "</domain>";

    private static final String SOCKET_BINDING_GROUP_ORIGINAL = ""
            + "<domain xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <socket-binding-groups>\n"
            + "        <socket-binding-group name=\"standard-sockets\" default-interface=\"public\">\n"
            + "            <socket-binding name=\"http\" port=\"8080\"/>\n"
            + "            <outbound-socket-binding name=\"mail-smtp\">\n"
            + "                <remote-destination host=\"localhost\" port=\"25\"/>\n"
            + "            </outbound-socket-binding>\n"
            + "        </socket-binding-group>\n"
            + "        <socket-binding-group name=\"ha-sockets\" default-interface=\"public\"/>\n"
            + "    </socket-binding-groups>\n"
            + "</domain>";
    private static final String SOCKET_BINDING_GROUP_EXPECTED = ""
            + "<domain xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <socket-binding-groups>\n"
            + "        <socket-binding-group name=\"standard-sockets\" default-interface=\"public\">\n"
            + "            <socket-binding name=\"http\" port=\"10080\"/>\n"
            + "            <socket-binding name=\"https\" port=\"10443\"/>\n"
            + "            <outbound-socket-binding name=\"mail-smtp\">\n"
            + "                <remote-destination host=\"localhost\" port=\"587\"/>\n"
            + "            </outbound-socket-binding>\n"
            + "        </socket-binding-group>\n"
            + "        <socket-binding-group name=\"ha-sockets\" default-interface=\"public\"/>\n"
            + "    </socket-binding-groups>\n"
            + "</domain>";

    // domain-only; domain.xml

    public static final String PROFILES_ORIGINAL = ""
            + "<domain xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profiles>\n"
            + "        <profile name=\"default\">\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:logging:1.5\"/>\n"
            + "        </profile>\n"
            + "    </profiles>\n"
            + "</domain>";
    public static final String PROFILES_EXPECTED = ""
            + "<domain xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profiles>\n"
            + "        <profile name=\"default\">\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:logging:1.5\"/>\n"
            + "        </profile>\n"
            + "        <profile name=\"foobar\">\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:logging:1.5\"/>\n"
            + "        </profile>\n"
            + "    </profiles>\n"
            + "</domain>";

    private static final String SUBSYTEM_IN_PROFILE_ORIGINAL = ""
            + "<domain xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profiles>\n"
            + "        <profile name=\"default\">\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:logging:1.5\"/>\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:datasources:1.2\"/>\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:deployment-scanner:1.1\"/>\n"
            + "        </profile>\n"
            + "        <profile name=\"foobar\">\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:datasources:1.2\">\n"
            + "                <datasources>\n"
            + "                    <datasource jndi-name=\"java:jboss/datasources/ExampleDS\" pool-name=\"ExampleDS\" enabled=\"true\" use-java-context=\"true\">\n"
            + "                        <connection-url>jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE</connection-url>\n"
            + "                        <driver>h2</driver>\n"
            + "                        <security>\n"
            + "                            <user-name>sa</user-name>\n"
            + "                            <password>sa</password>\n"
            + "                        </security>\n"
            + "                    </datasource>\n"
            + "                </datasources>\n"
            + "            </subsystem>\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:ee:1.2\">\n"
            + "                <annotation-property-replacement>false</annotation-property-replacement>\n"
            + "            </subsystem>\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:infinispan:1.5\">\n"
            + "                <cache-container name=\"web\" aliases=\"standard-session-cache\" default-cache=\"repl\" module=\"org.jboss.as.clustering.web.infinispan\">\n"
            + "                    <replicated-cache name=\"repl\" mode=\"ASYNC\" batching=\"true\">\n"
            + "                        <file-store/>\n"
            + "                    </replicated-cache>\n"
            + "                    <replicated-cache name=\"sso\" mode=\"SYNC\" batching=\"true\"/>\n"
            + "                    <distributed-cache name=\"dist\" mode=\"ASYNC\" batching=\"true\" l1-lifespan=\"0\">\n"
            + "                        <file-store/>\n"
            + "                    </distributed-cache>\n"
            + "                </cache-container>\n"
            + "                <cache-container name=\"ejb\"/>\n"
            + "            </subsystem>\n"
            + "        </profile>\n"
            + "    </profiles>\n"
            + "</domain>";
    private static final String SUBSYTEM_IN_PROFILE_EXPECTED = ""
            + "<domain xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profiles>\n"
            + "        <profile name=\"default\">\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:logging:1.5\"/>\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:datasources:1.2\"/>\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:deployment-scanner:1.1\"/>\n"
            + "        </profile>\n"
            + "        <profile name=\"foobar\">\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:datasources:1.2\">\n"
            + "                <datasources>\n"
            + "                    <datasource jndi-name=\"java:jboss/datasources/ExampleDS\" pool-name=\"ExampleDS\" enabled=\"true\" use-java-context=\"true\">\n"
            + "                        <connection-url>jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE</connection-url>\n"
            + "                        <driver>h2</driver>\n"
            + "                        <security>\n"
            + "                            <user-name>my-user-name</user-name>\n"
            + "                            <password>my-password</password>\n"
            + "                        </security>\n"
            + "                    </datasource>\n"
            + "                </datasources>\n"
            + "            </subsystem>\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:ee:1.2\">\n"
            + "                <annotation-property-replacement>true</annotation-property-replacement>\n"
            + "            </subsystem>\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:infinispan:1.5\">\n"
            + "                <cache-container name=\"web\" aliases=\"standard-session-cache\" default-cache=\"dist\" module=\"org.jboss.as.clustering.web.infinispan\">\n"
            + "                    <replicated-cache name=\"repl\" mode=\"SYNC\" batching=\"false\">\n"
            + "                        <file-store/>\n"
            + "                    </replicated-cache>\n"
            + "                    <replicated-cache name=\"sso\" mode=\"SYNC\" batching=\"false\"/>\n"
            + "                    <distributed-cache name=\"dist\" mode=\"SYNC\" batching=\"false\" l1-lifespan=\"0\">\n"
            + "                        <file-store/>\n"
            + "                    </distributed-cache>\n"
            + "                </cache-container>\n"
            + "                <cache-container name=\"ejb\"/>\n"
            + "            </subsystem>\n"
            + "        </profile>\n"
            + "    </profiles>\n"
            + "</domain>";


    public static final String SOCKET_BINDING_GROUPS_ORIGINAL = ""
            + "<domain xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <socket-binding-groups>\n"
            + "        <socket-binding-group name=\"standard-sockets\" default-interface=\"public\">\n"
            + "            <socket-binding name=\"http\" port=\"8080\"/>\n"
            + "            <outbound-socket-binding name=\"mail-smtp\">\n"
            + "                <remote-destination host=\"localhost\" port=\"25\"/>\n"
            + "            </outbound-socket-binding>\n"
            + "        </socket-binding-group>\n"
            + "    </socket-binding-groups>\n"
            + "</domain>";
    public static final String SOCKET_BINDING_GROUPS_EXPECTED = ""
            + "<domain xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <socket-binding-groups>\n"
            + "        <socket-binding-group name=\"standard-sockets\" default-interface=\"public\">\n"
            + "            <socket-binding name=\"http\" port=\"8080\"/>\n"
            + "            <outbound-socket-binding name=\"mail-smtp\">\n"
            + "                <remote-destination host=\"localhost\" port=\"25\"/>\n"
            + "            </outbound-socket-binding>\n"
            + "        </socket-binding-group>\n"
            + "        <socket-binding-group name=\"foobar-sockets\" default-interface=\"public\">\n"
            + "            <socket-binding name=\"http\" port=\"9090\"/>\n"
            + "            <outbound-socket-binding name=\"mail-smtp\">\n"
            + "                <remote-destination host=\"localhost\" port=\"587\"/>\n"
            + "            </outbound-socket-binding>\n"
            + "        </socket-binding-group>\n"
            + "    </socket-binding-groups>\n"
            + "</domain>";

    private static final String SERVER_GROUPS_ORIGINAL = ""
            + "<domain xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <server-groups>\n"
            + "        <server-group name=\"main-server-group\" profile=\"full\">\n"
            + "            <jvm name=\"default\">\n"
            + "                <heap size=\"1000m\" max-size=\"1000m\"/>\n"
            + "                <permgen max-size=\"256m\"/>\n"
            + "            </jvm>\n"
            + "            <socket-binding-group ref=\"full-sockets\"/>\n"
            + "        </server-group>\n"
            + "    </server-groups>\n"
            + "</domain>";
    private static final String SERVER_GROUPS_EXPECTED = ""
            + "<domain xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <server-groups>\n"
            + "        <server-group name=\"main-server-group\" profile=\"full\">\n"
            + "            <jvm name=\"default\">\n"
            + "                <heap size=\"1000m\" max-size=\"1000m\"/>\n"
            + "                <permgen max-size=\"256m\"/>\n"
            + "            </jvm>\n"
            + "            <socket-binding-group ref=\"full-sockets\"/>\n"
            + "        </server-group>\n"
            + "        <server-group name=\"foobar-server-group\" profile=\"full\">\n"
            + "            <jvm name=\"default\">\n"
            + "                <heap size=\"1000m\" max-size=\"1000m\"/>\n"
            + "                <permgen max-size=\"256m\"/>\n"
            + "            </jvm>\n"
            + "            <socket-binding-group ref=\"foobar-sockets\"/>\n"
            + "        </server-group>\n"
            + "    </server-groups>\n"
            + "</domain>";

    // domain-only; host.xml

    private static final String DOMAIN_CONTROLLER_ORIGINAL = ""
            + "<host name=\"master\" xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <domain-controller>\n"
            + "        <local/>\n"
            + "    </domain-controller>\n"
            + "</host>  ";
    private static final String DOMAIN_CONTROLLER_EXPECTED = ""
            + "<host name=\"master\" xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <domain-controller>\n"
            + "        <remote host=\"${jboss.domain.master.address}\" port=\"${jboss.domain.master.port:9999}\" security-realm=\"ManagementRealm\"/>\n"
            + "    </domain-controller>\n"
            + "</host>  ";

    private static final String JVMS_ORIGINAL = ""
            + "<host name=\"master\" xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <jvms>\n"
            + "        <jvm name=\"default\">\n"
            + "            <heap size=\"64m\" max-size=\"256m\"/>\n"
            + "            <permgen size=\"256m\" max-size=\"256m\"/>\n"
            + "            <jvm-options>\n"
            + "                <option value=\"-server\"/>\n"
            + "            </jvm-options>\n"
            + "        </jvm>\n"
            + "    </jvms>\n"
            + "</host>";
    private static final String JVMS_EXPECTED = ""
            + "<host name=\"master\" xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <jvms>\n"
            + "        <jvm name=\"default\">\n"
            + "            <heap size=\"128m\" max-size=\"256m\"/>\n"
            + "            <permgen size=\"256m\" max-size=\"256m\"/>\n"
            + "            <jvm-options>\n"
            + "                <option value=\"-server\"/>\n"
            + "            </jvm-options>\n"
            + "        </jvm>\n"
            + "        <jvm name=\"foobar\">\n"
            + "            <heap size=\"1024m\" max-size=\"2048m\"/>\n"
            + "            <permgen size=\"512m\" max-size=\"512m\"/>\n"
            + "            <jvm-options>\n"
            + "                <option value=\"-server\"/>\n"
            + "                <option value=\"-XX:+UseConcMarkSweepGC\"/>\n"
            + "            </jvm-options>\n"
            + "        </jvm>\n"
            + "    </jvms>\n"
            + "</host>";

    private static final String SERVERS_ORIGINAL = ""
            + "<host xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <servers>\n"
            + "        <server name=\"server-one\" group=\"main-server-group\"/>\n"
            + "        <server name=\"server-two\" group=\"other-server-group\">\n"
            + "            <socket-bindings port-offset=\"150\"/>\n"
            + "        </server>\n"
            + "    </servers>\n"
            + "</host>";
    private static final String SERVERS_EXPECTED = ""
            + "<host xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <servers>\n"
            + "        <server name=\"server-one\" group=\"main-server-group\"/>\n"
            + "        <server name=\"server-two\" group=\"other-server-group\">\n"
            + "            <socket-bindings port-offset=\"100\"/>\n"
            + "        </server>\n"
            + "        <server name=\"server-three\" group=\"other-server-group\">\n"
            + "            <socket-bindings port-offset=\"200\"/>\n"
            + "        </server>\n"
            + "    </servers>\n"
            + "</host>";

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][] {
                // universal
                {"extensions", EXTENSIONS_ORIGINAL, EXTENSIONS_EXPECTED, ExampleSubtreeExtensionsTransformation.class},
                {"systemProperties_host", SYSTEM_PROPERTIES_HOST_ORIGINAL, SYSTEM_PROPERTIES_HOST_EXPECTED,
                        ExampleSubtreeSystemPropertiesTransformation.class},
                {"systemProperties_domain", SYSTEM_PROPERTIES_DOMAIN_ORIGINAL, SYSTEM_PROPERTIES_DOMAIN_EXPECTED,
                        ExampleSubtreeSystemPropertiesTransformation.class},
                {"paths_host", PATHS_HOST_ORIGINAL, PATHS_HOST_EXPECTED, ExampleSubtreePathsTransformation.class},
                {"paths_domain", PATHS_DOMAIN_ORIGINAL, PATHS_DOMAIN_EXPECTED, ExampleSubtreePathsTransformation.class},
                {"management_host", MANAGEMENT_HOST_ORIGINAL, MANAGEMENT_HOST_EXPECTED,
                        ExampleSubtreeManagementTransformation_Host.class},
                {"management_domain", MANAGEMENT_DOMAIN_ORIGINAL, MANAGEMENT_DOMAIN_EXPECTED,
                        ExampleSubtreeManagementTransformation.class},
                {"profile", PROFILE_ORIGINAL, PROFILE_EXPECTED, ExampleSubtreeProfileTransformation.class},
                {"subsystem", SUBSYTEM_ORIGINAL, SUBSYTEM_EXPECTED, ExampleSubtreeSubsystemTransformation.class},
                {"interfaces_host", INTERFACES_HOST_ORIGINAL, INTERFACES_HOST_EXPECTED,
                        ExampleSubtreeInterfacesTransformation.class},
                {"interfaces_domain", INTERFACES_DOMAIN_ORIGINAL, INTERFACES_DOMAIN_EXPECTED,
                        ExampleSubtreeInterfacesTransformation_Domain.class},
                {"socketBindingGroup", SOCKET_BINDING_GROUP_ORIGINAL, SOCKET_BINDING_GROUP_EXPECTED,
                        ExampleSubtreeSocketBindingGroupTransformation.class},

                // domain.xml
                {"profiles", PROFILES_ORIGINAL, PROFILES_EXPECTED, ExampleSubtreeProfilesTransformation.class},
                {"subsystemInProfile", SUBSYTEM_IN_PROFILE_ORIGINAL, SUBSYTEM_IN_PROFILE_EXPECTED,
                        ExampleSubtreeSubsystemInProfileTransformation.class},
                {"socketBindingGroups", SOCKET_BINDING_GROUPS_ORIGINAL, SOCKET_BINDING_GROUPS_EXPECTED,
                        ExampleSubtreeSocketBindingGroupsTransformation.class},
                {"serverGroups", SERVER_GROUPS_ORIGINAL, SERVER_GROUPS_EXPECTED,
                        ExampleSubtreeServerGroupsTransformation.class},

                // host.xml
                {"domainController", DOMAIN_CONTROLLER_ORIGINAL, DOMAIN_CONTROLLER_EXPECTED,
                        ExampleSubtreeDomainControllerTransformation.class},
                {"jvms", JVMS_ORIGINAL, JVMS_EXPECTED, ExampleSubtreeJvmsTransformation.class},
                {"servers", SERVERS_ORIGINAL, SERVERS_EXPECTED, ExampleSubtreeServersTransformation.class},
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
                OfflineOptions.domain().forHost("master").forProfile("default").build().configurationFile(cfg).build());

        assertXmlIdentical(originalXml, Files.toString(cfg, Charsets.UTF_8));
        client.apply(transformationCommand.newInstance());
        assertXmlIdentical(expectedXml, Files.toString(cfg, Charsets.UTF_8));
    }
}
