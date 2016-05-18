package org.wildfly.extras.creaper.commands.auditlog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Batch;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

public final class ChangeAuditLogger implements OnlineCommand, OfflineCommand {

    private final Boolean enabled;
    private final List<String> addHandlers;
    private final List<String> removeHandlers;
    private final Boolean logBoot;
    private final Boolean logReadOnly;

    public ChangeAuditLogger(Builder builder) {
        this.enabled = builder.enabled;
        this.logBoot = builder.logBoot;
        this.logReadOnly = builder.logReadOnly;
        this.addHandlers = builder.addHandlers;
        this.removeHandlers = builder.removeHandlers;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address loggerAddress = Address.coreService("management")
                .and("access", "audit")
                .and("logger", "audit-log");

        Batch batch = new Batch();
        if (addHandlers != null && !addHandlers.isEmpty()) {
            for (String handler : addHandlers) {
                Address handlerAddress = loggerAddress.and("handler", handler);
                batch.add(handlerAddress, Values.empty());
            }
        }

        if (removeHandlers != null && !removeHandlers.isEmpty()) {
            for (String handler : removeHandlers) {
                Address handlerAddress = loggerAddress.and("handler", handler);
                batch.remove(handlerAddress);
            }
        }

        if (enabled != null) {
            batch.writeAttribute(loggerAddress, "enabled", enabled);
        }
        if (logBoot != null) {
            batch.writeAttribute(loggerAddress, "log-boot", logBoot);
        }
        if (logReadOnly != null) {
            batch.writeAttribute(loggerAddress, "log-read-only", logReadOnly);
        }
        ops.batch(batch);
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        ctx.client.apply(GroovyXmlTransform.of(ChangeAuditLogger.class)
                .subtree("management", Subtree.management())
                .parameter("atrEnabled", enabled == null ? null : String.valueOf(enabled))
                .parameter("atrAddHandlers", addHandlers)
                .parameter("atrRemoveHandlers", removeHandlers)
                .parameter("atrLogBoot", logBoot == null ? null : String.valueOf(logBoot))
                .parameter("atrLogReadOnly", logReadOnly == null ? null : String.valueOf(logReadOnly))
                .build());
    }

    public static final class Builder {

        private Boolean enabled;
        private List<String> addHandlers;
        private List<String> removeHandlers;
        private Boolean logBoot;
        private Boolean logReadOnly;

        public Builder() {
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder logBoot(boolean logBoot) {
            this.logBoot = logBoot;
            return this;
        }

        public Builder logReadOnly(boolean logReadOnly) {
            this.logReadOnly = logReadOnly;
            return this;
        }

        public Builder addHandler(String handler) {
            if (handler == null) {
                throw new IllegalArgumentException("handler can not be null");
            }
            if (this.addHandlers == null) {
                this.addHandlers = new ArrayList<String>();
            }
            this.addHandlers.add(handler);
            return this;
        }

        public Builder addHandlers(String... handlers) {
            if (handlers == null) {
                throw new IllegalArgumentException("handlers can not be null");
            }
            if (this.addHandlers == null) {
                this.addHandlers = new ArrayList<String>();
            }
            this.addHandlers.addAll(Arrays.asList(handlers));
            return this;
        }

        public Builder removeHandler(String handler) {
            if (handler == null) {
                throw new IllegalArgumentException("handler can not be null");
            }
            if (this.removeHandlers == null) {
                this.removeHandlers = new ArrayList<String>();
            }
            this.removeHandlers.add(handler);
            return this;
        }

        public Builder removeHandlers(String... handlers) {
            if (handlers == null) {
                throw new IllegalArgumentException("handlers can not be null");
            }
            if (this.removeHandlers == null) {
                this.removeHandlers = new ArrayList<String>();
            }
            this.removeHandlers.addAll(Arrays.asList(handlers));
            return this;
        }

        public ChangeAuditLogger build() {
            if (addHandlers != null && removeHandlers != null) {
                Set<String> intersection = new HashSet<String>(addHandlers);
                intersection.retainAll(removeHandlers);
                if (!intersection.isEmpty()) {
                    throw new IllegalArgumentException("handler can not be added and removed at the same time");
                }
            }

            return new ChangeAuditLogger(this);

        }

    }
}
