package org.wildfly.extras.creaper.core.online.operations.admin;

import org.jboss.logging.Logger;
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
    private static final Logger log = Logger.getLogger(OnlineManagementClient.class);

    static final int DEFAULT_TIMEOUT = 60; // seconds

    private final AdministrationOperations ops;

    public Administration(OnlineManagementClient client) {
        this(client, DEFAULT_TIMEOUT);
    }

    public Administration(OnlineManagementClient client, int timeoutInSeconds) {
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
        } catch (Exception e) {
            log.error("Unexpected exception during 'isReloadRequired'", e);
            throw sneakyThrow(e);
        }
    }

    /** Reloads the server. In domain, reloads the entire host. */
    public final void reload() throws IOException, InterruptedException, TimeoutException {
        try {
            ops.reload();
        } catch (Exception e) {
            log.error("Unexpected exception during 'reload'", e);
            throw sneakyThrow(e);
        }
    }

    /**
     * Reloads the server if required. In domain, reloads the entire host if at least one server requires reload.
     * @return if the server was in fact reloaded; in domain, if the host was reloaded
     */
    public final boolean reloadIfRequired() throws IOException, InterruptedException, TimeoutException {
        try {
            return ops.reloadIfRequired();
        } catch (Exception e) {
            log.error("Unexpected exception during 'reloadIfRequired'", e);
            throw sneakyThrow(e);
        }
    }

    // ---

    /**
     * Returns whether restart is required. In domain, returns {@code true} if at least one of the servers
     * requires restart.
     */
    public final boolean isRestartRequired() {
        try {
            return ops.isRestartRequired();
        } catch (Exception e) {
            log.error("Unexpected exception during 'isRestartRequired", e);
            throw sneakyThrow(e);
        }
    }

    /** Restarts the server. In domain, restarts the entire host. */
    public final void restart() throws IOException, InterruptedException, TimeoutException {
        try {
            ops.restart();
        } catch (Exception e) {
            log.error("Unexpected exception during 'restart'", e);
            throw sneakyThrow(e);
        }
    }

    /**
     * Restarts the server if required. In domain, restarts the entire host if at least one server requires restart.
     * @return if the server was in fact restarted; in domain, if the host was restarted
     */
    public final boolean restartIfRequired() throws IOException, InterruptedException, TimeoutException {
        try {
            return ops.restartIfRequired();
        } catch (Exception e) {
            log.error("Unexpected exception during 'restartIfRequired'", e);
            throw sneakyThrow(e);
        }
    }

    // ---

    /**
     * Waits until the server is {@code running}. In domain, only waits for the host controller,
     * not for individual servers.
     */
    public final void waitUntilRunning() {
        try {
            ops.waitUntilRunning();
        } catch (Exception e) {
            log.error("Unexpected exception during 'waitUntilRunning'", e);
            throw sneakyThrow(e);
        }
    }

    // ---

    // this is only for debugging, as there is apparently a situation in which reload (and possibly
    // all the other methods) throws an unexpected exception that can be suppressed by another exception
    // thrown from a finally block
    //
    // once this problem is resolved, the debugging code here should be reconsidered

    private static RuntimeException sneakyThrow(Throwable t) {
        Administration.<RuntimeException>sneakyThrow0(t);
        return null; // dead code
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrow0(Throwable t) throws T {
        throw (T) t;
    }
}
