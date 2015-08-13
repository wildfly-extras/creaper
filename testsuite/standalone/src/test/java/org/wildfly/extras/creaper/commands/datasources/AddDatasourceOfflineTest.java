package org.wildfly.extras.creaper.commands.datasources;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.custommonkey.xmlunit.XMLUnit;
import org.jboss.logging.Logger;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;
import static org.junit.Assert.fail;

public class AddDatasourceOfflineTest {
    private static final Logger log = Logger.getLogger(AddDatasourceOfflineTest.class);

    private static final String SUBSYTEM_ORIGINAL = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:datasources:1.2\">\n"
            + "            <datasources>\n"
            + "                 %s\n"
            + "            </datasources>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";
    private static final String SUBSYSTEM_EXPECTED_REPLACE = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:datasources:1.2\">\n"
            + "            <datasources>\n"
            + "                <datasource pool-name=\"creaper-ds\" jndi-name=\"java:/jboss/datasources/creaper-ds\" enabled=\"false\">\n"
            + "                    <connection-url>jdbc:h2:mem:test-creaper;DB_CLOSE_DELAY=-1;</connection-url>\n"
            + "                    <driver>h2</driver>\n"
            + "                    <security>\n"
            + "                      <user-name>creaper</user-name>\n"
            + "                      <password>creaper</password>\n"
            + "                    </security>\n"
            + "                </datasource>\n"
            + "            </datasources>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";
    private static final String SUBSYTEM_EXPECTED = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:datasources:1.2\">\n"
            + "            <datasources>\n"
            + "                 <datasource pool-name=\"eap-qe\"/>\n"
            + "                 <datasource jta=\"true\" jndi-name=\"java:/jboss/datasources/creaper-ds\" pool-name=\"creaper-ds\" enabled=\"false\"\n"
            + "                     use-java-context=\"true\" spy=\"true\" use-ccm=\"true\" connectable=\"true\" statistics-enabled=\"true\">\n"
            + "                     <connection-url>jdbc:h2:mem:test-creaper;DB_CLOSE_DELAY=-1;</connection-url>\n"
            + "                     <driver-class>org.h2.Driver</driver-class>\n"
            + "                     <datasource-class>org.h2.jdbcx.JdbcDataSource</datasource-class>\n"
            + "                     <connection-property name=\"other-user-name\">right-its-other-username</connection-property>\n"
            + "                     <driver>h2</driver>\n"
            + "                     <new-connection-sql>SELECT 1</new-connection-sql>\n"
            + "                     <transaction-isolation>TRANSACTION_REPEATABLE_READ</transaction-isolation>\n"
            + "                     <url-delimiter>;</url-delimiter>\n"
            + "                     <pool>\n"
            + "                         <min-pool-size>1</min-pool-size>\n"
            + "                         <max-pool-size>1</max-pool-size>\n"
            + "                         <prefill>true</prefill>\n"
            + "                         <use-strict-min>true</use-strict-min>\n"
            + "                         <flush-strategy>EntirePool</flush-strategy>\n"
            + "                         <allow-multiple-users>true</allow-multiple-users>\n"
            + "                     </pool>\n"
            + "                     <security>\n"
            + "                         <user-name>creaper</user-name>\n"
            + "                         <password>creaper</password>\n"
            + "                     </security>\n"
            + "                     <validation>\n"
            + "                         <valid-connection-checker class-name=\"org.jboss.jca.adapters.jdbc.extensions.novendor.NullValidConnectionChecker\">\n"
            + "                             <config-property name=\"valid-checker-prop\">ok</config-property>\n"
            + "                         </valid-connection-checker>\n"
            + "                         <check-valid-connection-sql>SELECT 1</check-valid-connection-sql>\n"
            + "                         <validate-on-match>true</validate-on-match>\n"
            + "                         <background-validation>true</background-validation>\n"
            + "                         <background-validation-millis>1000</background-validation-millis>\n"
            + "                         <use-fast-fail>false</use-fast-fail>\n"
            + "                         <stale-connection-checker class-name=\"org.jboss.jca.adapters.jdbc.extensions.novendor.NullStaleConnectionChecker\">\n"
            + "                             <config-property name=\"stale-checker-prop\">ok</config-property>\n"
            + "                         </stale-connection-checker>\n"
            + "                         <exception-sorter class-name=\"org.jboss.jca.adapters.jdbc.extensions.novendor.NullExceptionSorter\">\n"
            + "                             <config-property name=\"exception-sorter-prop\">ok</config-property>\n"
            + "                         </exception-sorter>\n"
            + "                     </validation>\n"
            + "                     <timeout>\n"
            + "                         <set-tx-query-timeout>true</set-tx-query-timeout>\n"
            + "                         <blocking-timeout-millis>1000</blocking-timeout-millis>\n"
            + "                         <idle-timeout-minutes>10</idle-timeout-minutes>\n"
            + "                         <query-timeout>20</query-timeout>\n"
            + "                         <use-try-lock>60</use-try-lock>\n"
            + "                         <allocation-retry>3</allocation-retry>\n"
            + "                         <allocation-retry-wait-millis>1000</allocation-retry-wait-millis>\n"
            + "                     </timeout>\n"
            + "                     <statement>\n"
            + "                         <track-statements>true</track-statements>\n"
            + "                         <prepared-statement-cache-size>3</prepared-statement-cache-size>\n"
            + "                         <share-prepared-statements>true</share-prepared-statements>\n"
            + "                     </statement>\n"
            + "                </datasource>\n"
            + "            </datasources>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Before
    public void setUp() {
        XMLUnit.setNormalizeWhitespace(true);
    }

    @Test(expected = CommandFailedException.class)
    public void existing() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        String subsystemXml = String.format(SUBSYTEM_ORIGINAL, "<datasource pool-name=\"creaper-ds\"/>");
        Files.write(subsystemXml, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddDataSource addDataSource = new AddDataSource.Builder("creaper-ds")
            .connectionUrl("jdbc:h2:mem:test-creaper;DB_CLOSE_DELAY=-1;")
            .jndiName("java:/jboss/datasources/creaper-ds")
            .driverName("h2")
            .usernameAndPassword("creaper", "creaper")
            .build();

        assertXmlIdentical(subsystemXml, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addDataSource);
        fail("Datasource creaper-ds already exists in configuration, exception should be thrown");
    }

    @Test
    public void overrideExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        String subsystemXml = String.format(SUBSYTEM_ORIGINAL, "<datasource pool-name=\"creaper-ds\"/>");
        Files.write(subsystemXml, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddDataSource addDataSource = new AddDataSource.Builder("creaper-ds")
            .connectionUrl("jdbc:h2:mem:test-creaper;DB_CLOSE_DELAY=-1;")
            .jndiName("java:/jboss/datasources/creaper-ds")
            .driverName("h2")
            .usernameAndPassword("creaper", "creaper")
            .replaceExisting()
            .build();

        assertXmlIdentical(subsystemXml, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addDataSource);
        assertXmlIdentical(SUBSYSTEM_EXPECTED_REPLACE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void transform() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        String subsystemXml = String.format(SUBSYTEM_ORIGINAL, "<datasource pool-name=\"eap-qe\"/>");
        Files.write(subsystemXml, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddDataSource addDataSource = new AddDataSource.Builder("creaper-ds")
            .connectionUrl("jdbc:h2:mem:test-creaper;DB_CLOSE_DELAY=-1;")
            .jndiName("java:/jboss/datasources/creaper-ds")
            .driverName("h2")
            .usernameAndPassword("creaper", "creaper")

            .allocationRetry(3)
            .allocationRetryWaitMillis(1000)
            .allowMultipleUsers(true)
            .backgroundValidation(true)
            .backgroundValidationMillis(1000)
            .blockingTimeoutWaitMillis(1000)
            .checkValidConnectionSql("SELECT 1")
            .connectable(true)
            .addConnectionProperty("other-user-name", "right-its-other-username")
            .datasourceClass("org.h2.jdbcx.JdbcDataSource")
            .driverClass("org.h2.Driver")
            .exceptionSorterClass("org.jboss.jca.adapters.jdbc.extensions.novendor.NullExceptionSorter")
            .addExceptionSorterProperty("exception-sorter-prop", "ok")
            .idleTimeoutMinutes(10)
            .jta(true)
            .maxPoolSize(1)
            .minPoolSize(1)
            .newConnectionSql("SELECT 1")
            .flushStrategy(PoolFlushStrategy.ENTIRE_POOL)
            .prefill(true)
            .preparedStatementsCacheSize(3)
            .queryTimeout(20)
            .setTxQueryTimeout(true)
            .sharePreparedStatements(true)
            .spy(true)
            .staleConnectionCheckerClass("org.jboss.jca.adapters.jdbc.extensions.novendor.NullStaleConnectionChecker")
            .addStaleConnectionCheckerProperty("stale-checker-prop", "ok")
            .statisticsEnabled(true)
            .trackPreparedStatements(TrackStatementType.TRUE)
            .transactionIsolation(TransactionIsolation.TRANSACTION_REPEATABLE_READ)
            .urlDelimiter(";")
            .useCcm(true)
            .useFastFailAllocation(false)
            .useJavaContext(true)
            .useStrictMinPoolSize(true)
            .useTryLock(60)
            .validateOnMatch(true)
            .validConnectionCheckerClass("org.jboss.jca.adapters.jdbc.extensions.novendor.NullValidConnectionChecker")
            .addValidConnectionCheckerProperty("valid-checker-prop", "ok")
            .build();

        assertXmlIdentical(subsystemXml, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addDataSource);
        assertXmlIdentical(SUBSYTEM_EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }
}
