package org.wildfly.extras.creaper.commands.ra;

import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;

/**
 * Remove existing resource adapter
 */
public final class RemoveResourceAdapter implements OnlineCommand {
    private final String resourceAdapterId;

    private RemoveResourceAdapter(Builder builder) {
        this.resourceAdapterId = builder.resourceAdapterId;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address address = Address.subsystem("resource-adapters").and("resource-adapter", resourceAdapterId);
        ops.remove(address);
    }

    @Override
    public String toString() {
        return "RemoveResourceAdapter " + resourceAdapterId;
    }

    public static final class Builder {
        private final String resourceAdapterId;

        /**
         * @param resourceAdapterId name of resource adapter
         */
        public Builder(String resourceAdapterId) {
            if (resourceAdapterId == null) {
                throw new IllegalArgumentException("resourceAdapterId must be specified");
            }
            this.resourceAdapterId = resourceAdapterId;
        }

        public RemoveResourceAdapter build() {
            return new RemoveResourceAdapter(this);
        }
    }
}
