package org.wildfly.extras.creaper.core.online;

import org.wildfly.extras.creaper.core.ServerVersion;

public final class OnlineCommandContext {
    public final OnlineManagementClient client;
    public final OnlineOptions options; // same as client.options()
    public final ServerVersion version; // same as client.version()

    OnlineCommandContext(OnlineManagementClient client, ServerVersion version) {
        this.client = client;
        this.options = client.options();
        this.version = version;
    }
}
