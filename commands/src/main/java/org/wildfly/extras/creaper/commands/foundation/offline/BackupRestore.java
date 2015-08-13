package org.wildfly.extras.creaper.commands.foundation.offline;

import org.wildfly.extras.creaper.core.offline.OfflineCommand;

/**
 * @deprecated Use {@link ConfigurationFileBackup}.
 */
public final class BackupRestore {
    private final ConfigurationFileBackup delegate = new ConfigurationFileBackup();

    public OfflineCommand backup() {
        return delegate.backup();
    }

    public OfflineCommand destroy() {
        return delegate.destroy();
    }

    public OfflineCommand restore() {
        return delegate.restore();
    }
}
