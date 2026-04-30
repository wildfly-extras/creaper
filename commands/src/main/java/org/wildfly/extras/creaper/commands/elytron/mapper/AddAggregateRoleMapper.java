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

public final class AddAggregateRoleMapper implements OnlineCommand {

    private final String name;
    private final List<String> roleMapperNameList;
    private final boolean replaceExisting;

    private AddAggregateRoleMapper(Builder builder) {
        this.name = builder.name;
        this.roleMapperNameList = builder.roleMapperNameList;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address mapperAddress = Address.subsystem("elytron").and("aggregate-role-mapper", name);
        if (replaceExisting) {
            ops.removeIfExists(mapperAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(mapperAddress, Values.empty()
                .andList(String.class, "role-mappers", roleMapperNameList));
    }

    public static final class Builder {

        private final String name;
        private List<String> roleMapperNameList = new ArrayList<String>();
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the add-aggregate-role-mapper must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the aggregate-role-mapper must not be empty value");
            }

            this.name = name;
        }

        public Builder addRoleMappers(String... roleMappers) {
            if (roleMappers == null) {
                throw new IllegalArgumentException("Roles added to aggregate-role-mapper must not be null");
            }
            Collections.addAll(this.roleMapperNameList, roleMappers);
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddAggregateRoleMapper build() {
            if (roleMapperNameList == null || roleMapperNameList.isEmpty()) {
                throw new IllegalArgumentException("role-mapper must not be null and must include at least one entry");
            }
            return new AddAggregateRoleMapper(this);
        }
    }
}
