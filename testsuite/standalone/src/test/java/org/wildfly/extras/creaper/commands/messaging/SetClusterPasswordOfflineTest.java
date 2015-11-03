package org.wildfly.extras.creaper.commands.messaging;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;

import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;

public class SetClusterPasswordOfflineTest {

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();
    private static final String SUBSYTEM_ORIGINAL_ACTIVEMQ = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "         <subsystem xmlns=\"urn:jboss:domain:messaging-activemq:1.0\">\n"
            + "            <server name=\"default\">\n"
            + "                <jms-queue name=\"ExpiryQueue\" entries=\"java:/jms/queue/ExpiryQueue\"/>\n"
            + "                <jms-queue name=\"DLQ\" entries=\"java:/jms/queue/DLQ\"/>\n"
            + "                <connection-factory name=\"InVmConnectionFactory\" connectors=\"in-vm\" entries=\"java:/ConnectionFactory\"/>\n"
            + "            </server>\n"
            + "        </subsystem>"
            + "    </profile>\n"
            + "</server>";
    private static final String SUBSYTEM_PSWD1_ACTIVEMQ = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "         <subsystem xmlns=\"urn:jboss:domain:messaging-activemq:1.0\">\n"
            + "            <server name=\"default\">\n"
            + "                <jms-queue name=\"ExpiryQueue\" entries=\"java:/jms/queue/ExpiryQueue\"/>\n"
            + "                <jms-queue name=\"DLQ\" entries=\"java:/jms/queue/DLQ\"/>\n"
            + "                <connection-factory name=\"InVmConnectionFactory\" connectors=\"in-vm\" entries=\"java:/ConnectionFactory\"/>\n"
            + "          <cluster password=\"heslo1\"/>"
            + "            </server>\n"
            + "        </subsystem>"
            + "    </profile>\n"
            + "</server>";
    private static final String SUBSYTEM_PSWD2_ACTIVEMQ = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "         <subsystem xmlns=\"urn:jboss:domain:messaging-activemq:1.0\">\n"
            + "            <server name=\"default\">\n"
            + "                <jms-queue name=\"ExpiryQueue\" entries=\"java:/jms/queue/ExpiryQueue\"/>\n"
            + "                <jms-queue name=\"DLQ\" entries=\"java:/jms/queue/DLQ\"/>\n"
            + "                <connection-factory name=\"InVmConnectionFactory\" connectors=\"in-vm\" entries=\"java:/ConnectionFactory\"/>\n"
            + "          <cluster password=\"heslo2\"/>"
            + "            </server>\n"
            + "        </subsystem>"
            + "    </profile>\n"
            + "</server>";
    private static final String SUBSYSTEM_ORIGINAL_HORNETQ = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:messaging:1.4\">"
            + "            <hornetq-server>\n"
            + "                  <journal-file-size>102400</journal-file-size>\n"
            + "                <acceptors>\n"
            + "                    <http-acceptor http-listener=\"default\" name=\"http-acceptor\"/>\n"
            + "                    <http-acceptor http-listener=\"default\" name=\"http-acceptor-throughput\">\n"
            + "                        <param key=\"batch-delay\" value=\"50\"/>\n"
            + "                        <param key=\"direct-deliver\" value=\"false\"/>\n"
            + "                    </http-acceptor>\n"
            + "                    <in-vm-acceptor name=\"in-vm\" server-id=\"0\"/>\n"
            + "                </acceptors>\n"
            + "            </hornetq-server>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";
    private static final String SUBSYTEM_PSWD1_HORNETQ
            = "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:messaging:1.4\">"
            + "            <hornetq-server>\n"
            + "                  <journal-file-size>102400</journal-file-size>\n"
            + "                <acceptors>\n"
            + "                    <http-acceptor http-listener=\"default\" name=\"http-acceptor\"/>\n"
            + "                    <http-acceptor http-listener=\"default\" name=\"http-acceptor-throughput\">\n"
            + "                        <param key=\"batch-delay\" value=\"50\"/>\n"
            + "                        <param key=\"direct-deliver\" value=\"false\"/>\n"
            + "                    </http-acceptor>\n"
            + "                    <in-vm-acceptor name=\"in-vm\" server-id=\"0\"/>\n"
            + "                </acceptors>\n"
            + "<cluster-password>heslo1</cluster-password>"
            + "            </hornetq-server>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";
    private static final String SUBSYTEM_PSWD2_HORNETQ
            = "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:messaging:1.4\">"
            + "            <hornetq-server>\n"
            + "                  <journal-file-size>102400</journal-file-size>\n"
            + "                <acceptors>\n"
            + "                    <http-acceptor http-listener=\"default\" name=\"http-acceptor\"/>\n"
            + "                    <http-acceptor http-listener=\"default\" name=\"http-acceptor-throughput\">\n"
            + "                        <param key=\"batch-delay\" value=\"50\"/>\n"
            + "                        <param key=\"direct-deliver\" value=\"false\"/>\n"
            + "                    </http-acceptor>\n"
            + "                    <in-vm-acceptor name=\"in-vm\" server-id=\"0\"/>\n"
            + "                </acceptors>\n"
            + "<cluster-password>heslo2</cluster-password>"
            + "            </hornetq-server>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    @Before
    public void setUp() {
        XMLUnit.setNormalizeWhitespace(true);
    }

    @Test
    public void pswd1AMQ() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYTEM_ORIGINAL_ACTIVEMQ, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYTEM_ORIGINAL_ACTIVEMQ, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new SetMessagingClusterPassword("heslo1"));
        System.out.println(Files.toString(cfg, Charsets.UTF_8));
        assertXmlIdentical(SUBSYTEM_PSWD1_ACTIVEMQ, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void pswd2AMQ() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYTEM_PSWD1_ACTIVEMQ, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYTEM_PSWD1_ACTIVEMQ, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new SetMessagingClusterPassword("heslo2"));
        System.out.println(Files.toString(cfg, Charsets.UTF_8));
        assertXmlIdentical(SUBSYTEM_PSWD2_ACTIVEMQ, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void pswd1HQ() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_ORIGINAL_HORNETQ, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYSTEM_ORIGINAL_HORNETQ, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new SetMessagingClusterPassword("heslo1"));
        System.out.println(Files.toString(cfg, Charsets.UTF_8));
        assertXmlIdentical(SUBSYTEM_PSWD1_HORNETQ, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void pswd2HQ() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYTEM_PSWD1_HORNETQ, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYTEM_PSWD1_HORNETQ, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new SetMessagingClusterPassword("heslo2"));
        System.out.println(Files.toString(cfg, Charsets.UTF_8));
        assertXmlIdentical(SUBSYTEM_PSWD2_HORNETQ, Files.toString(cfg, Charsets.UTF_8));
    }

}
