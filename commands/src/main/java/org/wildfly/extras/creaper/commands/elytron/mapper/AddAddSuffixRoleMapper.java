package org.wildfly.extras.creaper.commands.elytron.mapper;

import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddAddSuffixRoleMapper implements OnlineCommand {

    private final String name;
    private final String suffix;
    private final boolean replaceExisting;

    private AddAddSuffixRoleMapper(Builder builder) {
        this.name = builder.name;
        this.suffix = builder.suffix;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address mapperAddress = Address.subsystem("elytron").and("add-suffix-role-mapper", name);
        if (replaceExisting) {
            ops.removeIfExists(mapperAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(mapperAddress, Values.empty()
                .and("suffix", suffix));
    }

    public static final class Builder {

        private final String name;
        private String suffix;
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the add-suffix-role-mapper must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the add-suffix-role-mapper must not be empty value");
            }

            this.name = name;
        }

        public Builder suffix(String suffix) {
            this.suffix = suffix;
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddAddSuffixRoleMapper build() {
            if (suffix == null || suffix.isEmpty()) {
                throw new IllegalArgumentException("suffix must not be null or empty string");
            }
            return new AddAddSuffixRoleMapper(this);
        }
    }
}
