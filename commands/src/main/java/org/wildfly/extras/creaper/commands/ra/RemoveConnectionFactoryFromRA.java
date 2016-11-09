package org.wildfly.extras.creaper.commands.ra;

import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;

/**
 * Removes existing connection factory from resource adapter
 */
public final class RemoveConnectionFactoryFromRA implements OnlineCommand {
    private final String poolName;
    private final String resourceAdapterId;

    private RemoveConnectionFactoryFromRA(Builder builder) {
        this.poolName = builder.poolName;
        this.resourceAdapterId = builder.resourceAdapterId;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address address = Address.subsystem("resource-adapters").and("resource-adapter", resourceAdapterId)
                .and("connection-definitions", poolName);
        ops.remove(address);
    }


    public static final class Builder {
        private final String poolName;
        private final String resourceAdapterId;

        /**
         * @param poolName unique id of connection factory
         * @param resourceAdapterId name of resource adapter
         */
        public Builder(String poolName, String resourceAdapterId) {
            this.poolName = poolName;
            this.resourceAdapterId = resourceAdapterId;
        }

        public RemoveConnectionFactoryFromRA build() {
            return new RemoveConnectionFactoryFromRA(this);
        }
    }

    @Override
    public String toString() {
        return "RemoveConnectionFactoryFromRA " + poolName;
    }
}
