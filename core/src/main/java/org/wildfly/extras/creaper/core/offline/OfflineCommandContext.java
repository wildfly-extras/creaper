package org.wildfly.extras.creaper.core.offline;

import org.wildfly.extras.creaper.core.ManagementVersion;

import java.io.File;

public final class OfflineCommandContext {
    public final OfflineManagementClient client;
    public final OfflineOptions options; // same as client.options()
    public final ManagementVersion serverVersion;
    public final File configurationFile; // same as client.options().configurationFile

    @Deprecated
    public final ManagementVersion currentVersion;

    OfflineCommandContext(OfflineManagementClient client, ManagementVersion serverVersion) {
        this.client = client;
        this.options = client.options();
        this.serverVersion = serverVersion;
        this.configurationFile = client.options().configurationFile;

        this.currentVersion = serverVersion;
    }
}
