package org.wildfly.extras.creaper.core.online.operations.admin;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

interface AdministrationOperations {
    boolean isReloadRequired() throws IOException;

    void reload() throws IOException, InterruptedException, TimeoutException;

    boolean reloadIfRequired() throws IOException, InterruptedException, TimeoutException;

    boolean isRestartRequired() throws IOException;

    void restart() throws IOException, InterruptedException, TimeoutException;

    boolean restartIfRequired() throws IOException, InterruptedException, TimeoutException;

    void waitUntilRunning() throws InterruptedException, TimeoutException, IOException;
}
