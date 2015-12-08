package org.wildfly.extras.creaper.commands.logging;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.custommonkey.xmlunit.XMLUnit;
import org.jboss.logging.Logger;
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

public class ChangePeriodicHandlerOfflineTest {
    private static final Logger log = Logger.getLogger(ChangePeriodicHandlerOfflineTest.class);

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
            + "            <periodic-rotating-file-handler name=\"handler\" autoflush=\"false\" enabled=\"false\">\n"
            + "                <level name=\"FINEST\"/>\n"
            + "                <filter-spec value=\"match(&quot;filter*&quot;)\"/>"
            + "                <encoding value=\"UTF-8\"/>\n"
            + "                <formatter>\n"
            + "                    <named-formatter name=\"PATTERN\"/>\n"
            + "                </formatter>\n"
            + "                <file relative-to=\"jboss.server.log.dir\" path=\"server.log\"/>\n"
            + "                <suffix value=\".suffix\"/>\n"
            + "                <append value=\"true\"/>\n"
            + "            </periodic-rotating-file-handler>\n"
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
    public void addAll() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        String loggingXmlOriginal = String.format(HANDLER_ORIGINAL, ""
                + "            <periodic-rotating-file-handler name=\"handler\">\n"
                + "            </periodic-rotating-file-handler>\n"
        );

        Files.write(loggingXmlOriginal, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        ChangePeriodicHandler changePeriodicHandler = new ChangePeriodicHandler.Builder("handler", "server.log", ".suffix")
                .level(Level.FINEST)
                .filter("match(\"filter*\")")
                .setAutoFlush(false)
                .setEnabled(false)
                .namedFormatter("PATTERN")
                .setAppend(true)
                .fileRelativeTo("jboss.server.log.dir")
                .encoding(Charsets.UTF_8)
                .build();

        assertXmlIdentical(loggingXmlOriginal, Files.toString(cfg, Charsets.UTF_8));
        client.apply(changePeriodicHandler);
        assertXmlIdentical(HANDLER_CHANGE_EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void changeAll() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        String loggingXmlOriginal = String.format(HANDLER_ORIGINAL, ""
                + "            <periodic-rotating-file-handler name=\"handler\" autoflush=\"true\" enabled=\"true\">\n"
                + "                <level name=\"WARN\"/>\n"
                + "                <filter-spec value=\"match(&quot;filter*&quot;)\"/>"
                + "                <encoding value=\"UTF-8\"/>\n"
                + "                <formatter>\n"
                + "                    <pattern-formatter pattern=\"p\"/>\n"
                + "                </formatter>\n"
                + "                <file relative-to=\"jboss.server.log.dir.bad\" path=\"server.log-bad\"/>\n"
                + "                <suffix value=\".bad-suffix\"/>\n"
                + "                <append value=\"false\"/>\n"
                + "            </periodic-rotating-file-handler>\n"
        );

        Files.write(loggingXmlOriginal, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        ChangePeriodicHandler changePeriodicHandler = new ChangePeriodicHandler.Builder("handler", "server.log", ".suffix")
                .level(Level.FINEST)
                .filter("match(\"filter*\")")
                .setAutoFlush(false)
                .setEnabled(false)
                .namedFormatter("PATTERN")
                .setAppend(true)
                .fileRelativeTo("jboss.server.log.dir")
                .encoding(Charsets.UTF_8)
                .build();

        assertXmlIdentical(loggingXmlOriginal, Files.toString(cfg, Charsets.UTF_8));
        client.apply(changePeriodicHandler);
        assertXmlIdentical(HANDLER_CHANGE_EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void changeSomething() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        String loggingXmlOriginal = String.format(HANDLER_ORIGINAL, ""
                + "            <periodic-rotating-file-handler name=\"handler\" autoflush=\"false\" enabled=\"false\">\n"
                + "                <level name=\"FINEST\"/>\n"
                + "                <filter-spec value=\"match(&quot;bad&quot;)\"/>"
                + "                <encoding value=\"UTF-9\"/>\n"
                + "                <formatter>\n"
                + "                    <named-formatter name=\"PATTERN\"/>\n"
                + "                </formatter>\n"
                + "                <file relative-to=\"jboss.server.log.dir\" path=\"server.log\"/>\n"
                + "                <suffix value=\".suffix\"/>\n"
                + "                <append value=\"true\"/>\n"
                + "            </periodic-rotating-file-handler>\n"
        );

        Files.write(loggingXmlOriginal, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        ChangePeriodicHandler changePeriodicHandler = new ChangePeriodicHandler.Builder("handler", "server.log", ".suffix")
                .filter("match(\"filter*\")")
                .encoding(Charsets.UTF_8)
                .build();

        assertXmlIdentical(loggingXmlOriginal, Files.toString(cfg, Charsets.UTF_8));
        client.apply(changePeriodicHandler);
        assertXmlIdentical(HANDLER_CHANGE_EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void changeNothing() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        String loggingXmlOriginal = String.format(HANDLER_ORIGINAL, ""
                + "            <periodic-rotating-file-handler name=\"handler\" autoflush=\"false\" enabled=\"false\">\n"
                + "                <level name=\"FINEST\"/>\n"
                + "                <filter-spec value=\"match(&quot;filter*&quot;)\"/>"
                + "                <encoding value=\"UTF-8\"/>\n"
                + "                <formatter>\n"
                + "                    <named-formatter name=\"PATTERN\"/>\n"
                + "                </formatter>\n"
                + "                <file relative-to=\"jboss.server.log.dir\" path=\"server.log\"/>\n"
                + "                <suffix value=\".suffix\"/>\n"
                + "                <append value=\"true\"/>\n"
                + "            </periodic-rotating-file-handler>\n"
        );

        Files.write(loggingXmlOriginal, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        ChangePeriodicHandler changePeriodicHandler = new ChangePeriodicHandler.Builder("handler", null, null)
                .build();

        assertXmlIdentical(loggingXmlOriginal, Files.toString(cfg, Charsets.UTF_8));
        client.apply(changePeriodicHandler);
        assertXmlIdentical(HANDLER_CHANGE_EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void nonExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        String loggingXmlOriginal = String.format(HANDLER_ORIGINAL, ""
                + "            <periodic-rotating-file-handler name=\"handler\" autoflush=\"false\" enabled=\"false\">\n"
                + "                <level name=\"FINEST\"/>\n"
                + "                <filter-spec value=\"match(&quot;bad&quot;)\"/>"
                + "                <encoding value=\"UTF-9\"/>\n"
                + "                <formatter>\n"
                + "                    <named-formatter name=\"PATTERN\"/>\n"
                + "                </formatter>\n"
                + "                <file relative-to=\"jboss.server.log.dir\" path=\"server.log\"/>\n"
                + "                <suffix value=\".suffix\"/>\n"
                + "                <append value=\"true\"/>\n"
                + "            </periodic-rotating-file-handler>\n"
        );

        Files.write(loggingXmlOriginal, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        ChangePeriodicHandler changePeriodicHandler = new ChangePeriodicHandler.Builder("Noexisting", "server.log", ".suffix")
                .filter("match(\"filter*\")")
                .encoding(Charsets.UTF_8)
                .build();

        assertXmlIdentical(loggingXmlOriginal, Files.toString(cfg, Charsets.UTF_8));
        client.apply(changePeriodicHandler);
    }

}
