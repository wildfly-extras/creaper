package org.wildfly.extras.creaper.commands.foundation.offline;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ConfigurationFileBackupTest {
    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    private File cfg;

    @Before
    public void setUpConfigurationFile() throws IOException {
        this.cfg = tmp.newFile("backupRestoreTest.xml");
        Files.write("foobar", cfg, Charsets.UTF_8);
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
    public void backupTwice() throws CommandFailedException, IOException {
        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build()
        );
        ConfigurationFileBackup configurationFileBackup = new ConfigurationFileBackup();

        assertEquals("foobar", Files.toString(cfg, Charsets.UTF_8));

        try {
            client.apply(configurationFileBackup.backup());
            client.apply(configurationFileBackup.backup()); // fail
        } finally {
            client.apply(configurationFileBackup.destroy());
        }
    }

    @Test(expected = CommandFailedException.class)
    public void restoreTwice() throws CommandFailedException, IOException {
        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build()
        );
        ConfigurationFileBackup configurationFileBackup = new ConfigurationFileBackup();

        assertEquals("foobar", Files.toString(cfg, Charsets.UTF_8));

        client.apply(configurationFileBackup.backup());

        Files.write("bazquux", cfg, Charsets.UTF_8);
        assertEquals("bazquux", Files.toString(cfg, Charsets.UTF_8));

        client.apply(configurationFileBackup.restore());

        assertEquals("foobar", Files.toString(cfg, Charsets.UTF_8));

        client.apply(configurationFileBackup.restore()); // fail
    }

    @Test
    public void correctUsage() throws CommandFailedException, IOException {
        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build()
        );
        ConfigurationFileBackup configurationFileBackup = new ConfigurationFileBackup();

        assertEquals("foobar", Files.toString(cfg, Charsets.UTF_8));

        client.apply(configurationFileBackup.backup());

        Files.write("bazquux", cfg, Charsets.UTF_8);
        assertEquals("bazquux", Files.toString(cfg, Charsets.UTF_8));

        client.apply(configurationFileBackup.restore());

        assertEquals("foobar", Files.toString(cfg, Charsets.UTF_8));
    }
}
