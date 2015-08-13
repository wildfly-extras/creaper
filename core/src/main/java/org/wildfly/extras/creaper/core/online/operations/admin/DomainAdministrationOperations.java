package org.wildfly.extras.creaper.core.online.operations.admin;

import org.wildfly.extras.creaper.core.online.Constants;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Batch;
import org.wildfly.extras.creaper.core.online.operations.Operations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class DomainAdministrationOperations implements AdministrationOperations {
    private final OnlineManagementClient client;
    private final Operations ops;
    private final int timeoutInSeconds;

    public DomainAdministrationOperations(OnlineManagementClient client, int timeoutInSeconds) {
        this.client = client;
        this.ops = new Operations(client);
        this.timeoutInSeconds = timeoutInSeconds;
    }

    @Override
    public boolean isReloadRequired() throws IOException {
        return isRestartOperationRequired(client.options().defaultHost, RestartOperation.RELOAD);
    }

    @Override
    public void reload() throws IOException, InterruptedException, TimeoutException {
        performRestartOperation(client.options().defaultHost, RestartOperation.RELOAD);
    }

    @Override
    public boolean reloadIfRequired() throws IOException, InterruptedException, TimeoutException {
        if (isRestartOperationRequired(client.options().defaultHost, RestartOperation.RELOAD)) {
            reload();
            return true;
        }
        return false;
    }

    @Override
    public boolean isRestartRequired() throws IOException {
        return isRestartOperationRequired(client.options().defaultHost, RestartOperation.RESTART);
    }

    @Override
    public void restart() throws IOException, InterruptedException, TimeoutException {
        performRestartOperation(client.options().defaultHost, RestartOperation.RESTART);
    }

    @Override
    public boolean restartIfRequired() throws IOException, InterruptedException, TimeoutException {
        if (isRestartOperationRequired(client.options().defaultHost, RestartOperation.RESTART)) {
            restart();
            return true;
        }
        return false;
    }

    @Override
    public void waitUntilRunning() throws InterruptedException, TimeoutException, IOException {
        waitUntilServersAreRunning(client.options().defaultHost, null, true);
    }

    void performRestartOperation(String host, RestartOperation restartOperation) throws IOException, TimeoutException,
            InterruptedException {
        List<String> allServers = allRunningServers(host);

        boolean needsToReconnect = false;
        try {
            restartOperation.perform(ops, Address.host(host));
        } catch (Throwable e) {
            // server went down before we received the response, this can happen
            needsToReconnect = true;
        }

        waitUntilServersAreRunning(host, allServers, needsToReconnect);
    }

    boolean isRestartOperationRequired(String host, RestartOperation restartOperation) throws IOException {
        List<String> allServers = allRunningServers(host);

        Batch batch = new Batch();
        batch.readAttribute(Address.host(host), Constants.HOST_STATE);
        for (String server : allServers) {
            batch.readAttribute(Address.host(host).and(Constants.SERVER, server), Constants.SERVER_STATE);
        }

        ModelNodeResult result = ops.batch(batch);
        result.assertDefinedValue();
        int counter = 0; // 0 == host, > 0 == server
        for (ModelNodeResult stepResult : result.forAllBatchSteps()) {
            if (restartOperation.isRequired(stepResult, counter > 0)) {
                return true;
            }
            counter++;
        }

        return false;
    }

    List<String> allRunningServers(String host) throws IOException {
        ModelNodeResult result = ops.readChildrenNames(Address.host(host), Constants.SERVER);
        result.assertDefinedValue();
        List<String> servers = result.stringListValue();

        List<String> startedServers = new ArrayList<String>();
        for (String server : servers) {
            ModelNodeResult serverStatus = ops.readAttribute(Address.host(host).and(Constants.SERVER_CONFIG, server),
                    Constants.STATUS);
            serverStatus.assertDefinedValue();
            if ("STARTED".equals(serverStatus.stringValue())) {
                startedServers.add(server);
            }
        }

        return startedServers;
    }

    /**
     * If {@code servers} is {@code null} or empty, only waits for the host controller to be running.
     * Otherwise, waits for all servers on given {@code host} to be running.
     */
    void waitUntilServersAreRunning(String host, List<String> servers, boolean reconnect) throws IOException,
            InterruptedException, TimeoutException {

        Thread.sleep(500); // this value is taken from implementation of CLI "reload"

        if (reconnect) {
            client.reconnect(timeoutInSeconds);
        }

        long endTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeoutInSeconds);
        while (System.currentTimeMillis() < endTime) {
            try {
                if (areServersRunning(host, servers)) {
                    break;
                }
            } catch (Throwable ignored) {
                // server is probably down, will retry
            }

            Thread.sleep(200); // this value is completely arbitrary
        }

        boolean running = false;
        try {
            running = areServersRunning(host, servers);
        } catch (Throwable ignored) {
            // server probably down
        }
        if (!running) {
            throw new TimeoutException("Waiting for host '" + host + "' / servers " + servers + " timed out");
        }
    }

    private boolean areServersRunning(String host, List<String> servers) throws IOException {
        Address hostAddress = Address.host(host);

        if (servers == null || servers.isEmpty()) {
            ModelNodeResult result = ops.readAttribute(Address.host(host), Constants.HOST_STATE);
            result.assertDefinedValue();
            return Constants.CONTROLLER_PROCESS_STATE_RUNNING.equals(result.stringValue());
        } else {
            for (String server : servers) {
                Address serverAddress = hostAddress.and(Constants.SERVER, server);
                ModelNodeResult result = ops.readAttribute(serverAddress, Constants.SERVER_STATE);
                if (!result.hasDefinedValue()
                        || !Constants.CONTROLLER_PROCESS_STATE_RUNNING.equals(result.stringValue())) {
                    return false;
                }
            }
            return true;
        }
    }
}
