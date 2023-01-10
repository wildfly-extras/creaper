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

public class AddAuditLogFileHandlerOfflineTest {

    private static final String TEST_HANDLER_NAME = "creaper-file-handler";

    private static final String NO_HANDLERS = ""
            + "<server xmlns=\"urn:jboss:domain:4.1\">\n"
            + "    <management>\n"
            + "        <audit-log>\n"
            + "             <formatters>\n"
            + "                <json-formatter name=\"json-formatter\"/>\n"
            + "            </formatters>"
            + "            <handlers/>\n"
            + "        </audit-log>\n"
            + "    </management>\n"
            + "</server>";

    private static final String ADDED_FILE_HANDLER = ""
            + "<server xmlns=\"urn:jboss:domain:4.1\">\n"
            + "    <management>\n"
            + "        <audit-log>\n"
            + "             <formatters>\n"
            + "                <json-formatter name=\"json-formatter\"/>\n"
            + "            </formatters>"
            + "            <handlers>\n"
            + "                <file-handler name=\"creaper-file-handler\" formatter=\"json-formatter\" max-failure-count=\"10\" path=\"audit-log.log\" relative-to=\"jboss.dir\"/>"
            + "           </handlers>\n"
            + "        </audit-log>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REPLACED_FILE_HANDLER_EXPECTED = ""
            + "<server xmlns=\"urn:jboss:domain:4.1\">\n"
            + "    <management>\n"
            + "        <audit-log>\n"
            + "             <formatters>\n"
            + "                <json-formatter name=\"json-formatter\"/>\n"
            + "            </formatters>"
            + "            <handlers>\n"
            + "                <file-handler name=\"creaper-file-handler\" formatter=\"json-formatter\" max-failure-count=\"20\" path=\"different-audit-log.log\" relative-to=\"/tmp\"/>"
            + "           </handlers>\n"
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
    public void addFileHandler() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(NO_HANDLERS, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuditLogFileHandler addFileHandler = new AddAuditLogFileHandler.Builder(TEST_HANDLER_NAME)
                .formatter("json-formatter")
                .maxFailureCount(10)
                .path("audit-log.log")
                .relativeTo("jboss.dir")
                .build();

        assertXmlIdentical(NO_HANDLERS, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addFileHandler);

        assertXmlIdentical(ADDED_FILE_HANDLER, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void overrideExistingFileHandler() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(ADDED_FILE_HANDLER, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuditLogFileHandler addFileHandler = new AddAuditLogFileHandler.Builder(TEST_HANDLER_NAME)
                .formatter("json-formatter")
                .path("different-audit-log.log")
                .maxFailureCount(20)
                .relativeTo("/tmp")
                .replaceExisting()
                .build();

        assertXmlIdentical(ADDED_FILE_HANDLER, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addFileHandler);

        assertXmlIdentical(REPLACED_FILE_HANDLER_EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void overrideExistingFileHandler_notAllowed() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(ADDED_FILE_HANDLER, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuditLogFileHandler addFileHandler = new AddAuditLogFileHandler.Builder(TEST_HANDLER_NAME)
                .formatter("json-formatter")
                .path("different-audit-log.log")
                .maxFailureCount(20)
                .relativeTo("jboss.dir")
                .build();

        assertXmlIdentical(ADDED_FILE_HANDLER, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addFileHandler);

        fail("File handler creaper-file-handler already exists, an exception should be thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addFileHandler_nullPath() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(NO_HANDLERS, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuditLogFileHandler addFileHandler = new AddAuditLogFileHandler.Builder(TEST_HANDLER_NAME)
                .formatter("json-formatter")
                .path(null)
                .build();

        assertXmlIdentical(NO_HANDLERS, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addFileHandler);

        fail("Creating command with null path should throw an exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addFileHandler_emptyPath() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(NO_HANDLERS, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuditLogFileHandler addFileHandler = new AddAuditLogFileHandler.Builder(TEST_HANDLER_NAME)
                .formatter("json-formatter")
                .path("")
                .build();

        assertXmlIdentical(NO_HANDLERS, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addFileHandler);

        fail("Creating command with empty path should throw an exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addFileHandler_nullFormatter() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(NO_HANDLERS, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuditLogFileHandler addFileHandler = new AddAuditLogFileHandler.Builder(TEST_HANDLER_NAME)
                .formatter(null)
                .path("/tmp/audit-log.log")
                .build();

        assertXmlIdentical(NO_HANDLERS, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addFileHandler);

        fail("Creating command with null formatter should throw an exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addFileHandler_emptyFormatter() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(NO_HANDLERS, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuditLogFileHandler addFileHandler = new AddAuditLogFileHandler.Builder(TEST_HANDLER_NAME)
                .formatter("")
                .path("/tmp/audit-log.log")
                .build();

        assertXmlIdentical(NO_HANDLERS, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addFileHandler);

        fail("Creating command with empty formatter should throw an exception");
    }
}
