package org.wildfly.extras.creaper.core.online.operations.admin;

import org.wildfly.extras.creaper.core.online.Constants;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Batch;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * <p>A convenience for commonly performed administration operations in managed domain. This is an extension
 * of the {@link Administration} class for domain-only administration operations.</p>
 *
 * <p>All operations have a variant with an explicitly specified host (always as a first parameter) and without a host.
 * The variant without a host is always equivalent to calling the variant with a host, where the host provided is
 * the default host from {@link OnlineManagementClient#options()}.</p>
 *
 * <p>This class contains an {@link OnlineManagementClient}, but is otherwise stateless. Most importantly, this class
 * <b>doesn't</b> close the underlying {@code OnlineManagementClient}. This means that as long as that
 * {@code OnlineManagementClient} is valid, this class is usable.</p>
 */
public final class DomainAdministration extends Administration {
    private final OnlineManagementClient client;
    private final Operations ops;
    private final DomainAdministrationOperations domainOps;

    public DomainAdministration(OnlineManagementClient client) {
        this(client, DEFAULT_TIMEOUT);
    }

    public DomainAdministration(OnlineManagementClient client, int timeoutInSeconds) {
        super(client);
        this.client = client;
        this.ops = new Operations(client);
        this.domainOps = new DomainAdministrationOperations(client, timeoutInSeconds);
    }

    // ---

    /** Retrieves all server groups in domain. */
    public List<String> serverGroups() throws IOException {
        ModelNodeResult result = ops.readChildrenNames(Address.root(), Constants.SERVER_GROUP);
        result.assertDefinedValue();
        return result.stringListValue();
    }

    /** Retrieves all hosts in domain. */
    public List<String> hosts() throws IOException {
        ModelNodeResult result = ops.readChildrenNames(Address.root(), Constants.HOST);
        result.assertDefinedValue();
        return result.stringListValue();
    }

    /** @see #allRunningServers(String) */
    public List<String> allRunningServers() throws IOException {
        return allRunningServers(client.options().defaultHost);
    }

    /** Retrieves all servers running on given {@code host}. */
    public List<String> allRunningServers(String host) throws IOException {
        return domainOps.allRunningServers(host);
    }

    /** @see #allServers(String) */
    public List<String> allServers() throws IOException {
        return allServers(client.options().defaultHost);
    }

    /** Retrieves all configured servers on given {@code host}. */
    public List<String> allServers(String host) throws IOException {
        ModelNodeResult result = ops.readChildrenNames(Address.host(host), Constants.SERVER_CONFIG);
        result.assertDefinedValue();
        return result.stringListValue();
    }

    // ---

    /** @see #startServer(String, String) */
    public boolean startServer(String server) throws IOException {
        return startServer(client.options().defaultHost, server);
    }

    /**
     * Starts given {@code server} on given {@code host}.
     *
     * @return {@code true} when server was successfully started, {@code false} otherwise
     */
    public boolean startServer(String host, String server) throws IOException {
        ModelNodeResult result = ops.invoke("start", Address.host(host).and(Constants.SERVER_CONFIG, server),
                Values.of("blocking", true));
        result.assertDefinedValue();
        return result.isSuccess();
    }

    // ---

    /**
     * Returns whether the {@code host} requires reload (that is, at least one of the servers on the host
     * requires reload). This is a variant of {@link Administration#isReloadRequired()}.
     */
    public boolean isReloadRequired(String host) throws IOException {
        return domainOps.isRestartOperationRequired(host, RestartOperation.RELOAD);
    }

    /** Reloads given {@code host}. This is a variant of {@link Administration#reload()}. */
    public void reload(String host) throws IOException, InterruptedException, TimeoutException {
        domainOps.performRestartOperation(host, RestartOperation.RELOAD);
    }

    /** Reloads given {@code host} if required. This is a variant of {@link Administration#reloadIfRequired()}. */
    public boolean reloadIfRequired(String host) throws IOException, InterruptedException, TimeoutException {
        if (domainOps.isRestartOperationRequired(host, RestartOperation.RELOAD)) {
            reload(host);
            return true;
        }
        return false;
    }

    // ---

    /**
     * Returns whether the {@code host} requires restart (that is, at least one of the servers on the host
     * requires restart). This is a variant of {@link Administration#isRestartRequired()}.
     */
    public boolean isRestartRequired(String host) throws IOException {
        return domainOps.isRestartOperationRequired(host, RestartOperation.RESTART);
    }

    /** Restarts given {@code host}. This is a variant of {@link Administration#restart()}. */
    public void restart(String host) throws IOException, InterruptedException, TimeoutException {
        domainOps.performRestartOperation(host, RestartOperation.RESTART);
    }

    /** Restarts given {@code host} if required. This is a variant of {@link Administration#restartIfRequired()}. */
    public boolean restartIfRequired(String host) throws IOException, InterruptedException, TimeoutException {
        if (domainOps.isRestartOperationRequired(host, RestartOperation.RESTART)) {
            restart(host);
            return true;
        }
        return false;
    }

    /** @see #restartAllServers(String) */
    public void restartAllServers() throws InterruptedException, IOException, TimeoutException {
        restartAllServers(client.options().defaultHost);
    }

    /**
     * Restarts all the servers on given {@code host}. As opposed to {@link Administration#restart()}, this doesn't
     * restart the entire host, just the servers.
     */
    public void restartAllServers(String host) throws InterruptedException, TimeoutException, IOException {
        restartServers(host, allRunningServers(host));
    }

    /** @see #restartServer(String, String) */
    public void restartServer(String server) throws InterruptedException, TimeoutException, IOException {
        restartServer(client.options().defaultHost, server);
    }

    /** Restarts given {@code server} on given {@code host}. */
    public void restartServer(String host, String server) throws InterruptedException, TimeoutException, IOException {
        restartServers(host, Collections.singletonList(server));
    }

    void restartServers(String host, List<String> servers) throws IOException, InterruptedException, TimeoutException {
        Batch batch = new Batch();
        for (String server : servers) {
            batch.invoke(Constants.RESTART, Address.host(host).and(Constants.SERVER_CONFIG, server));
        }

        boolean needsToReconnect = false;
        try {
            ops.batch(batch);
        } catch (Throwable e) {
            // server went down before we received the response, this can happen
            needsToReconnect = true;
        }

        domainOps.waitUntilServersAreRunning(host, servers, needsToReconnect);
    }

    // ---

    /**
     * Waits until the the host controller of given {@code host} is running.
     * This is a variant of {@link Administration#waitUntilRunning()}.
     */
    public void waitUntilRunning(String host) throws InterruptedException, TimeoutException, IOException {
        domainOps.waitUntilServersAreRunning(host, null, true);
    }

    /** @see #waitUntilServersRunning(String, java.util.List)  */
    public void waitUntilServersRunning(List<String> servers) throws InterruptedException, TimeoutException,
            IOException {
        domainOps.waitUntilServersAreRunning(client.options().defaultHost, servers, true);
    }

    /** Waits until all the {@code servers} on given {@code host} are running. */
    public void waitUntilServersRunning(String host, List<String> servers) throws InterruptedException,
            TimeoutException, IOException {
        domainOps.waitUntilServersAreRunning(host, servers, true);
    }

    // ---

    /** @see #stopServer(String, String) */
    public boolean stopServer(String server) throws IOException {
        return stopServer(client.options().defaultHost, server);
    }

    /**
     * Stops given {@code server} on given {@code host}.
     *
     * @return {@code true} when server was successfully stopped, {@code false} otherwise
     */
    public boolean stopServer(String host, String server) throws IOException {
        ModelNodeResult result = ops.invoke("stop", Address.host(host).and(Constants.SERVER_CONFIG, server),
                Values.of("blocking", true));
        result.assertDefinedValue();
        return result.isSuccess();
    }

    // ---

    /** @see #removeServer(String, String) */
    public boolean removeServer(String server) throws IOException, OperationException {
        return removeServer(client.options().defaultHost, server);
    }

    /**
     * Removes given {@code server} from given {@code host}
     *
     * @return {@code true} if the server was actually removed, {@code false} otherwise
     */
    public boolean removeServer(String host, String server) throws IOException, OperationException {
        return ops.removeIfExists(Address.host(host).and(Constants.SERVER_CONFIG, server));
    }
}
