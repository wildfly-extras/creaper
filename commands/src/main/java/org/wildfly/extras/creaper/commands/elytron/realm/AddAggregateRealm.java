package org.wildfly.extras.creaper.commands.elytron.realm;

import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddAggregateRealm implements OnlineCommand {

    private final String name;
    private final String authenticationRealm;
    private final String authorizationRealm;
    private final boolean replaceExisting;

    private AddAggregateRealm(Builder builder) {
        this.name = builder.name;
        this.authenticationRealm = builder.authenticationRealm;
        this.authorizationRealm = builder.authorizationRealm;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address securityRealmAddress = Address.subsystem("elytron").and("aggregate-realm", name);
        if (replaceExisting) {
            ops.removeIfExists(securityRealmAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(securityRealmAddress, Values.empty()
            .and("authentication-realm", authenticationRealm)
            .and("authorization-realm", authorizationRealm));
    }

    public static final class Builder {

        private final String name;
        private String authenticationRealm;
        private String authorizationRealm;
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the aggregate-realm must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the aggregate-realm must not be empty value");
            }

            this.name = name;
        }

        public Builder authenticationRealm(String authenticationRealm) {
            this.authenticationRealm = authenticationRealm;
            return this;
        }

        public Builder authorizationRealm(String authorizationRealm) {
            this.authorizationRealm = authorizationRealm;
            return this;
        }


        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddAggregateRealm build() {
            if (authenticationRealm == null || authenticationRealm.isEmpty()) {
                throw new IllegalArgumentException("authentication-realm must not be null or empty");
            }
            if (authorizationRealm == null || authorizationRealm.isEmpty()) {
                throw new IllegalArgumentException("authorization-realm must not be null or empty");
            }
            return new AddAggregateRealm(this);
        }
    }
}
