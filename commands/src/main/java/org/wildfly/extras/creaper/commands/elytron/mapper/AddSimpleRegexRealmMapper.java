package org.wildfly.extras.creaper.commands.elytron.mapper;

import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddSimpleRegexRealmMapper implements OnlineCommand {

    private final String name;
    private final String pattern;
    private final String delegateRealmMapper;
    private final boolean replaceExisting;

    private AddSimpleRegexRealmMapper(Builder builder) {
        this.name = builder.name;
        this.pattern = builder.pattern;
        this.delegateRealmMapper = builder.delegateRealmMapper;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address mapperAddress = Address.subsystem("elytron").and("simple-regex-realm-mapper", name);
        if (replaceExisting) {
            ops.removeIfExists(mapperAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(mapperAddress, Values.empty()
                .and("pattern", pattern)
                .andOptional("delegate-realm-mapper", delegateRealmMapper));
    }

    public static final class Builder {

        private String name;
        private String pattern;
        private String delegateRealmMapper;
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the simple-regex-realm-mapper must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the simple-regex-realm-mapper must not be empty value");
            }
            this.name = name;
        }

        public Builder pattern(String pattern) {
            this.pattern = pattern;
            return this;
        }

        public Builder delegateRealmMapper(String delegateRealmMapper) {
            this.delegateRealmMapper = delegateRealmMapper;
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddSimpleRegexRealmMapper build() {
            if (pattern == null || pattern.isEmpty()) {
                throw new IllegalArgumentException("pattern must not be null or empty");
            }
            return new AddSimpleRegexRealmMapper(this);
        }
    }
}
