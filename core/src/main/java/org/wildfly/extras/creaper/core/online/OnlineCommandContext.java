package org.wildfly.extras.creaper.core.online;

import org.wildfly.extras.creaper.core.ManagementVersion;
import org.wildfly.extras.creaper.core.ServerVersion;

public final class OnlineCommandContext {
    public final OnlineManagementClient client;
    public final OnlineOptions options; // same as client.options()
    public final ServerVersion version; // same as client.version()

    /** @deprecated use {@link #version} instead, this will be removed before 1.0 */
    @Deprecated
    public final ManagementVersion currentVersion;
    /** @deprecated use {@link #version} instead, this will be removed before 1.0 */
    @Deprecated
    public final ManagementVersion serverVersion;

    OnlineCommandContext(OnlineManagementClient client, ServerVersion version) {
        this.client = client;
        this.options = client.options();
        this.version = version;

        ManagementVersion managementVersion = null;
        try {
            managementVersion = ManagementVersion.from(version);
        } catch (IllegalArgumentException ignored) {
            // unknown server version, better to keep the deprecated fields set to "null" than fail
        }

        this.currentVersion = managementVersion;
        this.serverVersion = managementVersion;
    }
}
