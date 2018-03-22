package org.wildfly.extras.creaper.commands.elytron.audit;

import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddSizeRotatingFileAuditLog implements OnlineCommand {

    private final String name;
    private final String path;
    private final String relativeTo;
    private final Boolean paramSynchronized;
    private final AuditFormat format;
    private final String suffix;
    private final String rotateSize;
    private final Long maxBackupIndex;
    private final Boolean rotateOnBoot;
    private final boolean replaceExisting;

    private AddSizeRotatingFileAuditLog(Builder builder) {
        this.name = builder.name;
        this.path = builder.path;
        this.relativeTo = builder.relativeTo;
        this.paramSynchronized = builder.paramSynchronized;
        this.format = builder.format;
        this.suffix = builder.suffix;
        this.replaceExisting = builder.replaceExisting;
        this.rotateSize = builder.rotateSize;
        this.maxBackupIndex = builder.maxBackupIndex;
        this.rotateOnBoot = builder.rotateOnBoot;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (ctx.version.lessThan(ServerVersion.VERSION_5_0_0)) {
            throw new AssertionError("Elytron is available since WildFly 11.");
        }

        Operations ops = new Operations(ctx.client);
        Address fileAuditAddress = Address.subsystem("elytron").and("size-rotating-file-audit-log", name);
        if (replaceExisting) {
            ops.removeIfExists(fileAuditAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(fileAuditAddress, Values.empty()
                .and("path", path)
                .andOptional("suffix", suffix)
                .andOptional("max-backup-index", maxBackupIndex)
                .andOptional("rotate-on-boot", rotateOnBoot)
                .andOptional("rotate-size", rotateSize)
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
        private String rotateSize;
        private Long maxBackupIndex;
        private Boolean rotateOnBoot;
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the size-rotating-file-audit-log must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the size-rotating-file-audit-log must not be empty value");
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

        public Builder rotateSize(String rotateSize) {
            this.rotateSize = rotateSize;
            return this;
        }

        public Builder maxBackupIndex(long maxBackupIndex) {
            this.maxBackupIndex = maxBackupIndex;
            return this;
        }

        public Builder rotateOnBoot(boolean rotateOnBoot) {
            this.rotateOnBoot = rotateOnBoot;
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddSizeRotatingFileAuditLog build() {
            if (path == null || path.isEmpty()) {
                throw new IllegalArgumentException("Path must not be null and must have a minimum length of 1 character");
            }

            return new AddSizeRotatingFileAuditLog(this);
        }

    }
}
