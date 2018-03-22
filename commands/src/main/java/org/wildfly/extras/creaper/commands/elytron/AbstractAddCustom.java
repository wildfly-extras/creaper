package org.wildfly.extras.creaper.commands.elytron;

import java.util.LinkedHashMap;
import java.util.Map;

import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public abstract class AbstractAddCustom implements OnlineCommand, OfflineCommand {

    private final String name;
    private final String className;
    private final String module;
    private final Map<String, String> configuration;
    private final boolean replaceExisting;

    protected AbstractAddCustom(Builder<? extends Builder> builder) {
        this.name = builder.name;
        this.className = builder.className;
        this.module = builder.module;
        this.configuration = builder.configuration;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public final void apply(OnlineCommandContext ctx) throws Exception {
        if (ctx.version.lessThan(ServerVersion.VERSION_5_0_0)) {
            throw new AssertionError("Elytron is available since WildFly 11.");
        }

        Operations ops = new Operations(ctx.client);
        Address address = Address.subsystem("elytron").and(getCustomTypeName(), name);
        if (replaceExisting) {
            ops.removeIfExists(address);
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(address, Values.empty()
                .and("class-name", className)
                .and("module", module)
                .andObjectOptional("configuration", Values.fromMap(configuration)));
    }

    @Override
    public final void apply(OfflineCommandContext ctx) throws Exception {
        if (ctx.version.lessThan(ServerVersion.VERSION_5_0_0)) {
            throw new AssertionError("Elytron is available since WildFly 11.");
        }

        ctx.client.apply(getGroovyBuilder()
                .subtree("elytronSubsystem", Subtree.subsystem("elytron"))
                .parameter("atrName", name)
                .parameter("atrClassName", className)
                .parameter("atrModule", module)
                .parameter("atrConfiguration", configuration)
                .parameter("atrReplaceExisting", replaceExisting)
                .build());
    }

    protected abstract GroovyXmlTransform.Builder getGroovyBuilder();

    protected abstract String getCustomTypeName();

    protected abstract static class Builder<T extends Builder> {

        private final String name;
        protected String className;
        protected String module;
        private Map<String, String> configuration = new LinkedHashMap<String, String>();
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the custom-realm-mapper must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the custom-realm-mapper must not be empty value");
            }

            this.name = name;
        }

        public abstract <S extends AbstractAddCustom> S build();

        public final T className(String className) {
            this.className = className;
            return (T) this;
        }

        public final T module(String module) {
            this.module = module;
            return (T) this;
        }

        public final T addConfiguration(String name, String value) {
            configuration.put(name, value);
            return (T) this;
        }

        public final T addConfiguration(String name, boolean value) {
            configuration.put(name, Boolean.toString(value));
            return (T) this;
        }

        public final T replaceExisting() {
            this.replaceExisting = true;
            return (T) this;
        }

        protected final void checkClassNameAndModule() {
            if (className == null || className.isEmpty()) {
                throw new IllegalArgumentException("className must not be null or empty string");
            }
            if (module == null || module.isEmpty()) {
                throw new IllegalArgumentException("module must not be null or empty string");
            }
        }
    }
}
