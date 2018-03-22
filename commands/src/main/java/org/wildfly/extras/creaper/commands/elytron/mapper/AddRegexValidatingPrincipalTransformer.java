package org.wildfly.extras.creaper.commands.elytron.mapper;

import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddRegexValidatingPrincipalTransformer implements OnlineCommand {

    private final String name;
    private final String pattern;
    private final Boolean match;
    private final boolean replaceExisting;

    private AddRegexValidatingPrincipalTransformer(Builder builder) {
        this.name = builder.name;
        this.pattern = builder.pattern;
        this.match = builder.match;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (ctx.version.lessThan(ServerVersion.VERSION_5_0_0)) {
            throw new AssertionError("Elytron is available since WildFly 11.");
        }

        Operations ops = new Operations(ctx.client);
        Address regexPrincipalTransformerAddress = Address.subsystem("elytron").and("regex-validating-principal-transformer",
                name);
        if (replaceExisting) {
            ops.removeIfExists(regexPrincipalTransformerAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(regexPrincipalTransformerAddress, Values.empty()
                .and("pattern", pattern)
                .andOptional("match", match)
                .andOptional("replace-all", match));
    }

    public static final class Builder {

        private final String name;
        private String pattern;
        private Boolean match;
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the regex-validating-principal-transformer must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the regex-validating-principal-transformer must not be empty value");
            }
            this.name = name;
        }

        public Builder pattern(String pattern) {
            this.pattern = pattern;
            return this;
        }

        public Builder match(boolean match) {
            this.match = match;
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddRegexValidatingPrincipalTransformer build() {
            if (pattern == null || pattern.isEmpty()) {
                throw new IllegalArgumentException("Pattern must not be null and must have a minimum length of 1 character");
            }
            return new AddRegexValidatingPrincipalTransformer(this);
        }
    }
}
