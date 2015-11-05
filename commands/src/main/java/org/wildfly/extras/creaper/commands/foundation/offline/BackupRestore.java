package org.wildfly.extras.creaper.commands.foundation.offline;

import org.wildfly.extras.creaper.core.offline.OfflineCommand;

/** @deprecated use {@link ConfigurationFileBackup} instead, this will be removed before 1.0 */
@Deprecated
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
