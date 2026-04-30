package org.wildfly.extras.creaper.commands.elytron.audit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddAggregateSecurityEventListener implements OnlineCommand {

    private final String name;
    private final List<String> securityEventListenerList;
    private final boolean replaceExisting;

    private AddAggregateSecurityEventListener(Builder builder) {
        this.name = builder.name;
        this.securityEventListenerList = builder.securityEventListenerList;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address listenerAddress = Address.subsystem("elytron").and("aggregate-security-event-listener", name);
        if (replaceExisting) {
            ops.removeIfExists(listenerAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(listenerAddress, Values.empty()
                .andList(String.class, "security-event-listeners", securityEventListenerList));
    }

    public static final class Builder {

        private final String name;
        private List<String> securityEventListenerList = new ArrayList<String>();
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the aggregate-security-event-listener must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the aggregate-security-event-listener must not be empty value");
            }

            this.name = name;
        }

        public Builder addSecurityEventListeners(String... securityEventListeners) {
            if (securityEventListeners == null) {
                throw new IllegalArgumentException("Listeners added to aggregate-security-event-listener must not be null");
            }
            Collections.addAll(this.securityEventListenerList, securityEventListeners);
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddAggregateSecurityEventListener build() {
            if (securityEventListenerList == null || securityEventListenerList.size() < 2) {
                throw new IllegalArgumentException("Security-event-listener must not be null and must include at least two entries");
            }
            return new AddAggregateSecurityEventListener(this);
        }
    }
}
