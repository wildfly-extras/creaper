package org.wildfly.extras.creaper.commands.elytron.realm;

import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddCachingRealm implements OnlineCommand, OfflineCommand {

    private final String name;
    private final String realm;
    private final Integer maximumEntries;
    private final Long maximumAge;
    private final boolean replaceExisting;

    private AddCachingRealm(Builder builder) {
        this.name = builder.name;
        this.realm = builder.realm;
        this.maximumEntries = builder.maximumEntries;
        this.maximumAge = builder.maximumAge;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address securityRealmAddress = Address.subsystem("elytron").and("caching-realm", name);
        if (replaceExisting) {
            ops.removeIfExists(securityRealmAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(securityRealmAddress, Values.empty()
            .and("realm", realm)
            .andOptional("maximum-entries", maximumEntries)
            .andOptional("maximum-age", maximumAge));
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        ctx.client.apply(GroovyXmlTransform.of(AddCachingRealm.class)
                .subtree("elytronSubsystem", Subtree.subsystem("elytron"))
                .parameter("atrName", name)
                .parameter("atrRealm", realm)
                .parameter("atrMaximumEntries", maximumEntries)
                .parameter("atrMaximumAge", maximumAge)
                .parameter("atrReplaceExisting", replaceExisting)
                .build());
    }

    public static final class Builder {

        private final String name;
        private String realm;
        private Integer maximumEntries;
        private Long maximumAge;
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the caching-realm must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the caching-realm must not be empty value");
            }

            this.name = name;
        }

        /**
         * A reference to a cacheable security realm.
         */
        public Builder realm(String realm) {
            this.realm = realm;
            return this;
        }

        /**
         * The maximum number of entries to keep in the cache. Defaults to 16.
         */
        public Builder maximumEntries(int maximumEntries) {
            if (maximumEntries < 0) {
                throw new IllegalArgumentException("maximum-entries must not be negative");
            }
            this.maximumEntries = maximumEntries;
            return this;
        }

        /**
         * The time in milliseconds that an item can stay in the cache. Defaults to -1.
         */
        public Builder maximumAge(long maximumAge) {
            this.maximumAge = maximumAge;
            return this;
        }

        /**
         * Replace caching-realm with the same name, if exists.
         */
        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddCachingRealm build() {
            if (realm == null || realm.isEmpty()) {
                throw new IllegalArgumentException("realm must not be null or empty");
            }
            return new AddCachingRealm(this);
        }
    }
}
