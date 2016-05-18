package org.wildfly.extras.creaper.commands.auditlog;

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

public class ChangeAuditLoggerOfflineTest {

    private static final String DEFAULT_LOGGER = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <audit-log>\n"
            + "            <logger enabled=\"false\" log-boot=\"true\" log-read-only=\"false\">\n"
            + "                <handlers/>\n"
            + "            </logger>\n"
            + "        </audit-log>\n"
            + "    </management>\n"
            + "</server>";

    private static final String CHANGED_LOGGER_WITH_ONE_HANDLER  = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <audit-log>\n"
            + "            <logger enabled=\"true\" log-boot=\"false\" log-read-only=\"true\">\n"
            + "               <handlers>\n"
            + "                    <handler name=\"test-creaper-handler\"/>\n"
            + "               </handlers>\n"
            + "            </logger>\n"
            + "        </audit-log>\n"
            + "    </management>\n"
            + "</server>";

    private static final String CHANGED_LOGGER_WITH_TWO_HANDLERS  = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <audit-log>\n"
            + "            <logger enabled=\"true\" log-boot=\"false\" log-read-only=\"true\">\n"
            + "               <handlers>\n"
            + "                    <handler name=\"test-creaper-handler\"/>\n"
            + "                    <handler name=\"test-creaper-handler2\"/>\n"
            + "               </handlers>\n"
            + "            </logger>\n"
            + "        </audit-log>\n"
            + "    </management>\n"
            + "</server>";

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Before
    public void setUp() {
        XMLUnit.setNormalizeWhitespace(true);
    }

    @Test
    public void changeLogger() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(DEFAULT_LOGGER, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        ChangeAuditLogger changeLogger = new ChangeAuditLogger.Builder()
                .enabled(true)
                .logBoot(false)
                .logReadOnly(true)
                .addHandlers("test-creaper-handler", "test-creaper-handler2")
                .build();

        assertXmlIdentical(DEFAULT_LOGGER, Files.toString(cfg, Charsets.UTF_8));

        client.apply(changeLogger);
        assertXmlIdentical(CHANGED_LOGGER_WITH_TWO_HANDLERS, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void changeNothing() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(CHANGED_LOGGER_WITH_ONE_HANDLER, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        ChangeAuditLogger changeLogger = new ChangeAuditLogger.Builder()
                .build();

        assertXmlIdentical(CHANGED_LOGGER_WITH_ONE_HANDLER, Files.toString(cfg, Charsets.UTF_8));

        client.apply(changeLogger);
        assertXmlIdentical(CHANGED_LOGGER_WITH_ONE_HANDLER, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addHandlerToExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(CHANGED_LOGGER_WITH_ONE_HANDLER, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        ChangeAuditLogger changeLogger = new ChangeAuditLogger.Builder()
                .addHandler("test-creaper-handler2")
                .build();

        assertXmlIdentical(CHANGED_LOGGER_WITH_ONE_HANDLER, Files.toString(cfg, Charsets.UTF_8));

        client.apply(changeLogger);
        assertXmlIdentical(CHANGED_LOGGER_WITH_TWO_HANDLERS, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void removeHandler() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(CHANGED_LOGGER_WITH_TWO_HANDLERS, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        ChangeAuditLogger changeLogger = new ChangeAuditLogger.Builder()
                .removeHandler("test-creaper-handler2")
                .build();

        assertXmlIdentical(CHANGED_LOGGER_WITH_TWO_HANDLERS, Files.toString(cfg, Charsets.UTF_8));

        client.apply(changeLogger);
        assertXmlIdentical(CHANGED_LOGGER_WITH_ONE_HANDLER, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void removeTwoHandlers() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(CHANGED_LOGGER_WITH_TWO_HANDLERS, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        ChangeAuditLogger changeLogger = new ChangeAuditLogger.Builder()
                .removeHandlers("test-creaper-handler", "test-creaper-handler2")
                .logBoot(true)
                .logReadOnly(false)
                .enabled(false)
                .build();

        assertXmlIdentical(CHANGED_LOGGER_WITH_TWO_HANDLERS, Files.toString(cfg, Charsets.UTF_8));

        client.apply(changeLogger);
        assertXmlIdentical(DEFAULT_LOGGER, Files.toString(cfg, Charsets.UTF_8));
    }
}
