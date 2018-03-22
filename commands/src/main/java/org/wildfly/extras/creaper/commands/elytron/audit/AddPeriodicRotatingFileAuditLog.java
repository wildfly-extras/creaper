package org.wildfly.extras.creaper.commands.elytron.audit;

import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddPeriodicRotatingFileAuditLog implements OnlineCommand {

    private final String name;
    private final String path;
    private final String relativeTo;
    private final Boolean paramSynchronized;
    private final AuditFormat format;
    private final String suffix;
    private final boolean replaceExisting;

    private AddPeriodicRotatingFileAuditLog(Builder builder) {
        this.name = builder.name;
        this.path = builder.path;
        this.relativeTo = builder.relativeTo;
        this.paramSynchronized = builder.paramSynchronized;
        this.format = builder.format;
        this.suffix = builder.suffix;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (ctx.version.lessThan(ServerVersion.VERSION_5_0_0)) {
            throw new AssertionError("Elytron is available since WildFly 11.");
        }

        Operations ops = new Operations(ctx.client);
        Address fileAuditAddress = Address.subsystem("elytron").and("periodic-rotating-file-audit-log", name);
        if (replaceExisting) {
            ops.removeIfExists(fileAuditAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(fileAuditAddress, Values.empty()
                .and("path", path)
                .and("suffix", suffix)
                .andOptional("relative-to", relativeTo)
                .andOptional("synchronized", paramSynchronized)
                .andOptional("format", format == null ? null : format.name()));
    }

    public static final class Builder {

        private final String name;
        private String path;
        private String relativeTo;
        private Boolean paramSynchronized;
        private AuditFormat format;
        private String suffix;
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the periodic-rotating-file-audit-log must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the periodic-rotating-file-audit-log must not be empty value");
            }
            this.name = name;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder relativeTo(String relativeTo) {
            this.relativeTo = relativeTo;
            return this;
        }

        public Builder paramSynchronized(boolean paramSynchronized) {
            this.paramSynchronized = paramSynchronized;
            return this;
        }

        public Builder format(AuditFormat format) {
            this.format = format;
            return this;
        }

        public Builder suffix(String suffix) {
            this.suffix = suffix;
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddPeriodicRotatingFileAuditLog build() {
            if (path == null || path.isEmpty()) {
                throw new IllegalArgumentException("Path must not be null and must have a minimum length of 1 character");
            }
            if (suffix == null || suffix.isEmpty()) {
                throw new IllegalArgumentException("Suffix must not be null and must have a minimum length of 1 character");
            }

            return new AddPeriodicRotatingFileAuditLog(this);
        }

    }
}
