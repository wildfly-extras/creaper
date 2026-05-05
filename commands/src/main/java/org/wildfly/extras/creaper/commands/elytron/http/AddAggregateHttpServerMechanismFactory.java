package org.wildfly.extras.creaper.commands.elytron.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddAggregateHttpServerMechanismFactory implements OnlineCommand {

    private final String name;
    private final List<String> httpServerMechanismFactories;
    private final boolean replaceExisting;

    private AddAggregateHttpServerMechanismFactory(Builder builder) {
        this.name = builder.name;
        this.httpServerMechanismFactories = builder.httpServerMechanismFactories;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address factoryAddress = Address.subsystem("elytron")
                .and("aggregate-http-server-mechanism-factory", name);
        if (replaceExisting) {
            ops.removeIfExists(factoryAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(factoryAddress, Values.empty()
                .andList(String.class, "http-server-mechanism-factories", httpServerMechanismFactories));
    }

    public static final class Builder {

        private final String name;
        private List<String> httpServerMechanismFactories = new ArrayList<String>();
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the aggregate-http-server-mechanism-factory must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the aggregate-http-server-mechanism-factory must not be empty value");
            }
            this.name = name;
        }

        public Builder addHttpServerMechanismFactories(String... httpServerMechanismFactories) {
            if (httpServerMechanismFactories == null) {
                throw new IllegalArgumentException("http-server-mechanism-factories added to aggregate-http-server-mechanism-factory must not be null");
            }
            Collections.addAll(this.httpServerMechanismFactories, httpServerMechanismFactories);
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddAggregateHttpServerMechanismFactory build() {
            if (httpServerMechanismFactories == null || httpServerMechanismFactories.size() < 2) {
                throw new IllegalArgumentException("http-server-mechanism-factory must not be null and must include at least two entries");
            }
            return new AddAggregateHttpServerMechanismFactory(this);
        }
    }
}
