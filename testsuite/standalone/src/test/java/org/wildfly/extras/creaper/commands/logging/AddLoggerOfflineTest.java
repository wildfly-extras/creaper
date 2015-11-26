package org.wildfly.extras.creaper.commands.logging;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;

import java.io.File;

import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;

public class AddLoggerOfflineTest {
    private static final String LOGGER_ORIGINAL = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:logging:3.0\">\n"
            + "             %s"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String LOGGER_ADD_EXPECTED = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:logging:3.0\">\n"
            + "            <logger category=\"creaper.category\" use-parent-handlers=\"true\">\n"
            + "                <level name=\"OFF\"/>\n"
            + "                <filter-spec value=\"match(&quot;filter&quot;)\"/>\n"
            + "                <handlers>\n"
            + "                    <handler name=\"HANDLER-1\"/>\n"
            + "                    <handler name=\"HANDLER-2\"/>\n"
            + "                </handlers>\n"
            + "            </logger>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Before
    public void setUp() {
        XMLUnit.setNormalizeWhitespace(true);
    }

    @Test
    public void addNew() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        String loggingXmlOriginal = String.format(LOGGER_ORIGINAL, "");

        Files.write(loggingXmlOriginal, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLogger addLogger = Logging.logger().add("creaper.category")
                .level(LogLevel.OFF)
                .filter("match(\"filter\")")
                .handlers("HANDLER-1", "HANDLER-2")
                .useParentHandler(true)
                .build();

        assertXmlIdentical(loggingXmlOriginal, Files.toString(cfg, Charsets.UTF_8));

        client.apply(addLogger);

        assertXmlIdentical(LOGGER_ADD_EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void replaceExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        String loggingXmlOriginal = String.format(LOGGER_ORIGINAL, ""
                + "            <logger category=\"creaper.category\" use-parent-handlers=\"true\">\n"
                + "                <level name=\"FINE\"/>\n"
                + "                <filter-spec value=\"match(&quot;filter&quot;)\" />\n"
                + "            </logger>\n"
        );

        Files.write(loggingXmlOriginal, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLogger addLogger = Logging.logger().add("creaper.category")
                .level(LogLevel.OFF)
                .filter("match(\"filter\")")
                .handlers("HANDLER-1", "HANDLER-2")
                .useParentHandler(true)
                .replaceExisting()
                .build();

        assertXmlIdentical(loggingXmlOriginal, Files.toString(cfg, Charsets.UTF_8));

        client.apply(addLogger);

        assertXmlIdentical(LOGGER_ADD_EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void replaceExisting2() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        String loggingXmlOriginal = String.format(LOGGER_ORIGINAL, ""
                + "            <logger category=\"creaper.category\" use-parent-handlers=\"true\">\n"
                + "                <level name=\"FINE\"/>\n"
                + "                <filter-spec value=\"match(&quot;filter&quot;)\" />\n"
                + "            </logger>\n"
        );

        Files.write(loggingXmlOriginal, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddLogger addLogger = Logging.logger().add("creaper.category")
                .level(LogLevel.OFF)
                .filter("match(\"filter\")")
                .handlers("HANDLER-1", "HANDLER-2")
                .useParentHandler(true)
                .build();

        assertXmlIdentical(loggingXmlOriginal, Files.toString(cfg, Charsets.UTF_8));

        client.apply(addLogger);
    }
}
