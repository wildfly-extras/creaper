package org.wildfly.extras.creaper.commands.datasources;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.jboss.logging.Logger;
import org.wildfly.extras.creaper.XmlAssert;
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

public class RemoveXADatasourceOfflineTest {
    private static final Logger log = Logger.getLogger(RemoveXADatasourceOfflineTest.class);

    private static final String SUBSYSTEM_ORIGINAL = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:datasources:1.2\">\n"
            + "            <datasources>\n"
            + "                <xa-datasource pool-name=\"creaper-ds\" jndi-name=\"java:/jboss/datasources/creaper-ds\">\n"
            + "                    <connection-url>jdbc:h2:mem:test-creaper;DB_CLOSE_DELAY=-1;</connection-url>\n"
            + "                </xa-datasource>\n"
            + "            </datasources>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";
    private static final String SUBSYSTEM_EXPECTED = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:datasources:1.2\">\n"
            + "            <datasources/>\n"
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
    public void transform() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_ORIGINAL, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYSTEM_ORIGINAL, Files.toString(cfg, Charsets.UTF_8));
        client.apply(new RemoveXADataSource("creaper-ds"));
        assertXmlIdentical(SUBSYSTEM_EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void transformDsNotExists() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_ORIGINAL, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYSTEM_ORIGINAL, Files.toString(cfg, Charsets.UTF_8));
        client.apply(new RemoveXADataSource("creaper-ds-not-existing"));
        fail("The datasource should not exist in configuration, so an exception should be thrown");
    }
}
