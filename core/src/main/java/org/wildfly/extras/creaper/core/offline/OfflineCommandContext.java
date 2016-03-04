package org.wildfly.extras.creaper.core.offline;

import org.wildfly.extras.creaper.core.ServerVersion;

import java.io.File;

public final class OfflineCommandContext {
    public final OfflineManagementClient client;
    public final OfflineOptions options; // same as client.options()
    public final ServerVersion version;
    public final File configurationFile; // same as client.options().configurationFile

    OfflineCommandContext(OfflineManagementClient client, ServerVersion version) {
        this.client = client;
        this.options = client.options();
        this.version = version;
        this.configurationFile = client.options().configurationFile;
    }
}
