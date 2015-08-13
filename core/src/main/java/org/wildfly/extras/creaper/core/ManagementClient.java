package org.wildfly.extras.creaper.core;

import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;

import java.io.IOException;

/**
 * Entrypoint of the entire Creaper library. Serves as a factory of both
 * {@link org.wildfly.extras.creaper.core.online.OnlineManagementClient online} and
 * {@link org.wildfly.extras.creaper.core.offline.OfflineManagementClient offline} management clients.
 * Note that the <b>entire</b> Creaper library is meant for <b>single-threaded</b> use and <b>no</b> thread-safety
 * guarantees are made!
 */
public final class ManagementClient {
    private ManagementClient() {} // avoid instantiation

    /**
     * Creates an eagerly-initialized {@link org.wildfly.extras.creaper.core.online.OnlineManagementClient}. That is,
     * when this method is called, the application server must already be running.
     * @param options connection options (host, port etc.)
     * @throws IOException if the {@code host} cannot be found or an I/O error occurs during initial discovery
     * (e.g. there's no server listening on host:port)
     * @throws IllegalStateException if the client is connected to a domain controller even if the {@code options}
     * say that it should be a standalone server, or if the client is connected to a standalone server even if
     * the {@code options} say that it should be a domain controller
     */
    public static OnlineManagementClient online(OnlineOptions options) throws IOException {
        return OnlineClientFactory.instance.create(options);
    }

    /**
     * Creates a lazily-initialized {@link org.wildfly.extras.creaper.core.online.OnlineManagementClient}. That is,
     * when this method is called, the application server doesn't have to be running, it only has to be running
     * when the first method on the {@code OnlineManagementClient} is called. Note that any exceptions while connecting
     * to the application server will also be thrown lazily.
     * @param options connection options (host, port etc.)
     */
    public static OnlineManagementClient onlineLazy(OnlineOptions options) {
        return OnlineClientFactory.instance.createLazy(options);
    }

    /**
     * Creates a {@link org.wildfly.extras.creaper.core.offline.OfflineManagementClient}.
     * @param options connection options (root directory of the application server etc.)
     * @throws IOException if the specified configuration file doesn't exist or an I/O error occurs during initial
     * discovery
     */
    public static OfflineManagementClient offline(OfflineOptions options) throws IOException {
        return OfflineClientFactory.instance.create(options);
    }

    // ---
    // http://wiki.apidesign.org/wiki/APIDesignPatterns:FriendPackages

    /** Ignore, this is not a part of public API. */
    public abstract static class OnlineClientFactory {
        private static OnlineClientFactory instance;

        public static void set(OnlineClientFactory factory) {
            OnlineClientFactory.instance = factory;
        }

        protected abstract OnlineManagementClient create(OnlineOptions options) throws IOException;

        protected abstract OnlineManagementClient createLazy(OnlineOptions options);
    }

    /** Ignore, this is not a part of public API. */
    public abstract static class OfflineClientFactory {
        private static OfflineClientFactory instance;

        public static void set(OfflineClientFactory factory) {
            OfflineClientFactory.instance = factory;
        }

        protected abstract OfflineManagementClient create(OfflineOptions options) throws IOException;
    }
}
