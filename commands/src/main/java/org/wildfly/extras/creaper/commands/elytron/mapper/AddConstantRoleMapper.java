package org.wildfly.extras.creaper.commands.elytron.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddConstantRoleMapper implements OnlineCommand {

    private final String name;
    private final List<String> roles;
    private final boolean replaceExisting;

    private AddConstantRoleMapper(Builder builder) {
        this.name = builder.name;
        this.roles = builder.roles;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address mapperAddress = Address.subsystem("elytron").and("constant-role-mapper", name);
        if (replaceExisting) {
            ops.removeIfExists(mapperAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(mapperAddress, Values.empty()
                .andList(String.class, "roles", roles));
    }

    public static final class Builder {

        private final String name;
        private final List<String> roles = new ArrayList<String>();
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the constant-role-mapper must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the constant-role-mapper must not be empty value");
            }
            this.name = name;
        }

        public Builder addRoles(String... roles) {
            if (roles == null) {
                throw new IllegalArgumentException("Roles added to constant-role-mapper must not be null");
            }
            Collections.addAll(this.roles, roles);
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddConstantRoleMapper build() {
            if (roles == null || roles.isEmpty()) {
                throw new IllegalArgumentException("roles must not be null and must include at least one entry");
            }
            return new AddConstantRoleMapper(this);
        }
    }
}
