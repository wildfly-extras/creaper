package org.wildfly.extras.creaper.core.online.operations.admin;

import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * <p>A convenience for commonly performed server administration management operations. This is very similar
 * to the {@link org.wildfly.extras.creaper.core.online.operations.Operations} class, but it's focused on server
 * administration.</p>
 *
 * <p>This class contains operations that can be performed both on a standalone server and on a managed domain.
 * When the operation is performed against a domain controller, the {@link OnlineManagementClient#options()} are
 * consulted to figure out against which host the operation should be performed.</p>
 *
 * <p>Some operations that can only be performed on a managed domain are available in the {@link DomainAdministration}
 * class, which is a subclass of this class.</p>
 *
 * <p>This class contains an {@link OnlineManagementClient}, but is otherwise stateless. Most importantly, this class
 * <b>doesn't</b> close the underlying {@code OnlineManagementClient}. This means that as long as that
 * {@code OnlineManagementClient} is valid, this class is usable.</p>
 */
public class Administration {
    static final int DEFAULT_TIMEOUT = 60; // seconds

    private final OnlineManagementClient client;
    private final AdministrationOperations ops;

    public Administration(OnlineManagementClient client) {
        this(client, DEFAULT_TIMEOUT);
    }

    public Administration(OnlineManagementClient client, int timeoutInSeconds) {
        this.client = client;
        if (client.options().isDomain) {
            this.ops = new DomainAdministrationOperations(client, timeoutInSeconds);
        } else {
            this.ops = new StandaloneAdministrationOperations(client, timeoutInSeconds);
        }
    }

    // ---

    /**
     * Returns whether reload is required. In domain, returns {@code true} if at least one of the servers
     * requires reload.
     */
    public final boolean isReloadRequired() {
        try {
            return ops.isReloadRequired();
        } catch (IOException e) {
            throw sneakyThrow(e);
        }
    }

    /** Reloads the server. In domain, reloads the entire host, which includes restarting all its servers. */
    public final void reload() throws IOException, InterruptedException, TimeoutException {
        ops.reload();
    }

    /**
     * Reloads the server if required. In domain, reloads the entire host if at least one server requires reload.
     * Reloading the host includes restarting all its servers.
     * @return if the server was in fact reloaded; in domain, if the host was reloaded
     */
    public final boolean reloadIfRequired() throws IOException, InterruptedException, TimeoutException {
        return ops.reloadIfRequired();
    }

    // ---

    /**
     * Returns whether restart is required. In domain, returns {@code true} if at least one of the servers
     * requires restart.
     */
    public final boolean isRestartRequired() {
        try {
            return ops.isRestartRequired();
        } catch (IOException e) {
            throw sneakyThrow(e);
        }
    }

    /** Restarts the server. In domain, restarts the entire host. */
    public final void restart() throws IOException, InterruptedException, TimeoutException {
        ops.restart();
    }

    /**
     * Restarts the server if required. In domain, restarts the entire host if at least one server requires restart.
     * @return if the server was in fact restarted; in domain, if the host was restarted
     */
    public final boolean restartIfRequired() throws IOException, InterruptedException, TimeoutException {
        return ops.restartIfRequired();
    }

    // ---

    /** Shuts down the server. In domain, shuts down the entire host. */
    public final void shutdown() throws IOException {
        ops.shutdown(0);
    }

    /**
     * Shuts down the server gracefully. That is, the server will wait up to {@code timeoutInSeconds} for all active
     * requests to finish. In domain, all servers on the host are shut down gracefully and then the host itself
     * is shut down (there's no such thing as graceful shutdown of a host controller).
     *
     * @param timeoutInSeconds if {@code == 0}, then the server will shutdown immediately without waiting
     * for the active requests to finish; if {@code <= 0}, then the server will wait indefinitely for the active
     * requests to finish
     */
    public final void shutdownGracefully(int timeoutInSeconds) throws IOException {
        client.version().assertAtLeast(ServerVersion.VERSION_3_0_0, "Graceful shutdown is only supported since WildFly 9");
        ops.shutdown(timeoutInSeconds);
    }

    // ---

    /**
     * Waits until the server is {@code running}. In domain, only waits for the host controller,
     * not for individual servers.
     */
    public final void waitUntilRunning() {
        try {
            ops.waitUntilRunning();
        } catch (InterruptedException e) {
            throw sneakyThrow(e);
        } catch (IOException e) {
            throw sneakyThrow(e);
        } catch (TimeoutException e) {
            throw sneakyThrow(e);
        }
    }

    // ---

    // this remained after a piece of debugging code and it's only maintained because some of the methods above
    // forgot to declare checked exceptions properly
    //
    // this should be resolved properly in next major version

    private static RuntimeException sneakyThrow(Throwable t) {
        Administration.<RuntimeException>sneakyThrow0(t);
        return null; // dead code
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrow0(Throwable t) throws T {
        throw (T) t;
    }
}
