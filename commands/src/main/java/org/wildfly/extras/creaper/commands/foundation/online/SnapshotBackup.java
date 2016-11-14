package org.wildfly.extras.creaper.commands.foundation.online;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.jboss.logging.Logger;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.CliException;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.admin.ReloadToSnapshot;

import com.google.common.base.Strings;

/**
 * Provides a pair of online commands to backup and then restore application
 * server configuration while the server is running. Backup consists of taking a
 * snapshot, restore then means reloading the server from the snapshot.
 *
 * <p>
 * The {@code backup} command must be applied before {@code restore},
 * {@code backup} can be applied once, and then {@code restore} can be applied
 * many times. If any one of these rules is violated, an exception is thrown.
 * For special circumstances, when the backup that was already acquired is no
 * longer needed and is not going to be restored, a {@code destroy} command is
 * provided. If there was no backup acquired, the destroy command does nothing.
 * </p>
 */
public final class SnapshotBackup {
    private static final Logger log = Logger.getLogger(SnapshotBackup.class);

    private String snapshotName; // null <=> backup wasn't acquired, can't restore

    private final OnlineCommand backupPart = new OnlineCommand() {
        @Override
        public void apply(OnlineCommandContext occ) throws CommandFailedException, IOException, CliException {
            if (SnapshotBackup.this.snapshotName != null) {
                throw new CommandFailedException("Snapshot was already taken: "
                        + SnapshotBackup.this.snapshotName);
            }
            ModelNodeResult modelNodeResult = occ.client.execute(":take-snapshot");
            // Ensure operation succeeded and value is returned
            modelNodeResult.assertDefinedValue("Taking snapshot failed.");
            String snaphotAbsolutePath = modelNodeResult.stringValue();
            SnapshotBackup.this.snapshotName = getSnapshotName(snaphotAbsolutePath);
        }

        /**
         * Extract file name of absolute path to snapshot
         *
         * <p>
         * Also handles situation when server and client are running on different platforms.
         * </p>
         *
         * @param snapshotAbsolutePath Absolute path to snapshot
         * @return name of snapshot or empty string if snapshotAbsolutePath is null or empty.
         */
        private String getSnapshotName(String snapshotAbsolutePath) {
            if (Strings.isNullOrEmpty(snapshotAbsolutePath)) {
                return "";
            }
            // Windows file names can't contain "/", so if there's a "/" in the full path, it's not Windows.
            String fileSeparator = "/";
            if (!snapshotAbsolutePath.contains("/")) {
                // absolute path is from windows environment
                fileSeparator = "\\";
            }
            return snapshotAbsolutePath.substring(snapshotAbsolutePath.lastIndexOf(fileSeparator) + 1);
        }

        @Override
        public String toString() {
            return "SnapshotBackup.backup";
        }
    };

    private final OnlineCommand restorePart = new OnlineCommand() {
        @Override
        public void apply(OnlineCommandContext occ) throws CommandFailedException, IOException, InterruptedException,
            TimeoutException {
            if (SnapshotBackup.this.snapshotName == null) {
                throw new CommandFailedException("There's no snapshot to restore");
            }
            ReloadToSnapshot reloadToSnapshot = new ReloadToSnapshot(occ.client, SnapshotBackup.this.snapshotName);
            reloadToSnapshot.perform();
        }

        @Override
        public String toString() {
            return "SnapshotBackup.restore";
        }
    };

    private final OnlineCommand destroyPart = new OnlineCommand() {
        @Override
        public void apply(OnlineCommandContext occ) throws CliException, IOException {
            if (SnapshotBackup.this.snapshotName == null) {
                return;
            }
            occ.client.executeCli(":delete-snapshot(name=" + SnapshotBackup.this.snapshotName + ")");
            SnapshotBackup.this.snapshotName = null;
        }

        @Override
        public String toString() {
            return "SnapshotBackup.destroy";
        }
    };

    public OnlineCommand backup() {
        return backupPart;
    }

    public OnlineCommand restore() {
        return restorePart;
    }

    public OnlineCommand destroy() {
        return destroyPart;
    }
}
