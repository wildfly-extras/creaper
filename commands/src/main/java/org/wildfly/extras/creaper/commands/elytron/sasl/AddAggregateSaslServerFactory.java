package org.wildfly.extras.creaper.commands.elytron.sasl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddAggregateSaslServerFactory implements OnlineCommand {

    private final String name;
    private final List<String> saslServerFactories;
    private final boolean replaceExisting;

    private AddAggregateSaslServerFactory(Builder builder) {
        this.name = builder.name;
        this.saslServerFactories = builder.saslServerFactories;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address factoryAddress = Address.subsystem("elytron")
                .and("aggregate-sasl-server-factory", name);
        if (replaceExisting) {
            ops.removeIfExists(factoryAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(factoryAddress, Values.empty()
                .andList(String.class, "sasl-server-factories", saslServerFactories));
    }

    public static final class Builder {

        private final String name;
        private List<String> saslServerFactories = new ArrayList<String>();
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the aggregate-sasl-server-factory must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the aggregate-sasl-server-factory must not be empty value");
            }
            this.name = name;
        }

        public Builder addSaslServerFactories(String... saslServerFactories) {
            if (saslServerFactories == null) {
                throw new IllegalArgumentException("sasl-server-factories added to aggregate-sasl-server-factory must not be null");
            }
            Collections.addAll(this.saslServerFactories, saslServerFactories);
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddAggregateSaslServerFactory build() {
            if (saslServerFactories == null || saslServerFactories.size() < 2) {
                throw new IllegalArgumentException("sasl-server-factory must not be null and must include at least two entries");
            }
            return new AddAggregateSaslServerFactory(this);
        }
    }

}
