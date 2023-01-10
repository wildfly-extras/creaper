package org.wildfly.extras.creaper.commands.logging;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wildfly.extras.creaper.XmlAssert;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;

import java.io.File;

import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;

public class ChangeConsoleLogHandlerOfflineTest {
    private static final String HANDLER_ORIGINAL = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:logging:3.0\">\n"
            + "             %s"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String HANDLER_CHANGE_EXPECTED = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:logging:3.0\">\n"
            + "            <console-handler name=\"consolehandler\" autoflush=\"false\" enabled=\"false\">\n"
            + "                <level name=\"FINEST\"/>\n"
            + "                <filter-spec value=\"match(&quot;filter*&quot;)\"/>"
            + "                <encoding value=\"UTF-8\"/>\n"
            + "                <formatter>\n"
            + "                    <pattern-formatter pattern=\"pattern\"/>\n"
            + "                </formatter>\n"
            + "                <target name=\"System.out\"/>\n"
            + "            </console-handler>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Before
    public void setUp() {
        XmlAssert.setNormalizeWhitespace(true);
    }

    @Test
    public void addAll() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        String loggingXmlOriginal = String.format(HANDLER_ORIGINAL, ""
                + "            <console-handler name=\"consolehandler\">\n"
                + "            </console-handler>\n"
        );

        Files.write(loggingXmlOriginal, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        ChangeConsoleLogHandler changeConsoleHandler = Logging.handler().console().change("consolehandler")
                .level(LogLevel.FINEST)
                .filter("match(\"filter*\")")
                .autoFlush(false)
                .enabled(false)
                .patternFormatter("pattern")
                .target(ConsoleTarget.STDOUT)
                .encoding(Charsets.UTF_8)
                .build();

        assertXmlIdentical(loggingXmlOriginal, Files.toString(cfg, Charsets.UTF_8));

        client.apply(changeConsoleHandler);

        assertXmlIdentical(HANDLER_CHANGE_EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void changeAll() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        String loggingXmlOriginal = String.format(HANDLER_ORIGINAL, ""
                + "            <console-handler name=\"consolehandler\" autoflush=\"true\" enabled=\"true\">\n"
                + "                <level name=\"WARN\"/>\n"
                + "                <filter-spec value=\"aa\"/>"
                + "                <encoding value=\"UTF-9\"/>\n"
                + "                <formatter>\n"
                + "                    <named-formatter name=\"PATTERN\"/>\n"
                + "                </formatter>\n"
                + "                <target name=\"System.err\"/>\n"
                + "            </console-handler>\n"
        );

        Files.write(loggingXmlOriginal, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        ChangeConsoleLogHandler changeConsoleHandler = Logging.handler().console().change("consolehandler")
                .level(LogLevel.FINEST)
                .filter("match(\"filter*\")")
                .autoFlush(false)
                .enabled(false)
                .patternFormatter("pattern")
                .target(ConsoleTarget.STDOUT)
                .encoding(Charsets.UTF_8)
                .build();

        assertXmlIdentical(loggingXmlOriginal, Files.toString(cfg, Charsets.UTF_8));

        client.apply(changeConsoleHandler);

        assertXmlIdentical(HANDLER_CHANGE_EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void changeSomething() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        String loggingXmlOriginal = String.format(HANDLER_ORIGINAL, ""
                + "            <console-handler name=\"consolehandler\" autoflush=\"false\" enabled=\"false\">\n"
                + "                <level name=\"FINEST\"/>\n"
                + "                <filter-spec value=\"aa\"/>"
                + "                <encoding value=\"UTF-8\"/>\n"
                + "                <formatter>\n"
                + "                    <pattern-formatter pattern=\"pattern\"/>\n"
                + "                </formatter>\n"
                + "                <target name=\"System.err\"/>\n"
                + "            </console-handler>\n"
        );

        Files.write(loggingXmlOriginal, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        ChangeConsoleLogHandler changeConsoleHandler = Logging.handler().console().change("consolehandler")
                .filter("match(\"filter*\")")
                .target(ConsoleTarget.STDOUT)
                .build();

        assertXmlIdentical(loggingXmlOriginal, Files.toString(cfg, Charsets.UTF_8));

        client.apply(changeConsoleHandler);

        assertXmlIdentical(HANDLER_CHANGE_EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void changeNothing() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        String loggingXmlOriginal = String.format(HANDLER_ORIGINAL, ""
                + "            <console-handler name=\"consolehandler\" autoflush=\"false\" enabled=\"false\">\n"
                + "                <level name=\"FINEST\"/>\n"
                + "                <filter-spec value=\"match(&quot;filter*&quot;)\"/>"
                + "                <encoding value=\"UTF-8\"/>\n"
                + "                <formatter>\n"
                + "                    <pattern-formatter pattern=\"pattern\"/>\n"
                + "                </formatter>\n"
                + "                <target name=\"System.out\"/>\n"
                + "            </console-handler>\n"
        );

        Files.write(loggingXmlOriginal, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        ChangeConsoleLogHandler changeConsoleHandler = Logging.handler().console().change("consolehandler")
                .build();

        assertXmlIdentical(loggingXmlOriginal, Files.toString(cfg, Charsets.UTF_8));

        client.apply(changeConsoleHandler);

        assertXmlIdentical(HANDLER_CHANGE_EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void nonExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        String loggingXmlOriginal = String.format(HANDLER_ORIGINAL, ""
                + "            <console-handler name=\"consolehandler\">\n"
                + "            </console-handler>\n"
        );

        Files.write(loggingXmlOriginal, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        ChangeConsoleLogHandler changeConsoleHandler = Logging.handler().console().change("NOTEXISTING")
                .level(LogLevel.FINEST)
                .build();

        assertXmlIdentical(loggingXmlOriginal, Files.toString(cfg, Charsets.UTF_8));

        client.apply(changeConsoleHandler);
    }
}
