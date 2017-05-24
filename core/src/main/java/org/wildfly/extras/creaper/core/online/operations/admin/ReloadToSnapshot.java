package org.wildfly.extras.creaper.core.online.operations.admin;

import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.Constants;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * <p>Utility for reloading the server to a snapshot. This requires WildFly Core 3.</p>
 */
public final class ReloadToSnapshot {
    private final OnlineManagementClient client;
    private final String snapshot;
    private final int timeoutInSeconds;

    public ReloadToSnapshot(OnlineManagementClient client, String snapshot) throws IOException {
        this(client, snapshot, Administration.DEFAULT_TIMEOUT);
    }

    public ReloadToSnapshot(OnlineManagementClient client, String snapshot, int timeoutInSeconds) throws IOException {
        if (client.version().lessThan(ServerVersion.VERSION_5_0_0)) {
            throw new IllegalStateException("ReloadToSnapshot requires at least WildFly Core 3.");
        }

        this.client = client;
        this.snapshot = snapshot;
        this.timeoutInSeconds = timeoutInSeconds;
    }

    /**
     * Reload the server to a snapshot. In managed domain, reloads the default host to a domain.xml snapshot.
     */
    public void perform() throws InterruptedException, TimeoutException, IOException {
        if (client.options().isStandalone) {
            new StandaloneAdministrationOperations(client, timeoutInSeconds)
                    .performRestartOperation(new ReloadToStandaloneSnapshotRestartOperation(snapshot));
        } else {
            perform(client.options().defaultHost);
        }
    }

    /**
     * Reload given {@code host} to a domain.xml snapshot. This method only makes sense in managed domain.
     */
    public void perform(String host) throws InterruptedException, TimeoutException, IOException {
        if (!client.options().isDomain) {
            throw new IllegalStateException("Asked to reload host '" + host
                    + "' to a snapshot, but the server isn't a domain controller");
        }

        new DomainAdministrationOperations(client, timeoutInSeconds)
                .performRestartOperation(host, new ReloadToDomainSnapshotRestartOperation(snapshot));
    }

    private static final class ReloadToStandaloneSnapshotRestartOperation implements RestartOperation {
        private final String snapshot;

        ReloadToStandaloneSnapshotRestartOperation(String snapshot) {
            this.snapshot = snapshot;
        }

        @Override
        public ModelNodeResult perform(Operations ops, Address address) throws IOException {
            return ops.invoke(Constants.RELOAD, address, Values.of(Constants.SERVER_CONFIG, snapshot));
        }
    }

    private static final class ReloadToDomainSnapshotRestartOperation implements RestartOperation {
        private final String snapshot;

        ReloadToDomainSnapshotRestartOperation(String snapshot) {
            this.snapshot = snapshot;
        }

        @Override
        public ModelNodeResult perform(Operations ops, Address address) throws IOException {
            return ops.invoke(Constants.RELOAD, address, Values.of(Constants.DOMAIN_CONFIG, snapshot));
        }
    }
}
