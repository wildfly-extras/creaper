package org.wildfly.extras.creaper.commands.elytron.realm;

import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddKeyStoreRealm implements OnlineCommand {

    private final String name;
    private final String keyStore;
    private final boolean replaceExisting;

    private AddKeyStoreRealm(Builder builder) {
        this.name = builder.name;
        this.keyStore = builder.keyStore;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address securityRealmAddress = Address.subsystem("elytron").and("key-store-realm", name);
        if (replaceExisting) {
            ops.removeIfExists(securityRealmAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(securityRealmAddress, Values.empty()
            .and("key-store", keyStore));
    }

    public static final class Builder {

        private final String name;
        private String keyStore;
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the key-store-realm must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the key-store-realm must not be empty value");
            }

            this.name = name;
        }

        public Builder keyStore(String keyStore) {
            this.keyStore = keyStore;
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddKeyStoreRealm build() {
            if (keyStore == null || keyStore.isEmpty()) {
                throw new IllegalArgumentException("key-store must not be null or empty string");
            }
            return new AddKeyStoreRealm(this);
        }
    }
}
