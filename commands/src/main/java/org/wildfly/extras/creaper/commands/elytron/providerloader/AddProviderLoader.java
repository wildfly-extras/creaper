package org.wildfly.extras.creaper.commands.elytron.providerloader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.wildfly.extras.creaper.core.ServerVersion;

import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public class AddProviderLoader implements OnlineCommand {

    private final String name;
    private final List<String> classNames;
    private final Map<String, String> configuration;
    private final String module;
    private final String path;
    private final String relativeTo;
    private final String argument;
    private final boolean replaceExisting;

    private AddProviderLoader(Builder builder) {
        this.name = builder.name;
        this.classNames = builder.classNames;
        this.configuration = builder.configuration;
        this.module = builder.module;
        this.path = builder.path;
        this.relativeTo = builder.relativeTo;
        this.argument = builder.argument;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (ctx.version.lessThan(ServerVersion.VERSION_5_0_0)) {
            throw new AssertionError("Elytron is available since WildFly 11.");
        }

        Operations ops = new Operations(ctx.client);
        Address providerLoaderAddress = Address.subsystem("elytron").and("provider-loader", name);
        if (replaceExisting) {
            ops.removeIfExists(providerLoaderAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(providerLoaderAddress, Values.empty()
                .andListOptional(String.class, "class-names", classNames)
                .andObjectOptional("configuration", Values.fromMap(configuration))
                .andOptional("module", module)
                .andOptional("path", path)
                .andOptional("relative-to", relativeTo)
                .andOptional("argument", argument));
    }

    public static final class Builder {

        private final String name;
        private List<String> classNames;
        private Map<String, String> configuration = new LinkedHashMap<String, String>();
        private String module;
        private String path;
        private String relativeTo;
        private String argument;
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the provider-loader must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the provider-loader must not be empty value");
            }
            this.name = name;
        }

        public Builder classNames(String... classNames) {
            if (classNames == null) {
                throw new IllegalArgumentException("Class-names added to provider-loader must not be null");
            }
            if (this.classNames == null) {
                this.classNames = new ArrayList<String>();
            }

            Collections.addAll(this.classNames, classNames);
            return this;
        }

        public Builder addConfiguration(String name, String value) {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Name of the configuration of the provider-loader must not be null");
            }
            if (value == null || value.isEmpty()) {
                throw new IllegalArgumentException("Value of the configuration of the provider-loader must not be null");
            }
            configuration.put(name, value);
            return this;
        }

        public Builder module(String module) {
            this.module = module;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder relativeTo(String relativeTo) {
            this.relativeTo = relativeTo;
            return this;
        }

        public Builder argument(String argument) {
            this.argument = argument;
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddProviderLoader build() {
            boolean isConfiguration = configuration != null && !configuration.isEmpty();
            boolean isPath = path != null && !path.isEmpty();
            boolean isArgument = argument != null && !argument.isEmpty();

            if ((isArgument && isPath) || (isArgument && isConfiguration) || (isPath && isConfiguration)) {
                throw new IllegalArgumentException("There must be most one configuration approach chosen from [ argument, path, configuration ] ");
            }

            return new AddProviderLoader(this);
        }


    }

}
