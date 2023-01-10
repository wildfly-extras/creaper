package org.wildfly.extras.creaper.commands.transactions;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wildfly.extras.creaper.XmlAssert;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;

import java.io.File;
import java.io.IOException;

import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;

public class ChangeJdbcAttributesOfflineTest {

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
            + "        </subsystem>"
            + "    </profile>\n"
            + "</server>";

    private static final String TRANSACTION_DEFAULT_JOURNAL_STORE = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:transactions:3.0\">\n"
            + "            <core-environment>\n"
            + "                <process-id>\n"
            + "                    <uuid/>\n"
            + "                </process-id>\n"
            + "            </core-environment>\n"
            + "            <recovery-environment socket-binding=\"txn-recovery-environment\" status-socket-binding=\"txn-status-manager\"/>\n"
            + "            <use-journal-store/>"
            + "        </subsystem>"
            + "    </profile>\n"
            + "</server>";

    private static final String TRANSACTION_JDBC = ""
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

    private static final String TRANSACTION_JDBC_CHANGE = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:transactions:3.0\">\n"
            + "            <core-environment>\n"
            + "                <process-id>\n"
            + "                    <uuid/>\n"
            + "                </process-id>\n"
            + "            </core-environment>\n"
            + "            <recovery-environment socket-binding=\"txn-recovery-environment\" status-socket-binding=\"txn-status-manager\"/>\n"
            + "            <jdbc-store datasource-jndi-name=\"test-changed\" />\n"
            + "        </subsystem>"
            + "    </profile>\n"
            + "</server>";

    private static final String TRANSACTION_DEFAULT_ADD_ACTION = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:transactions:3.0\">\n"
            + "            <core-environment>\n"
            + "                <process-id>\n"
            + "                    <uuid/>\n"
            + "                </process-id>\n"
            + "            </core-environment>\n"
            + "            <recovery-environment socket-binding=\"txn-recovery-environment\" status-socket-binding=\"txn-status-manager\"/>\n"
            + "            <jdbc-store datasource-jndi-name=\"test\">\n"
            + "                <action table-prefix=\"test-action\" drop-table=\"true\"/>\n"
            + "            </jdbc-store>\n"
            + "        </subsystem>"
            + "    </profile>\n"
            + "</server>";

    private static final String TRANSACTION_DEFAULT_CHANGE_ACTION = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:transactions:3.0\">\n"
            + "            <core-environment>\n"
            + "                <process-id>\n"
            + "                    <uuid/>\n"
            + "                </process-id>\n"
            + "            </core-environment>\n"
            + "            <recovery-environment socket-binding=\"txn-recovery-environment\" status-socket-binding=\"txn-status-manager\"/>\n"
            + "            <jdbc-store datasource-jndi-name=\"test\">\n"
            + "                <action table-prefix=\"test-action-changed\" drop-table=\"false\"/>\n"
            + "            </jdbc-store>\n"
            + "        </subsystem>"
            + "    </profile>\n"
            + "</server>";

    private static final String TRANSACTION_DEFAULT_ADD_COMMUNICATION = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:transactions:3.0\">\n"
            + "            <core-environment>\n"
            + "                <process-id>\n"
            + "                    <uuid/>\n"
            + "                </process-id>\n"
            + "            </core-environment>\n"
            + "            <recovery-environment socket-binding=\"txn-recovery-environment\" status-socket-binding=\"txn-status-manager\"/>\n"
            + "            <jdbc-store datasource-jndi-name=\"test\">\n"
            + "                <communication table-prefix=\"test-communication\" drop-table=\"true\"/>\n"
            + "            </jdbc-store>\n"
            + "        </subsystem>"
            + "    </profile>\n"
            + "</server>";

    private static final String TRANSACTION_DEFAULT_CHANGE_COMMUNICATION = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:transactions:3.0\">\n"
            + "            <core-environment>\n"
            + "                <process-id>\n"
            + "                    <uuid/>\n"
            + "                </process-id>\n"
            + "            </core-environment>\n"
            + "            <recovery-environment socket-binding=\"txn-recovery-environment\" status-socket-binding=\"txn-status-manager\"/>\n"
            + "            <jdbc-store datasource-jndi-name=\"test\">\n"
            + "                <communication table-prefix=\"test-communication-changed\" drop-table=\"false\"/>\n"
            + "            </jdbc-store>\n"
            + "        </subsystem>"
            + "    </profile>\n"
            + "</server>";

    private static final String TRANSACTION_DEFAULT_ADD_STATE = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:transactions:3.0\">\n"
            + "            <core-environment>\n"
            + "                <process-id>\n"
            + "                    <uuid/>\n"
            + "                </process-id>\n"
            + "            </core-environment>\n"
            + "            <recovery-environment socket-binding=\"txn-recovery-environment\" status-socket-binding=\"txn-status-manager\"/>\n"
            + "            <jdbc-store datasource-jndi-name=\"test\">\n"
            + "                <state table-prefix=\"test-state\" drop-table=\"true\"/>\n"
            + "            </jdbc-store>\n"
            + "        </subsystem>"
            + "    </profile>\n"
            + "</server>";

    private static final String TRANSACTION_DEFAULT_CHANGE_STATE = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:transactions:3.0\">\n"
            + "            <core-environment>\n"
            + "                <process-id>\n"
            + "                    <uuid/>\n"
            + "                </process-id>\n"
            + "            </core-environment>\n"
            + "            <recovery-environment socket-binding=\"txn-recovery-environment\" status-socket-binding=\"txn-status-manager\"/>\n"
            + "            <jdbc-store datasource-jndi-name=\"test\">\n"
            + "                <state table-prefix=\"test-state-changed\" drop-table=\"false\"/>\n"
            + "            </jdbc-store>\n"
            + "        </subsystem>"
            + "    </profile>\n"
            + "</server>";

    private static final String TRANSACTION_JDBC_CHANGE_ALL = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:transactions:3.0\">\n"
            + "            <core-environment>\n"
            + "                <process-id>\n"
            + "                    <uuid/>\n"
            + "                </process-id>\n"
            + "            </core-environment>\n"
            + "            <recovery-environment socket-binding=\"txn-recovery-environment\" status-socket-binding=\"txn-status-manager\"/>\n"
            + "            <jdbc-store datasource-jndi-name=\"test\">\n"
            + "                <action table-prefix=\"test-action\" drop-table=\"true\"/>\n"
            + "                <communication table-prefix=\"test-communication\" drop-table=\"true\"/>\n"
            + "                <state table-prefix=\"test-state\" drop-table=\"true\"/>\n"
            + "            </jdbc-store>\n"
            + "        </subsystem>"
            + "    </profile>\n"
            + "</server>";

    private static final String TRANSACTION_JDBC_FALSE_ALL = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:transactions:3.0\">\n"
            + "            <core-environment>\n"
            + "                <process-id>\n"
            + "                    <uuid/>\n"
            + "                </process-id>\n"
            + "            </core-environment>\n"
            + "            <recovery-environment socket-binding=\"txn-recovery-environment\" status-socket-binding=\"txn-status-manager\"/>\n"
            + "            <jdbc-store datasource-jndi-name=\"test\">\n"
            + "                <action drop-table=\"false\"/>\n"
            + "                <communication drop-table=\"false\"/>\n"
            + "                <state drop-table=\"false\"/>\n"
            + "            </jdbc-store>\n"
            + "        </subsystem>"
            + "    </profile>\n"
            + "</server>";


    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        XmlAssert.setNormalizeWhitespace(true);
    }

    @Test
    public void addAndChangeActionIsolated() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(TRANSACTION_DEFAULT_JOURNAL_STORE, cfg, Charsets.UTF_8);
        assertXmlIdentical(TRANSACTION_DEFAULT_JOURNAL_STORE, Files.toString(cfg, Charsets.UTF_8));

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());


        ChangeJdbcTransactionAttributes addCmd = TransactionManager.jdbc()
                .useJdbcStore(true)
                .storeDatasource("test")
                .actionStoreDropTable(true)
                .actionStoreTablePrefix("test-action")
                .build();

        ChangeJdbcTransactionAttributes changeCmd = TransactionManager.jdbc()
                .actionStoreDropTable(false)
                .actionStoreTablePrefix("test-action-changed")
                .build();

        ChangeJdbcTransactionAttributes changeNothingCmd = TransactionManager.jdbc()
                .build();

        // jdbc node doesn't exits
        client.apply(addCmd);
        assertXmlIdentical(TRANSACTION_DEFAULT_ADD_ACTION, Files.toString(cfg, Charsets.UTF_8));
        client.apply(changeCmd);
        assertXmlIdentical(TRANSACTION_DEFAULT_CHANGE_ACTION, Files.toString(cfg, Charsets.UTF_8));
        client.apply(changeNothingCmd);
        assertXmlIdentical(TRANSACTION_DEFAULT_CHANGE_ACTION, Files.toString(cfg, Charsets.UTF_8));


        // jdbc node exists (changing have been covered above)
        Files.write(TRANSACTION_JDBC, cfg, Charsets.UTF_8);
        assertXmlIdentical(TRANSACTION_JDBC, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addCmd);
        assertXmlIdentical(TRANSACTION_DEFAULT_ADD_ACTION, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addAndChangeCommunicationIsolated() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(TRANSACTION_DEFAULT, cfg, Charsets.UTF_8);
        assertXmlIdentical(TRANSACTION_DEFAULT, Files.toString(cfg, Charsets.UTF_8));

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        ChangeJdbcTransactionAttributes addCmd = TransactionManager.jdbc()
                .useJdbcStore(true)
                .storeDatasource("test")
                .communicationStoreDropTable(true)
                .communicationStoreTablePrefix("test-communication")
                .build();

        ChangeJdbcTransactionAttributes changeCmd = TransactionManager.jdbc()
                .communicationStoreDropTable(false)
                .communicationStoreTablePrefix("test-communication-changed")
                .build();

        ChangeJdbcTransactionAttributes changeNothingCmd = TransactionManager.jdbc()
                .build();

        // jdbc node doesn't exists
        client.apply(addCmd);
        assertXmlIdentical(TRANSACTION_DEFAULT_ADD_COMMUNICATION, Files.toString(cfg, Charsets.UTF_8));
        client.apply(changeCmd);
        assertXmlIdentical(TRANSACTION_DEFAULT_CHANGE_COMMUNICATION, Files.toString(cfg, Charsets.UTF_8));
        client.apply(changeNothingCmd);
        assertXmlIdentical(TRANSACTION_DEFAULT_CHANGE_COMMUNICATION, Files.toString(cfg, Charsets.UTF_8));

        // jdbc node exists (changing have been covered above)
        Files.write(TRANSACTION_JDBC, cfg, Charsets.UTF_8);
        assertXmlIdentical(TRANSACTION_JDBC, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addCmd);
        assertXmlIdentical(TRANSACTION_DEFAULT_ADD_COMMUNICATION, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addAndChangeStateIsolated() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(TRANSACTION_DEFAULT, cfg, Charsets.UTF_8);
        assertXmlIdentical(TRANSACTION_DEFAULT, Files.toString(cfg, Charsets.UTF_8));

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        ChangeJdbcTransactionAttributes addCmd = TransactionManager.jdbc()
                .useJdbcStore(true)
                .storeDatasource("test")
                .stateStoreDropTable(true)
                .stateStoreTablePrefix("test-state")
                .build();

        ChangeJdbcTransactionAttributes changeCmd = TransactionManager.jdbc()
                .stateStoreDropTable(false)
                .stateStoreTablePrefix("test-state-changed")
                .build();

        ChangeJdbcTransactionAttributes changeNothingCmd = TransactionManager.jdbc()
                .build();

        // jdbc node doesn't exists
        client.apply(addCmd);
        assertXmlIdentical(TRANSACTION_DEFAULT_ADD_STATE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(changeCmd);
        assertXmlIdentical(TRANSACTION_DEFAULT_CHANGE_STATE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(changeNothingCmd);
        assertXmlIdentical(TRANSACTION_DEFAULT_CHANGE_STATE, Files.toString(cfg, Charsets.UTF_8));

        // jdbc node exists (changing have been covered above)
        Files.write(TRANSACTION_JDBC, cfg, Charsets.UTF_8);
        assertXmlIdentical(TRANSACTION_JDBC, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addCmd);
        assertXmlIdentical(TRANSACTION_DEFAULT_ADD_STATE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addAndChangeJdbcIsolated() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(TRANSACTION_DEFAULT, cfg, Charsets.UTF_8);
        assertXmlIdentical(TRANSACTION_DEFAULT, Files.toString(cfg, Charsets.UTF_8));

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        ChangeJdbcTransactionAttributes addCmd = TransactionManager.jdbc()
                .useJdbcStore(true)
                .storeDatasource("test")
                .build();

        ChangeJdbcTransactionAttributes changeCmd = TransactionManager.jdbc()
                .storeDatasource("test-changed")
                .build();

        ChangeJdbcTransactionAttributes changeNothingCmd = TransactionManager.jdbc()
                .build();


        client.apply(addCmd);
        assertXmlIdentical(TRANSACTION_JDBC, Files.toString(cfg, Charsets.UTF_8));
        client.apply(changeCmd);
        assertXmlIdentical(TRANSACTION_JDBC_CHANGE, Files.toString(cfg, Charsets.UTF_8));
        client.apply(changeNothingCmd);
        assertXmlIdentical(TRANSACTION_JDBC_CHANGE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addAndChangeALL() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(TRANSACTION_DEFAULT, cfg, Charsets.UTF_8);
        assertXmlIdentical(TRANSACTION_DEFAULT, Files.toString(cfg, Charsets.UTF_8));

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        ChangeJdbcTransactionAttributes addCmd = TransactionManager.jdbc()
                .useJdbcStore(true)
                .storeDatasource("test")
                .actionStoreDropTable(true)
                .actionStoreTablePrefix("test-action")
                .communicationStoreDropTable(true)
                .communicationStoreTablePrefix("test-communication")
                .stateStoreDropTable(true)
                .stateStoreTablePrefix("test-state")
                .build();

        ChangeJdbcTransactionAttributes falseCmd = TransactionManager.jdbc()
                .actionStoreDropTable(false)
                .actionStoreTablePrefix("")
                .communicationStoreDropTable(false)
                .communicationStoreTablePrefix("")
                .stateStoreDropTable(false)
                .stateStoreTablePrefix("")
                .build();

        client.apply(addCmd);
        assertXmlIdentical(TRANSACTION_JDBC_CHANGE_ALL, Files.toString(cfg, Charsets.UTF_8));
        client.apply(falseCmd);
        assertXmlIdentical(TRANSACTION_JDBC_FALSE_ALL, Files.toString(cfg, Charsets.UTF_8));
    }
}
