package org.wildfly.extras.creaper.core.offline;

import org.wildfly.extras.creaper.core.ManagementVersion;
import org.wildfly.extras.creaper.core.ServerVersion;

import java.io.File;

public final class OfflineCommandContext {
    public final OfflineManagementClient client;
    public final OfflineOptions options; // same as client.options()
    public final ServerVersion version;
    public final File configurationFile; // same as client.options().configurationFile

    /** @deprecated use {@link #version} instead, this will be removed before 1.0 */
    @Deprecated
    public final ManagementVersion currentVersion;
    /** @deprecated use {@link #version} instead, this will be removed before 1.0 */
    @Deprecated
    public final ManagementVersion serverVersion;

    OfflineCommandContext(OfflineManagementClient client, ServerVersion version) {
        this.client = client;
        this.options = client.options();
        this.version = version;
        this.configurationFile = client.options().configurationFile;

        ManagementVersion managementVersion = null;
        if (version != null) {
            try {
                managementVersion = ManagementVersion.from(version);
            } catch (IllegalArgumentException ignored) {
                // unknown server version, better to keep the deprecated fields set to "null" than fail
            }
        }

        this.currentVersion = managementVersion;
        this.serverVersion = managementVersion;
    }
}
