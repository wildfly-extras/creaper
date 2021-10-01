package org.wildfly.extras.creaper.core.online.operations.admin;

import org.wildfly.extras.creaper.core.online.Constants;

final class ServerState {
    private ServerState() {
        // avoid instantiation
    }

    static boolean isRunning(String serverState) {
        return Constants.CONTROLLER_PROCESS_STATE_RUNNING.equals(serverState)
                || Constants.CONTROLLER_PROCESS_STATE_RELOAD_REQUIRED.equals(serverState)
                || Constants.CONTROLLER_PROCESS_STATE_RESTART_REQUIRED.equals(serverState);
    }
}
