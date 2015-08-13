package org.wildfly.extras.creaper.commands.datasources;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.custommonkey.xmlunit.XMLUnit;
import org.jboss.logging.Logger;
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

public class AddRemoveJdbcDriverOfflineTest {
    private static final Logger log = Logger.getLogger(AddRemoveJdbcDriverOfflineTest.class);

    private static final String SUBSYSTEM_WITH_DRIVER = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:datasources:1.2\">\n"
            + "            <datasources>\n"
            + "                <drivers>\n"
            + "                    <driver name=\"h2\" module=\"com.h2database.h2:main\">\n"
            + "                        <driver-class>org.h2.Driver</driver-class>\n"
            + "                        <datasource-class>org.h2.jdbcx.JdbcDataSource</datasource-class>\n"
            + "                        <xa-datasource-class>org.h2.jdbcx.JdbcDataSource</xa-datasource-class>\n"
            + "                    </driver>\n"
            + "                </drivers>\n"
            + "            </datasources>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";
    private static final String SUBSYSTEM_WITH_EMPTY_DRIVERS = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:datasources:1.2\">\n"
            + "            <datasources>\n"
            + "               <drivers/>\n"
            + "            </datasources>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";
    private static final String SUBSYSTEM_EMPTY = ""
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
        XMLUnit.setNormalizeWhitespace(true);
    }

    @Test
    public void transformAddDriver() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_EMPTY, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYSTEM_EMPTY, Files.toString(cfg, Charsets.UTF_8));
        client.apply(new AddJdbcDriver.Builder("h2", "com.h2database.h2")
            .driverClass("org.h2.Driver")
            .datasourceClass("org.h2.jdbcx.JdbcDataSource")
            .xaDatasourceClass("org.h2.jdbcx.JdbcDataSource")
            .moduleSlot("main")
            .build());
        assertXmlIdentical(SUBSYSTEM_WITH_DRIVER, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void transformAddDriver_existingDriversTag() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_WITH_EMPTY_DRIVERS, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYSTEM_WITH_EMPTY_DRIVERS, Files.toString(cfg, Charsets.UTF_8));
        client.apply(new AddJdbcDriver.Builder("h2", "com.h2database.h2")
            .driverClass("org.h2.Driver")
            .datasourceClass("org.h2.jdbcx.JdbcDataSource")
            .xaDatasourceClass("org.h2.jdbcx.JdbcDataSource")
            .moduleSlot("main")
            .build());
        assertXmlIdentical(SUBSYSTEM_WITH_DRIVER, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void transformAddDriverExists() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_WITH_DRIVER, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYSTEM_WITH_DRIVER, Files.toString(cfg, Charsets.UTF_8));
        client.apply(new AddJdbcDriver.Builder("h2", "com.h2database.h2").build());
        fail("Driver 'h2' should exist in configuration, so adding shouldn't throw an exception");
    }

    @Test
    public void transformRemoveDriver() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_WITH_DRIVER, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYSTEM_WITH_DRIVER, Files.toString(cfg, Charsets.UTF_8));
        client.apply(new RemoveJdbcDriver("h2"));
        assertXmlIdentical(SUBSYSTEM_WITH_EMPTY_DRIVERS, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void transformRemoveDriverNotExists() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_EMPTY, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYSTEM_EMPTY, Files.toString(cfg, Charsets.UTF_8));
        client.apply(new RemoveJdbcDriver("h2-not-existing"));
        fail("Driver 'h2-not-existing' should not exist in configuration, so an exception should be thrown");
    }
}
