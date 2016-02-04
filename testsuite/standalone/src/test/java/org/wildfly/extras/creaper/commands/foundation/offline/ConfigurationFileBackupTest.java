package org.wildfly.extras.creaper.commands.foundation.offline;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;

public class ConfigurationFileBackupTest {
    private static final String CFG_XML_1 = "<server xmlns='urn:jboss:domain:1.7'><foobar/></server>";
    private static final String CFG_XML_2 = "<server xmlns='urn:jboss:domain:1.7'><bazquux/></server>";

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    private File cfg;

    @Before
    public void setUpConfigurationFile() throws IOException {
        this.cfg = tmp.newFile("backupRestoreTest.xml");
        Files.write(CFG_XML_1, cfg, Charsets.UTF_8);
    }

    @Test(expected = CommandFailedException.class)
    public void restoreBeforeBackup() throws CommandFailedException, IOException {
        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build()
        );
        ConfigurationFileBackup configurationFileBackup = new ConfigurationFileBackup();

        client.apply(configurationFileBackup.restore()); // fail
    }

    @Test(expected = CommandFailedException.class)
    public void backupTwice() throws CommandFailedException, IOException, SAXException {
        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build()
        );
        ConfigurationFileBackup configurationFileBackup = new ConfigurationFileBackup();

        assertXmlIdentical(CFG_XML_1, Files.toString(cfg, Charsets.UTF_8));

        try {
            client.apply(configurationFileBackup.backup());
            client.apply(configurationFileBackup.backup()); // fail
        } finally {
            client.apply(configurationFileBackup.destroy());
        }
    }

    @Test(expected = CommandFailedException.class)
    public void restoreTwice() throws CommandFailedException, IOException, SAXException {
        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build()
        );
        ConfigurationFileBackup configurationFileBackup = new ConfigurationFileBackup();

        assertXmlIdentical(CFG_XML_1, Files.toString(cfg, Charsets.UTF_8));

        client.apply(configurationFileBackup.backup());

        Files.write(CFG_XML_2, cfg, Charsets.UTF_8);
        assertXmlIdentical(CFG_XML_2, Files.toString(cfg, Charsets.UTF_8));

        client.apply(configurationFileBackup.restore());

        assertXmlIdentical(CFG_XML_1, Files.toString(cfg, Charsets.UTF_8));

        client.apply(configurationFileBackup.restore()); // fail
    }

    @Test
    public void correctUsage() throws CommandFailedException, IOException, SAXException {
        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build()
        );
        ConfigurationFileBackup configurationFileBackup = new ConfigurationFileBackup();

        assertXmlIdentical(CFG_XML_1, Files.toString(cfg, Charsets.UTF_8));

        client.apply(configurationFileBackup.backup());

        Files.write(CFG_XML_2, cfg, Charsets.UTF_8);
        assertXmlIdentical(CFG_XML_2, Files.toString(cfg, Charsets.UTF_8));

        client.apply(configurationFileBackup.restore());

        assertXmlIdentical(CFG_XML_1, Files.toString(cfg, Charsets.UTF_8));
    }
}
