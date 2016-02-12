package org.wildfly.extras.creaper.commands.security.realms;

import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

/**
 * Add ldap authentication to security realm.
 */
public final class AddLdapAuthentication extends AbstractAddSecurityRealmSubElement {

    private final String advancedFilter;
    private final Boolean allowEmptyPasswords;
    private final String baseDn;
    private final String connection;
    private final Boolean recursive;
    private final String userDn;
    private final String usernameAttribute;
    private final String usernameLoad;
    private final LdapCache cache;

    private AddLdapAuthentication(Builder builder) {
        super(builder);
        this.advancedFilter = builder.advancedFilter;
        this.allowEmptyPasswords = builder.allowEmptyPasswords;
        this.baseDn = builder.baseDn;
        this.connection = builder.connection;
        this.recursive = builder.recursive;
        this.userDn = builder.userDn;
        this.usernameAttribute = builder.usernameAttribute;
        this.usernameLoad = builder.usernameLoad;
        this.cache = builder.cache;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (usernameLoad != null) {
            ctx.version.assertAtLeast(ServerVersion.VERSION_2_0_0,
                    "Option username-load is available since WildFly 8");
        }

        Operations ops = new Operations(ctx.client);
        Address securityRealmLdapAuthnAddress = securityRealmAddress.and("authentication", "ldap");
        if (replaceExisting) {
            ops.removeIfExists(securityRealmLdapAuthnAddress);
            new Administration(ctx.client).reloadIfRequired();
        }
        ops.add(securityRealmLdapAuthnAddress, Values.empty()
                .andOptional("username-attribute", usernameAttribute)
                .andOptional("advanced-filter", advancedFilter)
                .andOptional("connection", connection)
                .andOptional("base-dn", baseDn)
                .andOptional("user-dn", userDn)
                .andOptional("username-load", usernameLoad)
                .andOptional("allow-empty-passwords", allowEmptyPasswords)
                .andOptional("recursive", recursive));

        if (cache != null) {
            String cacheType = null;
            if (cache.getByAccessTime()) {
                cacheType = "by-access-time";
            }
            if (cache.getBySearchTime()) {
                cacheType = "by-search-time";
            }
            ops.add(securityRealmLdapAuthnAddress.and("cache", cacheType), Values.empty()
                    .andOptional("cache-failures", cache.getCacheFailures())
                    .andOptional("eviction-time", cache.getEvictionTime())
                    .andOptional("max-cache-size", cache.getMaxCacheSize()));
        }
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        ctx.client.apply(GroovyXmlTransform.of(AddLdapAuthentication.class)
                .subtree("management", Subtree.management())
                .parameter("atrSecurityRealmName", securityRealmName)
                .parameter("atrAdvancedFilter", advancedFilter)
                .parameter("atrAllowEmptyPasswords", allowEmptyPasswords)
                .parameter("atrBaseDn", baseDn)
                .parameter("atrConnection", connection)
                .parameter("atrRecursive", recursive)
                .parameter("atrUserDn", userDn)
                .parameter("atrUsernameAttribute", usernameAttribute)
                .parameter("atrUsernameLoad", usernameLoad)
                .parameter("atrCache", cache)
                .parameter("atrReplaceExisting", replaceExisting)
                .build());
    }

    public static final class Builder extends AbstractAddSecurityRealmSubElement.Builder<Builder> {

        private String advancedFilter;
        private Boolean allowEmptyPasswords;
        private String baseDn;
        private String connection;
        private Boolean recursive;
        private String userDn;
        private String usernameAttribute;
        private String usernameLoad;
        private LdapCache cache;

        public Builder(String securityRealmName) {
            super(securityRealmName);
        }

        public Builder advancedFilter(String advancedFilter) {
            this.advancedFilter = advancedFilter;
            return this;
        }

        public Builder allowEmptyPasswords(Boolean allowEmptyPasswords) {
            this.allowEmptyPasswords = allowEmptyPasswords;
            return this;
        }

        public Builder baseDn(String baseDn) {
            this.baseDn = baseDn;
            return this;
        }

        public Builder connection(String connection) {
            this.connection = connection;
            return this;
        }

        public Builder recursive(Boolean recursive) {
            this.recursive = recursive;
            return this;
        }

        public Builder userDn(String userDn) {
            this.userDn = userDn;
            return this;
        }

        public Builder usernameAttribute(String usernameAttribute) {
            this.usernameAttribute = usernameAttribute;
            return this;
        }

        /**
         * <b>This option is available since WildFly 8!</b>
         */
        public Builder usernameLoad(String usernameLoad) {
            this.usernameLoad = usernameLoad;
            return this;
        }

        public Builder cache(LdapCache cache) {
            this.cache = cache;
            return this;
        }

        @Override
        public AddLdapAuthentication build() {
            if (usernameAttribute != null && !usernameAttribute.equals("")
                    && advancedFilter != null && !advancedFilter.equals("")) {
                throw new IllegalArgumentException("Only one of 'username-attribute' or 'advanced-filter' is required.");
            }
            if ((usernameAttribute == null || usernameAttribute.equals(""))
                    && (advancedFilter == null || advancedFilter.equals(""))) {
                throw new IllegalArgumentException("One of 'username-attribute' or 'advanced-filter' required.");
            }
            if (connection == null) {
                throw new IllegalArgumentException("Connection must be specified as non null value");
            }
            if (connection.isEmpty()) {
                throw new IllegalArgumentException("Connection must not be empty value");
            }
            if (baseDn == null) {
                throw new IllegalArgumentException("Connection must be specified as non null value");
            }
            if (baseDn.isEmpty()) {
                throw new IllegalArgumentException("Connection must not be empty value");
            }
            return new AddLdapAuthentication(this);
        }
    }
}
