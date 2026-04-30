package org.wildfly.extras.creaper.commands.elytron.realm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddIdentityRealm implements OnlineCommand {

    private final String name;
    private final String identity;
    private final String attributeName;
    private final List<String> attributeValues;
    private final boolean replaceExisting;

    private AddIdentityRealm(Builder builder) {
        this.name = builder.name;
        this.identity = builder.identity;
        this.attributeName = builder.attributeName;
        this.attributeValues = builder.attributeValues;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address identityRealmAddress = Address.subsystem("elytron").and("identity-realm", name);
        if (replaceExisting) {
            ops.removeIfExists(identityRealmAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(identityRealmAddress, Values.empty()
                .and("identity", identity)
                .andOptional("attribute-name", attributeName)
                .andListOptional(String.class, "attribute-values", attributeValues));
    }

    public static final class Builder {

        private final String name;
        private String identity;
        private String attributeName;
        private List<String> attributeValues;
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the identity-realm must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the identity-realm must not be empty value");
            }
            this.name = name;
        }

        public Builder identity(String identity) {
            this.identity = identity;
            return this;
        }

        public Builder attributeName(String attributeName) {
            this.attributeName = attributeName;
            return this;
        }

        public Builder attributeValues(String... attributeValues) {
            if (attributeValues == null) {
                throw new IllegalArgumentException("Attribute values added to identity-realm must not be null");
            }
            if (this.attributeValues == null) {
                this.attributeValues = new ArrayList<String>();
            }

            Collections.addAll(this.attributeValues, attributeValues);
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddIdentityRealm build() {
            if (identity == null || identity.isEmpty()) {
                throw new IllegalArgumentException("Identity must not be null and must have a minimum length of 1 character");
            }
            return new AddIdentityRealm(this);
        }
    }
}
