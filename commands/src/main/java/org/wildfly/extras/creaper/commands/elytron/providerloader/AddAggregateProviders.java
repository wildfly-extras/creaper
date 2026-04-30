package org.wildfly.extras.creaper.commands.elytron.providerloader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public class AddAggregateProviders implements OnlineCommand {

    private final String name;
    private final List<String> providers;
    private final boolean replaceExisting;

    private AddAggregateProviders(Builder builder) {
        this.name = builder.name;
        this.providers = builder.providers;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address aggregatepProvidersAddress = Address.subsystem("elytron").and("aggregate-providers", name);
        if (replaceExisting) {
            ops.removeIfExists(aggregatepProvidersAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(aggregatepProvidersAddress, Values.empty()
                .andList(String.class, "providers", providers));
    }

    public static final class Builder {

        private final String name;
        private final List<String> providers = new ArrayList<String>();
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the aggregate-providers must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the aggregate-providers must not be empty value");
            }

            this.name = name;
        }

        public Builder providers(String... providers) {
            if (providers == null) {
                throw new IllegalArgumentException("Providers added to aggregate-providers must not be null");
            }
            Collections.addAll(this.providers, providers);
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddAggregateProviders build() {
            if (providers.size() < 2) {
                throw new IllegalArgumentException("There must be at least two providers");
            }
            return new AddAggregateProviders(this);
        }
    }

}
