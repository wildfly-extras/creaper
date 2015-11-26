package org.wildfly.extras.creaper.commands.logging;


import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.custommonkey.xmlunit.XMLUnit;
import org.jboss.logging.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;

import java.io.File;

import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;

/**
 * @author Ivan Straka istraka@redhat.com
 */

public class ChangeRootLoggerOfflineTest {
    private static final Logger log = Logger.getLogger(ChangeRootLoggerOfflineTest.class);

    private static final String ROOT_ORIGINAL = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:logging:3.0\">\n"
            + "            <root-logger>\n"
            + "              %s"
            + "            </root-logger>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String ROOT_CHANGE_EXPECTED_1 = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:logging:3.0\">\n"
            + "            <root-logger>\n"
            + "                <level name=\"OFF\"/>\n"
            + "                <filter-spec value=\"match(&quot;filter&quot;)\"/>\n"
            + "                <handlers>\n"
            + "                    <handler name=\"HANDLER-1\"/>\n"
            + "                </handlers>\n"
            + "            </root-logger>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String ROOT_CHANGE_EXPECTED_2 = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:logging:3.0\">\n"
            + "            <root-logger>\n"
            + "                <level name=\"FINE\"/>\n"
            + "                <filter-spec value=\"match(&quot;filter&quot;)\"/>\n"
            + "            </root-logger>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String ROOT_CHANGE_EXPECTED_3 = ""
            + "<server xmlns=\"urn:jboss:domain:4.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:logging:3.0\">\n"
            + "            <root-logger>\n"
            + "                <level name=\"OFF\"/>\n"
            + "                <filter-spec value=\"match(&quot;filter&quot;)\"/>\n"
            + "                <handlers>\n"
            + "                    <handler name=\"HANDLER-1\"/>\n"
            + "                    <handler name=\"HANDLER-2\"/>\n"
            + "                </handlers>\n"
            + "            </root-logger>\n"
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
    public void changeExistingNodes() throws Exception {

        File cfg = tmp.newFile("xmlTransform.xml");
        String loggingXmlOriginal = String.format(ROOT_ORIGINAL, ""
                + "                <level name=\"INFO\"/>\n"
                + "                <filter-spec value=\"match(&quot;a&quot;)\"/>\n"
                + "                <handlers>\n"
                + "                    <handler name=\"CONSOLE\"/>\n"
                + "                    <handler name=\"FILE\"/>\n"
                + "                </handlers>\n");

        Files.write(loggingXmlOriginal, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        ChangeRootLogger addLogCategory = new ChangeRootLogger.Builder()
                .changeLevel(Level.OFF)
                .changeFilter("match(\"filter\")")
                .changeHandler("HANDLER-1")
                .build();

        assertXmlIdentical(loggingXmlOriginal, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLogCategory);

        assertXmlIdentical(ROOT_CHANGE_EXPECTED_1, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void changeExistingNodes2() throws Exception {

        File cfg = tmp.newFile("xmlTransform.xml");
        String loggingXmlOriginal = String.format(ROOT_ORIGINAL, ""
                + "                <level name=\"INFO\"/>\n"
                + "                <filter-spec value=\"match(&quot;filter&quot;)\"/>\n"
                + "                <handlers>\n"
                + "                    <handler name=\"CONSOLE\"/>\n"
                + "                    <handler name=\"FILE\"/>\n"
                + "                </handlers>\n");

        Files.write(loggingXmlOriginal, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        ChangeRootLogger addLogCategory = new ChangeRootLogger.Builder()
                .changeLevel(Level.FINE)
                .changeHandlers(new String[]{})
                .build();

        assertXmlIdentical(loggingXmlOriginal, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLogCategory);

        assertXmlIdentical(ROOT_CHANGE_EXPECTED_2, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void changeNonExistingNodes() throws Exception {

        File cfg = tmp.newFile("xmlTransform.xml");
        String loggingXmlOriginal = String.format(ROOT_ORIGINAL,
                "                <level name=\"OFF\"/>\n");

        Files.write(loggingXmlOriginal, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        ChangeRootLogger addLogCategory = new ChangeRootLogger.Builder()
                .changeHandlers("HANDLER-1", "HANDLER-2")
                .changeFilter("match(\"filter\")")
                .build();

        assertXmlIdentical(loggingXmlOriginal, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addLogCategory);

        assertXmlIdentical(ROOT_CHANGE_EXPECTED_3, Files.toString(cfg, Charsets.UTF_8));
    }

}
