package org.wildfly.extras.creaper.commands.transactions;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.wildfly.extras.creaper.XmlAssert;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;

@RunWith(Parameterized.class)
public class ChangeBasicAttributesOfflineTest {

    private static final String TRANSACTION_DEFAULT_WILDFLY10 = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:transactions:3.0\">\n"
            + "            <core-environment>\n"
            + "                <process-id>\n"
            + "                    <uuid/>\n"
            + "                </process-id>\n"
            + "            </core-environment>\n"
            + "            <recovery-environment socket-binding=\"txn-recovery-environment\" status-socket-binding=\"txn-status-manager\"/>\n"
            + "            <jdbc-store datasource-jndi-name=\"test\" />\n"
            + "        </subsystem>"
            + "    </profile>\n"
            + "</server>";

    private static final String TRANSACTION_DEFAULT_EAP64 = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:transactions:1.5\">\n"
            + "            <core-environment>\n"
            + "                <process-id>\n"
            + "                    <uuid/>\n"
            + "                </process-id>\n"
            + "            </core-environment>\n"
            + "            <recovery-environment socket-binding=\"txn-recovery-environment\" status-socket-binding=\"txn-status-manager\"/>\n"
            + "            <jdbc-store datasource-jndi-name=\"test\" />\n"
            + "        </subsystem>"
            + "    </profile>\n"
            + "</server>";

    private static final String TRANSACTION_CHANGE_FIRST_WILDFLY10 = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:transactions:3.0\">\n"
            + "            <core-environment node-identifier=\"3\">\n"
            + "                <process-id>\n"
            + "                    <socket socket-binding=\"a\" socket-process-id-max-ports=\"100\"/>\n"
            + "                </process-id>\n"
            + "            </core-environment>\n"
            + "            <recovery-environment socket-binding=\"socket-binding\" status-socket-binding=\"status-binding\" recovery-listener=\"true\"/>\n"
            + "            <coordinator-environment statistics-enabled=\"true\" enable-tsm-status=\"true\" default-timeout=\"999\"/>\n"
            + "             <object-store path=\"path\" relative-to=\"relative-to\"/>\n"
            + "            <jts/>\n"
            + "            <use-journal-store enable-async-io=\"true\"/>\n"
            + "        </subsystem>"
            + "    </profile>\n"
            + "</server>";

    private static final String TRANSACTION_CHANGE_FIRST_EAP64 = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:transactions:1.5\">\n"
            + "            <core-environment node-identifier=\"3\">\n"
            + "                <process-id>\n"
            + "                    <socket socket-binding=\"a\" socket-process-id-max-ports=\"100\"/>\n"
            + "                </process-id>\n"
            + "            </core-environment>\n"
            + "            <recovery-environment socket-binding=\"socket-binding\" status-socket-binding=\"status-binding\" recovery-listener=\"true\"/>\n"
            + "            <coordinator-environment enable-statistics=\"true\" enable-tsm-status=\"true\" default-timeout=\"999\"/>\n"
            + "             <object-store path=\"path\" relative-to=\"relative-to\"/>\n"
            + "            <jts/>\n"
            + "            <use-hornetq-store enable-async-io=\"true\"/>\n"
            + "        </subsystem>"
            + "    </profile>\n"
            + "</server>";

    private static final String TRANSACTION_CHANGE_SECOND_WILDFLY10 = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:transactions:3.0\">\n"
            + "            <core-environment node-identifier=\"1\">\n"
            + "                <process-id>\n"
            + "                    <uuid/>\n"
            + "                </process-id>\n"
            + "            </core-environment>\n"
            + "            <recovery-environment socket-binding=\"socket-binding-change\" status-socket-binding=\"status-binding-change\" recovery-listener=\"false\"/>\n"
            + "            <coordinator-environment statistics-enabled=\"false\" enable-tsm-status=\"false\" default-timeout=\"0\"/>\n"
            + "             <object-store path=\"path-change\" relative-to=\"relative-to-change\"/>\n"
            + "        </subsystem>"
            + "    </profile>\n"
            + "</server>";

    private static final String TRANSACTION_CHANGE_SECOND_EAP64 = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:transactions:1.5\">\n"
            + "            <core-environment node-identifier=\"1\">\n"
            + "                <process-id>\n"
            + "                    <uuid/>\n"
            + "                </process-id>\n"
            + "            </core-environment>\n"
            + "            <recovery-environment socket-binding=\"socket-binding-change\" status-socket-binding=\"status-binding-change\" recovery-listener=\"false\"/>\n"
            + "            <coordinator-environment enable-statistics=\"false\" enable-tsm-status=\"false\" default-timeout=\"0\"/>\n"
            + "            <object-store path=\"path-change\" relative-to=\"relative-to-change\"/>\n"
            + "        </subsystem>"
            + "    </profile>\n"
            + "</server>";


    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Parameterized.Parameter(0)
    public String defaultXml;

    @Parameterized.Parameter(1)
    public String firstXml;

    @Parameterized.Parameter(2)
    public String secondXml;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][] {
            {TRANSACTION_DEFAULT_WILDFLY10, TRANSACTION_CHANGE_FIRST_WILDFLY10, TRANSACTION_CHANGE_SECOND_WILDFLY10},
            {TRANSACTION_DEFAULT_EAP64, TRANSACTION_CHANGE_FIRST_EAP64, TRANSACTION_CHANGE_SECOND_EAP64}
        });
    }

    @Before
    public void setUp() throws IOException {
        XmlAssert.setNormalizeWhitespace(true);
    }

    @Test
    public void changeDefaultOffline() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");

        Files.write(defaultXml, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        ChangeBasicTransactionAttributes cmd = TransactionManager.basicAttributes()
                .enableTsmStatus(true)
                .journalStoreEnableAsyncIO(true)
                .jts(true)
                .nodeIdentifier("3")
                .timeout(999)
                .statisticsEnabled(true)
                .useJournalStore(true)
                .processIdSocketBinding("a")
                .processIdSocketMaxPorts(100)
                .socketBinding("socket-binding")
                .statusSocketBinding("status-binding")
                .recoveryListener(true)
                .objectStorePath("path")
                .objectStoreRelativeTo("relative-to")
                .build();


        assertXmlIdentical(defaultXml, Files.toString(cfg, Charsets.UTF_8));

        client.apply(cmd);

        assertXmlIdentical(firstXml, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void changeAllOffline() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");

        Files.write(firstXml, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        ChangeBasicTransactionAttributes cmd = TransactionManager.basicAttributes()
                .enableTsmStatus(false)
                .jts(false)
                .nodeIdentifier("1")
                .timeout(0)
                .statisticsEnabled(false)
                .useJournalStore(false)
                .processIdUuid(true)
                .socketBinding("socket-binding-change")
                .statusSocketBinding("status-binding-change")
                .recoveryListener(false)
                .objectStorePath("path-change")
                .objectStoreRelativeTo("relative-to-change")
                .build();


        assertXmlIdentical(firstXml, Files.toString(cfg, Charsets.UTF_8));

        client.apply(cmd);

        assertXmlIdentical(secondXml, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void changeNothingOffline() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");

        Files.write(firstXml, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        ChangeBasicTransactionAttributes cmd = TransactionManager.basicAttributes()
                .enableTsmStatus(true)
                .journalStoreEnableAsyncIO(true)
                .jts(true)
                .nodeIdentifier("3")
                .timeout(999)
                .statisticsEnabled(true)
                .useJournalStore(true)
                .build();

        client.apply(cmd);
        assertXmlIdentical(firstXml, Files.toString(cfg, Charsets.UTF_8));

        cmd = TransactionManager.basicAttributes()
                .build();

        client.apply(cmd);
        assertXmlIdentical(firstXml, Files.toString(cfg, Charsets.UTF_8));
    }
}
