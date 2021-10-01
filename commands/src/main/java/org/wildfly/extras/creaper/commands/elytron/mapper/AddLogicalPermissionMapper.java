package org.wildfly.extras.creaper.commands.elytron.mapper;

import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddLogicalPermissionMapper implements OnlineCommand {

    private final String name;
    private final LogicalOperation logicalOperation;
    private final String left;
    private final String right;
    private final boolean replaceExisting;

    private AddLogicalPermissionMapper(Builder builder) {
        this.name = builder.name;
        this.logicalOperation = builder.logicalOperation;
        this.left = builder.left;
        this.right = builder.right;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (ctx.version.lessThan(ServerVersion.VERSION_5_0_0)) {
            throw new AssertionError("Elytron is available since WildFly 11.");
        }

        Operations ops = new Operations(ctx.client);
        Address mapperAddress = Address.subsystem("elytron").and("logical-permission-mapper", name);
        if (replaceExisting) {
            ops.removeIfExists(mapperAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(mapperAddress, Values.empty()
                .and("logical-operation", logicalOperation.name())
                .and("left", left)
                .and("right", right));
    }

    public static final class Builder {

        private final String name;
        private LogicalOperation logicalOperation;
        private String left;
        private String right;
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the logical-permission-mapper must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the logical-permission-mapper must not be empty value");
            }
            this.name = name;
        }

        public Builder logicalOperation(LogicalOperation logicalOperation) {
            this.logicalOperation = logicalOperation;
            return this;
        }

        public Builder left(String left) {
            this.left = left;
            return this;
        }

        public Builder right(String right) {
            this.right = right;
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddLogicalPermissionMapper build() {
            if (logicalOperation == null) {
                throw new IllegalArgumentException("logical-operation must not be null");
            }
            if (left == null) {
                throw new IllegalArgumentException("left must not be null");
            }
            if (right == null) {
                throw new IllegalArgumentException("right must not be null");
            }
            return new AddLogicalPermissionMapper(this);
        }
    }

    public enum LogicalOperation {
        AND, UNLESS, OR, XOR
    }
}
