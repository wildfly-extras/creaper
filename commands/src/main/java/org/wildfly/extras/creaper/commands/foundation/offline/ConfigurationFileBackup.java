package org.wildfly.extras.creaper.commands.foundation.offline;

import com.google.common.io.Files;
import org.jboss.logging.Logger;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;

import java.io.File;
import java.io.IOException;

/**
 * Provides a pair of offline commands to backup and then restore the configuration file. The {@code backup} command
 * must be applied before {@code restore}, and both commands can only be applied once. If any one of these rules is
 * violated, an exception is thrown. For special circumstances, when the backup that was already acquired is no longer
 * needed and is not going to be restored, a {@code destroy} command is provided. If there was no backup acquired,
 * the destroy command does nothing.
 */
public final class ConfigurationFileBackup {
    private static final Logger log = Logger.getLogger(ConfigurationFileBackup.class);

    private File backupFile; // null <=> backup wasn't acquired, can't restore

    private final OfflineCommand backupPart = new OfflineCommand() {
        @Override
        public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {
            if (ConfigurationFileBackup.this.backupFile != null) {
                throw new CommandFailedException("Configuration file was already backed up to "
                        + ConfigurationFileBackup.this.backupFile);
            }

            File tempFile = File.createTempFile("creaper-backup", null);
            Files.copy(ctx.configurationFile, tempFile);
            ConfigurationFileBackup.this.backupFile = tempFile;
        }

        @Override
        public String toString() {
            return "ConfigurationFileBackup.backup";
        }
    };

    private final OfflineCommand restorePart = new OfflineCommand() {
        @Override
        public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {
            if (ConfigurationFileBackup.this.backupFile == null) {
                throw new CommandFailedException("There's no configuration file backup to restore");
            }

            Files.copy(ConfigurationFileBackup.this.backupFile, ctx.configurationFile);
            boolean deleted = ConfigurationFileBackup.this.backupFile.delete();
            if (!deleted) {
                log.errorf("Couldn't delete %s, continuing", ConfigurationFileBackup.this.backupFile);
            }
            ConfigurationFileBackup.this.backupFile = null;
        }

        @Override
        public String toString() {
            return "ConfigurationFileBackup.restore";
        }
    };

    private final OfflineCommand destroyPart = new OfflineCommand() {
        @Override
        public void apply(OfflineCommandContext ctx) {
            if (ConfigurationFileBackup.this.backupFile == null) {
                return;
            }

            boolean deleted = ConfigurationFileBackup.this.backupFile.delete();
            if (!deleted) {
                log.errorf("Couldn't delete %s, continuing", ConfigurationFileBackup.this.backupFile);
            }
            ConfigurationFileBackup.this.backupFile = null;
        }

        @Override
        public String toString() {
            return "ConfigurationFileBackup.destroy";
        }
    };

    public OfflineCommand backup() {
        return backupPart;
    }

    public OfflineCommand restore() {
        return restorePart;
    }

    public OfflineCommand destroy() {
        return destroyPart;
    }
}
