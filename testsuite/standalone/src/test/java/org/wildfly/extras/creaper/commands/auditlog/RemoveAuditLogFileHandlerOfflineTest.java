package org.wildfly.extras.creaper.commands.auditlog;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;

import static org.junit.Assert.fail;
import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;

public class RemoveAuditLogFileHandlerOfflineTest {

    private static final String TEST_FILE_HANDLER = ""
            + "<server xmlns=\"urn:jboss:domain:4.1\">\n"
            + "    <management>\n"
            + "        <audit-log>\n"
            + "            <handlers>\n"
            + "                <file-handler name=\"test-file-handler\" formatter=\"json-formatter\" path=\"audit-log.log\" relative-to=\"jboss.server.data.dir\"/>"
            + "           </handlers>\n"
            + "        </audit-log>\n"
            + "    </management>\n"
            + "</server>";

    private static final String NO_FILE_HANDLERS = ""
            + "<server xmlns=\"urn:jboss:domain:4.1\">\n"
            + "    <management>\n"
            + "        <audit-log>\n"
            + "            <handlers/>\n"
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
    public void removeFileHandler() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(TEST_FILE_HANDLER, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        RemoveAuditLogFileHandler removeFileHandler = new RemoveAuditLogFileHandler("test-file-handler");

        assertXmlIdentical(TEST_FILE_HANDLER, Files.toString(cfg, Charsets.UTF_8));

        client.apply(removeFileHandler);
        assertXmlIdentical(NO_FILE_HANDLERS, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void removeNonExistingFileHandler() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(TEST_FILE_HANDLER, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        RemoveAuditLogFileHandler removeFileHandler = new RemoveAuditLogFileHandler("non-existing-file-handler");

        assertXmlIdentical(TEST_FILE_HANDLER, Files.toString(cfg, Charsets.UTF_8));

        client.apply(removeFileHandler);

        fail("Specified file handler does not exist in configuration, an exception should be thrown");
    }
}
