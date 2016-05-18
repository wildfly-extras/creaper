package org.wildfly.extras.creaper.commands.auditlog;

import java.io.IOException;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

/**
 * Command for adding {@code logger} child into {@code audit-log} node.
 * Please note that there can be at most 1 {@code logger} currently so you likely
 * want to use {@code replaceExisting} attribute.
 */
public final class AddAuditLogger implements OnlineCommand, OfflineCommand {

    private final Boolean enabled;
    private final Boolean logBoot;
    private final Boolean logReadOnly;
    private final boolean replaceExisting;

    public AddAuditLogger(Builder builder) {
        this.enabled = builder.enabled;
        this.logBoot = builder.logBoot;
        this.logReadOnly = builder.logReadOnly;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address loggerAddress = Address.coreService("management")
                .and("access", "audit")
                .and("logger", "audit-log");

        if (replaceExisting) {
            try {
                ops.removeIfExists(loggerAddress);
            } catch (OperationException e) {
                throw new IOException("Failed to remove existing logger", e);
            }
        }
        ops.add(loggerAddress, Values.empty()
                .andOptional("enabled", enabled)
                .andOptional("log-boot", logBoot)
                .andOptional("log-read-only", logReadOnly)
        );
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        ctx.client.apply(GroovyXmlTransform.of(AddAuditLogger.class)
                .subtree("management", Subtree.management())
                .parameter("atrEnabled", enabled == null ? null : String.valueOf(enabled))
                .parameter("atrLogBoot", logBoot == null ? null : String.valueOf(logBoot))
                .parameter("atrLogReadOnly", logReadOnly == null ? null : String.valueOf(logReadOnly))
                .parameter("atrReplaceExisting", replaceExisting)
                .build());
    }

    public static final class Builder {

        private boolean replaceExisting;
        private Boolean enabled;
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

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddAuditLogger build() {
            return new AddAuditLogger(this);
        }

    }
}
