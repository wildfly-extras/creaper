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

public class ChangeLogCategoryOfflineTest {

    private static final Logger log = Logger.getLogger(ChangeLogCategoryOfflineTest.class);

    private static final String LOGGER_ORIGINAL = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:logging:3.0\">\n"
            + "             %s"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String LOGGER_CHANGE_EXPECTED = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:logging:3.0\">\n"
            + "            <logger category=\"creaper.category\" use-parent-handlers=\"false\">\n"
            + "                <level name=\"TRACE\"/>\n"
            + "                <filter-spec value=\"match(&quot;filter&quot;)\"/>\n"
            + "                <handlers>\n"
            + "                    <handler name=\"HANDLER-1\"/>\n"
            + "                </handlers>\n"
            + "            </logger>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String LOGGER_REMOVE_HANDLERS_EXPECTED = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:logging:3.0\">\n"
            + "            <logger category=\"creaper.category\" use-parent-handlers=\"false\">\n"
            + "                <level name=\"TRACE\"/>\n"
            + "                <filter-spec value=\"match(&quot;filter&quot;)\"/>\n"
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
    public void changeExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        String loggingXmlOriginal = String.format(LOGGER_ORIGINAL, ""
                + "            <logger category=\"creaper.category\" use-parent-handlers=\"true\">\n"
                + "                <level name=\"OFF\"/>\n"
                + "                <filter-spec value=\"match(&quot;filter&quot;)\" />\n"
                + "                <handlers>\n"
                + "                    <handler name=\"HANDLER-1\"/>\n"
                + "                    <handler name=\"HANDLER-2\"/>\n"
                + "                </handlers>\n"
                + "            </logger>\n"
        );

        Files.write(loggingXmlOriginal, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        ChangeLogCategory changeLogCategory = new ChangeLogCategory.Builder("creaper.category")
                .level(Level.TRACE)
                .handlers("HANDLER-1")
                .setUseParentHandler(false)
                .build();

        assertXmlIdentical(loggingXmlOriginal, Files.toString(cfg, Charsets.UTF_8));
        client.apply(changeLogCategory);
        assertXmlIdentical(LOGGER_CHANGE_EXPECTED, Files.toString(cfg, Charsets.UTF_8));

    }

    @Test
    public void changeExisting2() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        String loggingXmlOriginal = String.format(LOGGER_ORIGINAL, ""
                + "            <logger category=\"creaper.category\" use-parent-handlers=\"true\">\n"
                + "                <level name=\"OFF\"/>\n"
                + "                <filter-spec value=\"match(&quot;filter&quot;)\" />\n"
                + "                <handlers>\n"
                + "                    <handler name=\"HANDLER-1\"/>\n"
                + "                </handlers>\n"
                + "            </logger>\n"
        );

        Files.write(loggingXmlOriginal, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        ChangeLogCategory changeLogCategory = new ChangeLogCategory.Builder("creaper.category")
                .level(Level.TRACE)
                .setUseParentHandler(false)
                .build();

        assertXmlIdentical(loggingXmlOriginal, Files.toString(cfg, Charsets.UTF_8));
        client.apply(changeLogCategory);
        assertXmlIdentical(LOGGER_CHANGE_EXPECTED, Files.toString(cfg, Charsets.UTF_8));

    }

    @Test
    public void addEverything() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        String loggingXmlOriginal = String.format(LOGGER_ORIGINAL, ""
                + "            <logger category=\"creaper.category\">\n"
                + "            </logger>\n"
        );

        Files.write(loggingXmlOriginal, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        ChangeLogCategory changeLogCategory = new ChangeLogCategory.Builder("creaper.category")
                .level(Level.TRACE)
                .handlers("HANDLER-1")
                .setUseParentHandler(false)
                .filter("match(\"filter\")")
                .build();

        assertXmlIdentical(loggingXmlOriginal, Files.toString(cfg, Charsets.UTF_8));
        client.apply(changeLogCategory);
        assertXmlIdentical(LOGGER_CHANGE_EXPECTED, Files.toString(cfg, Charsets.UTF_8));

    }

    @Test
    public void changeNothing() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        String loggingXmlOriginal = String.format(LOGGER_ORIGINAL, ""
                + "            <logger category=\"creaper.category\" use-parent-handlers=\"false\">\n"
                + "                <level name=\"TRACE\"/>\n"
                + "                <filter-spec value=\"match(&quot;filter&quot;)\"/>\n"
                + "                <handlers>\n"
                + "                    <handler name=\"HANDLER-1\"/>\n"
                + "                </handlers>\n"
                + "            </logger>\n"
        );

        Files.write(loggingXmlOriginal, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        ChangeLogCategory changeLogCategory = new ChangeLogCategory.Builder("creaper.category")
                .build();

        assertXmlIdentical(loggingXmlOriginal, Files.toString(cfg, Charsets.UTF_8));
        client.apply(changeLogCategory);
        assertXmlIdentical(LOGGER_CHANGE_EXPECTED, Files.toString(cfg, Charsets.UTF_8));

    }

    @Test
    public void removeHandlers() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        String loggingXmlOriginal = String.format(LOGGER_ORIGINAL, ""
                + "            <logger category=\"creaper.category\" use-parent-handlers=\"true\">\n"
                + "                <level name=\"TRACE\"/>\n"
                + "                <filter-spec value=\"match(&quot;filter&quot;)\" />\n"
                + "                <handlers>\n"
                + "                    <handler name=\"HANDLER-1\"/>\n"
                + "                    <handler name=\"HANDLER-2\"/>\n"
                + "                </handlers>\n"
                + "            </logger>\n"
        );

        Files.write(loggingXmlOriginal, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        ChangeLogCategory changeLogCategory = new ChangeLogCategory.Builder("creaper.category")
                .handlers(new String[]{})
                .setUseParentHandler(false)
                .build();

        assertXmlIdentical(loggingXmlOriginal, Files.toString(cfg, Charsets.UTF_8));
        client.apply(changeLogCategory);
        assertXmlIdentical(LOGGER_REMOVE_HANDLERS_EXPECTED, Files.toString(cfg, Charsets.UTF_8));

    }

    @Test(expected = CommandFailedException.class)
    public void changeNonExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        String loggingXmlOriginal = String.format(LOGGER_ORIGINAL, ""
                + "            <logger category=\"creaper.category\" use-parent-handlers=\"true\">\n"
                + "                <level name=\"TRACE\"/>\n"
                + "                <filter-spec value=\"match(&quot;filter&quot;)\" />\n"
                + "                <handlers>\n"
                + "                    <handler name=\"HANDLER-1\"/>\n"
                + "                    <handler name=\"HANDLER-2\"/>\n"
                + "                </handlers>\n"
                + "            </logger>\n"
        );

        Files.write(loggingXmlOriginal, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        ChangeLogCategory changeLogCategory = new ChangeLogCategory.Builder("NOT_EXISTING")
                .handlers(new String[]{})
                .build();

        assertXmlIdentical(loggingXmlOriginal, Files.toString(cfg, Charsets.UTF_8));
        client.apply(changeLogCategory);

    }
}
