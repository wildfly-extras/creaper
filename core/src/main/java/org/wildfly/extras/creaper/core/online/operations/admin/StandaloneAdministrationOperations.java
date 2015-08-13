package org.wildfly.extras.creaper.core.online.operations.admin;

import org.wildfly.extras.creaper.core.online.Constants;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class StandaloneAdministrationOperations implements AdministrationOperations {
    private final OnlineManagementClient client;
    private final Operations ops;
    private final int timeoutInSeconds;

    public StandaloneAdministrationOperations(OnlineManagementClient client, int timeoutInSeconds) {
        this.client = client;
        this.ops = new Operations(client);
        this.timeoutInSeconds = timeoutInSeconds;
    }

    public boolean isReloadRequired() throws IOException {
        return isRestartOperationRequired(RestartOperation.RELOAD);
    }

    public void reload() throws IOException, InterruptedException, TimeoutException {
        performRestartOperation(RestartOperation.RELOAD);
    }

    public boolean reloadIfRequired() throws IOException, InterruptedException, TimeoutException {
        if (isRestartOperationRequired(RestartOperation.RELOAD)) {
            reload();
            return true;
        }
        return false;
    }

    @Override
    public boolean isRestartRequired() throws IOException {
        return isRestartOperationRequired(RestartOperation.RESTART);
    }

    public void restart() throws IOException, InterruptedException, TimeoutException {
        performRestartOperation(RestartOperation.RESTART);
    }

    public boolean restartIfRequired() throws IOException, InterruptedException, TimeoutException {
        if (isRestartOperationRequired(RestartOperation.RESTART)) {
            restart();
            return true;
        }
        return false;
    }

    @Override
    public void waitUntilRunning() throws InterruptedException, TimeoutException, IOException {
        waitUntilServerIsRunning(true);
    }

    private void performRestartOperation(RestartOperation restartOperation) throws IOException, InterruptedException,
            TimeoutException {
        boolean needsToReconnect = false;
        try {
            restartOperation.perform(ops, Address.root());
        } catch (Throwable e) {
            // server went down before we received the response, this can happen
            needsToReconnect = true;
        }

        waitUntilServerIsRunning(needsToReconnect);
    }

    private boolean isRestartOperationRequired(RestartOperation restartOperation) throws IOException {
        return restartOperation.isRequired(ops.readAttribute(Address.root(), Constants.SERVER_STATE), false);
    }

    private void waitUntilServerIsRunning(boolean reconnect) throws IOException, InterruptedException,
            TimeoutException {

        Thread.sleep(500); // this value is taken from implementation of CLI "reload"

        if (reconnect) {
            client.reconnect(timeoutInSeconds);
        }

        long endTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeoutInSeconds);
        while (System.currentTimeMillis() < endTime) {
            try {
                if (isServerRunning()) {
                    break;
                }
            } catch (Throwable ignored) {
                // server is probably down, will retry
            }

            Thread.sleep(200); // this value is completely arbitrary
        }

        boolean running = false;
        try {
            running = isServerRunning();
        } catch (Throwable ignored) {
            // server probably down
        }
        if (!running) {
            throw new TimeoutException("Waiting for server timed out");
        }
    }

    private boolean isServerRunning() throws IOException {
        ModelNodeResult result = ops.readAttribute(Address.root(), Constants.SERVER_STATE);
        result.assertDefinedValue();
        return Constants.CONTROLLER_PROCESS_STATE_RUNNING.equals(result.stringValue());
    }
}
