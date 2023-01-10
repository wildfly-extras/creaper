package org.wildfly.extras.creaper.commands.auditlog;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wildfly.extras.creaper.XmlAssert;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;

import static org.junit.Assert.fail;
import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;

public class AddAuditLoggerOfflineTest {

    private static final String NO_LOGGER = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <audit-log/>\n"
            + "    </management>\n"
            + "</server>";

    private static final String DEFAULT_LOGGER = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <audit-log>\n"
            + "            <logger enabled=\"false\" log-boot=\"true\" log-read-only=\"false\"/>\n"
            + "        </audit-log>\n"
            + "    </management>\n"
            + "</server>";

    private static final String UPDATED_LOGGER = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <audit-log>\n"
            + "            <logger enabled=\"true\" log-boot=\"false\" log-read-only=\"true\"/>\n"
            + "        </audit-log>\n"
            + "    </management>\n"
            + "</server>";

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Before
    public void setUp() {
        XmlAssert.setNormalizeWhitespace(true);
    }

    @Test
    public void addLogger() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(NO_LOGGER, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuditLogger addLogger = new AddAuditLogger.Builder()
                .enabled(false)
                .logBoot(true)
                .logReadOnly(false)
                .build();

        assertXmlIdentical(NO_LOGGER, Files.toString(cfg, Charsets.UTF_8));

        client.apply(addLogger);
        assertXmlIdentical(DEFAULT_LOGGER, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void replaceExistingLogger() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(DEFAULT_LOGGER, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuditLogger addLogger = new AddAuditLogger.Builder()
                .enabled(true)
                .logBoot(false)
                .logReadOnly(true)
                .replaceExisting()
                .build();

        assertXmlIdentical(DEFAULT_LOGGER, Files.toString(cfg, Charsets.UTF_8));

        client.apply(addLogger);
        assertXmlIdentical(UPDATED_LOGGER, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void replaceExistingLogger_notAllowed() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(DEFAULT_LOGGER, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuditLogger addLogger = new AddAuditLogger.Builder()
                .enabled(true)
                .logBoot(false)
                .logReadOnly(true)
                .build();

        assertXmlIdentical(DEFAULT_LOGGER, Files.toString(cfg, Charsets.UTF_8));

        client.apply(addLogger);
        fail("Logger already exists, an exception should be thrown");
    }
}
