package org.wildfly.extras.creaper.commands.elytron.realm;

import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddFilesystemRealm implements OnlineCommand {

    private final String name;
    private final Integer levels;
    private final String path;
    private final String relativeTo;
    private final Boolean encoded;
    private final boolean replaceExisting;

    private AddFilesystemRealm(Builder builder) {
        this.name = builder.name;
        this.levels = builder.levels;
        this.path = builder.path;
        this.relativeTo = builder.relativeTo;
        this.encoded = builder.encoded;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address securityRealmAddress = Address.subsystem("elytron").and("filesystem-realm", name);
        if (replaceExisting) {
            ops.removeIfExists(securityRealmAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(securityRealmAddress, Values.empty()
                .and("path", path)
                .andOptional("encoded", encoded)
                .andOptional("levels", levels)
                .andOptional("relative-to", relativeTo));

        new Administration(ctx.client).reloadIfRequired();
    }


    public static final class Builder {

        private final String name;
        private Integer levels;
        private String path;
        private String relativeTo;
        private Boolean encoded;
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the filesystem-realm must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the filesystem-realm must not be empty value");
            }
            this.name = name;
        }

        public Builder levels(Integer levels) {
            this.levels = levels;
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

        public Builder encoded(Boolean encoded) {
            this.encoded = encoded;
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddFilesystemRealm build() {
            if (path == null || path.isEmpty()) {
                throw new IllegalArgumentException("path must not be null and must have a minimum length of 1 characters");
            }
            return new AddFilesystemRealm(this);
        }

    }

}
