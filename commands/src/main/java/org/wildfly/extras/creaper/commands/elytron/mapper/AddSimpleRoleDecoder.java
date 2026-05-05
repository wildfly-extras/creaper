package org.wildfly.extras.creaper.commands.elytron.mapper;

import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddSimpleRoleDecoder implements OnlineCommand {

    private final String name;
    private final String attribute;
    private final boolean replaceExisting;

    private AddSimpleRoleDecoder(Builder builder) {
        this.name = builder.name;
        this.replaceExisting = builder.replaceExisting;
        this.attribute = builder.attribute;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address simpleRoleDecoderAddress = Address.subsystem("elytron").and("simple-role-decoder", name);
        if (replaceExisting) {
            ops.removeIfExists(simpleRoleDecoderAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(simpleRoleDecoderAddress, Values.empty()
                .and("attribute", attribute));
    }

    public static final class Builder {

        private final String name;
        private String attribute;
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the simple-role-decoder must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the simple-role-decoder must not be empty value");
            }
            this.name = name;
        }

        public Builder attribute(String attribute) {
            this.attribute = attribute;
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddSimpleRoleDecoder build() {
            if (attribute == null || attribute.isEmpty()) {
                throw new IllegalArgumentException("Attribute must not be null and must have a minimum length of 1 character");
            }
            return new AddSimpleRoleDecoder(this);
        }
    }

}
