package org.wildfly.extras.creaper.core.online.operations.admin;

import org.wildfly.extras.creaper.core.online.Constants;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;

enum CommonRestartOperation implements RestartOperation {
    RELOAD {
        @Override
        public boolean isRequired(ModelNodeResult serverStateResult, boolean isManagedServerInDomain) {
            if (isManagedServerInDomain) {
                // reloading an individual server in managed domain is not supported on AS 7, so when trying to figure
                // out if reload of host in domain is required, some servers on that host might actually signal
                // "restart required" (even if it would be "reload required" in the same situation in standalone);
                // this is also true even on WildFly, which supports reloading a server in domain (see WFLY-351)
                //
                // anyway, reloading a host means restarting all its servers, so returning true is fine in this case
                if (Constants.CONTROLLER_PROCESS_STATE_RESTART_REQUIRED.equals(serverStateResult.stringValue())) {
                    return true;
                }
            }

            return Constants.CONTROLLER_PROCESS_STATE_RELOAD_REQUIRED.equals(serverStateResult.stringValue());
        }

        @Override
        public ModelNodeResult perform(Operations ops, Address address) throws IOException {
            return ops.invoke(Constants.RELOAD, address);
        }
    },
    RESTART {
        @Override
        public boolean isRequired(ModelNodeResult serverStateResult, boolean isManagedServerInDomain) {
            serverStateResult.assertDefinedValue();
            return Constants.CONTROLLER_PROCESS_STATE_RESTART_REQUIRED.equals(serverStateResult.stringValue());
        }

        @Override
        public ModelNodeResult perform(Operations ops, Address address) throws IOException {
            return ops.invoke(Constants.SHUTDOWN, address, Values.of(Constants.RESTART, true));
        }
    };

    abstract boolean isRequired(ModelNodeResult serverStateResult, boolean isManagedServerInDomain);
}
