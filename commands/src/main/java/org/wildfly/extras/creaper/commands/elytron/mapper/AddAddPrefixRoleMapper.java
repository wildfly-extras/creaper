package org.wildfly.extras.creaper.commands.elytron.mapper;

import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddAddPrefixRoleMapper implements OnlineCommand {

    private final String name;
    private final String prefix;
    private final boolean replaceExisting;

    private AddAddPrefixRoleMapper(Builder builder) {
        this.name = builder.name;
        this.prefix = builder.prefix;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address mapperAddress = Address.subsystem("elytron").and("add-prefix-role-mapper", name);
        if (replaceExisting) {
            ops.removeIfExists(mapperAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(mapperAddress, Values.empty()
                .and("prefix", prefix));
    }

    public static final class Builder {

        private final String name;
        private String prefix;
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the add-prefix-role-mapper must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the add-prefix-role-mapper must not be empty value");
            }

            this.name = name;
        }

        public Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddAddPrefixRoleMapper build() {
            if (prefix == null || prefix.isEmpty()) {
                throw new IllegalArgumentException("prefix must not be null or empty string");
            }
            return new AddAddPrefixRoleMapper(this);
        }
    }
}
