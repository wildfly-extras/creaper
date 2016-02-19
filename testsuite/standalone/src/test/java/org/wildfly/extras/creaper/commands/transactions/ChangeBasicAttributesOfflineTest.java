package org.wildfly.extras.creaper.commands.transactions;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;

import java.io.File;
import java.io.IOException;

import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;

public class ChangeBasicAttributesOfflineTest {

    private static final String TRANSACTION_DEFAULT = ""
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


    private static final String TRANSACTION_CHANGE_FIRST = ""
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

    private static final String TRANSACTION_CHANGE_SECOND = ""
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


    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        XMLUnit.setNormalizeWhitespace(true);
    }

    @Test
    public void changeDefaultOffline() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");

        Files.write(TRANSACTION_DEFAULT, cfg, Charsets.UTF_8);

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


        assertXmlIdentical(TRANSACTION_DEFAULT, Files.toString(cfg, Charsets.UTF_8));

        client.apply(cmd);

        assertXmlIdentical(TRANSACTION_CHANGE_FIRST, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void changeAllOffline() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");

        Files.write(TRANSACTION_CHANGE_FIRST, cfg, Charsets.UTF_8);

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


        assertXmlIdentical(TRANSACTION_CHANGE_FIRST, Files.toString(cfg, Charsets.UTF_8));

        client.apply(cmd);

        assertXmlIdentical(TRANSACTION_CHANGE_SECOND, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void changeNothingOffline() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");

        Files.write(TRANSACTION_CHANGE_FIRST, cfg, Charsets.UTF_8);

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
        assertXmlIdentical(TRANSACTION_CHANGE_FIRST, Files.toString(cfg, Charsets.UTF_8));

        cmd = TransactionManager.basicAttributes()
                .build();

        client.apply(cmd);
        assertXmlIdentical(TRANSACTION_CHANGE_FIRST, Files.toString(cfg, Charsets.UTF_8));
    }
}
