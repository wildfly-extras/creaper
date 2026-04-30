package org.wildfly.extras.creaper.commands.elytron.mapper;

import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddConstantRealmMapper implements OnlineCommand {

    private final String name;
    private final String realmName;
    private final boolean replaceExisting;

    private AddConstantRealmMapper(Builder builder) {
        this.name = builder.name;
        this.realmName = builder.realmName;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address constantRealmMapperAddress = Address.subsystem("elytron").and("constant-realm-mapper", name);
        if (replaceExisting) {
            ops.removeIfExists(constantRealmMapperAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(constantRealmMapperAddress, Values.empty()
                .and("realm-name", realmName));
    }

    public static final class Builder {

        private final String name;
        private String realmName;
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the constant-realm-mapper must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the constant-realm-mapper must not be empty value");
            }
            this.name = name;
        }

        public Builder realmName(String realmName) {
            this.realmName = realmName;
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddConstantRealmMapper build() {
            if (realmName == null || realmName.isEmpty()) {
                throw new IllegalArgumentException("Realm-name must not be null and must have a minimum length of 1 character");
            }
            return new AddConstantRealmMapper(this);
        }
    }

}
