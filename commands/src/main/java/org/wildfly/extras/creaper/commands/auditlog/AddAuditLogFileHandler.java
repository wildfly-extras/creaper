package org.wildfly.extras.creaper.commands.auditlog;

import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

public final class AddAuditLogFileHandler implements OnlineCommand, OfflineCommand {

    private final String name;
    private final String formatter;
    private final int maxFailureCount;
    private final String path;
    private final String relativeTo;
    private final boolean replaceExisting;

    public AddAuditLogFileHandler(Builder builder) {
        this.name = builder.name;
        this.formatter = builder.formatter;
        this.maxFailureCount = builder.maxFailureCount;
        this.path = builder.path;
        this.relativeTo = builder.relativeTo;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);

        Address handlerAddress = Address.coreService("management")
                .and("access", "audit")
                .and("file-handler", name);

        if (replaceExisting) {
            try {
                ops.removeIfExists(handlerAddress);
            } catch (OperationException ex) {
                throw new CommandFailedException("Failed to remove existing file-handler " + name, ex);
            }
        }

        ops.add(handlerAddress, Values.empty()
                .andOptional("name", name)
                .andOptional("formatter", formatter)
                .andOptional("max-failure-count", maxFailureCount)
                .andOptional("path", path)
                .andOptional("relative-to", relativeTo));
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        ctx.client.apply(GroovyXmlTransform.of(AddAuditLogFileHandler.class)
                .subtree("management", Subtree.management())
                .parameter("atrName", name)
                .parameter("atrFormatter", formatter)
                .parameter("atrMaxFailureCount", maxFailureCount)
                .parameter("atrPath", path)
                .parameter("atrRelativeTo", relativeTo)
                .parameter("atrReplaceExisting", replaceExisting)
                .build());
    }

    public static final class Builder {

        private String name;
        private String formatter;
        private int maxFailureCount;
        private String path;
        private String relativeTo;
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("File handler name must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("File handler name must not be an empty string");
            }

            this.name = name;
        }

        public Builder formatter(String formatter) {
            this.formatter = formatter;
            return this;
        }

        public Builder maxFailureCount(int maxFailureCount) {
            this.maxFailureCount = maxFailureCount;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder relativeTo(String relativeTo) {
            this.relativeTo = relativeTo;
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddAuditLogFileHandler build() {
            if (path == null) {
                throw new IllegalArgumentException("Path parameter must be specified as non null value");
            }
            if (path.isEmpty()) {
                throw new IllegalArgumentException("Path parameter cannot be empty");
            }
            if (formatter == null) {
                throw new IllegalArgumentException("Formatter parameter must be specified as non null value");
            }
            if (formatter.isEmpty()) {
                throw new IllegalArgumentException("Formatter parameter cannot be empty");
            }

            return new AddAuditLogFileHandler(this);
        }

    }
}
