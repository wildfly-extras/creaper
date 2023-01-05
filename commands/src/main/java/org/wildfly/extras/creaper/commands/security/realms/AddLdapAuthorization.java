package org.wildfly.extras.creaper.commands.security.realms;

import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Batch;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

/**
 * Add LDAP authorization to security realm.
 */
public final class AddLdapAuthorization extends AbstractAddSecurityRealmSubElement {

    private final String connection;
    private final GroupToPrincipal groupToPrincipal;
    private final PrincipalToGroup principalToGroup;
    private final AdvancedFilter advancedFilter;
    private final UsernameFilter usernameFilter;
    private final UsernameIsDn usernameIsDn;

    private AddLdapAuthorization(Builder builder) {
        super(builder);
        this.connection = builder.connection;
        this.groupToPrincipal = builder.groupToPrincipal;
        this.principalToGroup = builder.principalToGroup;
        this.advancedFilter = builder.advancedFilter;
        this.usernameFilter = builder.usernameFilter;
        this.usernameIsDn = builder.usernameIsDn;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (ctx.version.greaterThanOrEqualTo(ServerVersion.VERSION_18_0_0)) {
            throw new AssertionError("Legacy security was removed in WildFly 25.");
        }

        if (groupToPrincipal != null) {
            if (groupToPrincipal.preferOriginalConnection != null) {
                if (ctx.version.lessThan(ServerVersion.VERSION_1_7_0)
                        || ctx.version.inRange(ServerVersion.VERSION_2_0_0, ServerVersion.VERSION_2_2_0)) {
                    throw new AssertionError("Option prefer-original-connection for group-to-principal is available since WildFly 9 or in EAP 6.4.x.");
                }
            }
        }

        if (principalToGroup != null) {
            if (principalToGroup.skipMissingGroups != null) {
                if (ctx.version.lessThan(ServerVersion.VERSION_1_7_0)
                        || ctx.version.inRange(ServerVersion.VERSION_2_0_0, ServerVersion.VERSION_2_2_0)) {
                    throw new AssertionError("Option skip-missing-groups for principal-to-group is available since WildFly 9 or in EAP 6.4.x.");
                }
            }
            if (principalToGroup.preferOriginalConnection != null) {
                if (ctx.version.lessThan(ServerVersion.VERSION_1_7_0)
                        || ctx.version.equalTo(ServerVersion.VERSION_2_0_0)) {
                    throw new AssertionError("Option prefer-original-connection for principal-to-group is available since WildFly 8.1.0 or in EAP 6.4.x.");
                }
            }
            if (principalToGroup.cache != null) {
                if (ctx.version.lessThan(ServerVersion.VERSION_1_7_0)
                        || ctx.version.inRange(ServerVersion.VERSION_2_0_0, ServerVersion.VERSION_2_2_0)) {
                    throw new AssertionError("Cache for principal-to-group is available since WildFly 9 or in EAP 6.4.x.");
                }

            }
        }

        Operations ops = new Operations(ctx.client);
        Address securityRealmLdapAuthzAddress = securityRealmAddress.and("authorization", "ldap");
        if (replaceExisting) {
            ops.removeIfExists(securityRealmLdapAuthzAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        Batch batch = new Batch();

        batch.add(securityRealmLdapAuthzAddress, Values.empty()
                .andOptional("connection", connection));

        if (groupToPrincipal != null) {
            Address groupToPrincipalAddress = securityRealmLdapAuthzAddress.and("group-search", "group-to-principal");
            batch.add(groupToPrincipalAddress, Values.empty()
                    .andOptional("base-dn", groupToPrincipal.baseDn)
                    .andOptional("group-dn-attribute", groupToPrincipal.groupDnAttribute)
                    .andOptional("group-name", groupToPrincipal.groupName)
                    .andOptional("group-name-attribute", groupToPrincipal.groupNameAttribute)
                    .andOptional("iterative", groupToPrincipal.iterative)
                    .andOptional("prefer-original-connection", groupToPrincipal.preferOriginalConnection)
                    .andOptional("principal-attribute", groupToPrincipal.principalAttribute)
                    .andOptional("recursive", groupToPrincipal.recursive)
                    .andOptional("search-by", groupToPrincipal.searchBy));

            addCache(batch, groupToPrincipalAddress, groupToPrincipal.cache);
        }

        if (principalToGroup != null) {
            Address principalToGroupAddress = securityRealmLdapAuthzAddress.and("group-search", "principal-to-group");
            batch.add(principalToGroupAddress, Values.empty()
                    .andOptional("group-attribute", principalToGroup.groupAttribute)
                    .andOptional("group-dn-attribute", principalToGroup.groupDnAttribute)
                    .andOptional("group-name", principalToGroup.groupName)
                    .andOptional("group-name-attribute", principalToGroup.groupNameAttribute)
                    .andOptional("iterative", principalToGroup.iterative)
                    .andOptional("prefer-original-connection", principalToGroup.preferOriginalConnection)
                    .andOptional("skip-missing-groups", principalToGroup.skipMissingGroups));

            addCache(batch, principalToGroupAddress, principalToGroup.cache);
        }

        if (advancedFilter != null) {
            Address advancedFilterAddress = securityRealmLdapAuthzAddress.and("username-to-dn", "advanced-filter");
            batch.add(advancedFilterAddress, Values.empty()
                    .andOptional("base-dn", advancedFilter.baseDn)
                    .andOptional("filter", advancedFilter.filter)
                    .andOptional("force", advancedFilter.force)
                    .andOptional("recursive", advancedFilter.recursive)
                    .andOptional("user-dn-attribute", advancedFilter.userDnAttribute));

            addCache(batch, advancedFilterAddress, advancedFilter.cache);
        }

        if (usernameFilter != null) {
            Address usernameFilterAddress = securityRealmLdapAuthzAddress.and("username-to-dn", "username-filter");
            batch.add(usernameFilterAddress, Values.empty()
                    .andOptional("attribute", usernameFilter.attribute)
                    .andOptional("base-dn", usernameFilter.baseDn)
                    .andOptional("force", usernameFilter.force)
                    .andOptional("recursive", usernameFilter.recursive)
                    .andOptional("user-dn-attribute", usernameFilter.userDnAttribute));

            addCache(batch, usernameFilterAddress, usernameFilter.cache);
        }

        if (usernameIsDn != null) {
            Address usernameIsDnAddress = securityRealmLdapAuthzAddress.and("username-to-dn", "username-is-dn");
            batch.add(usernameIsDnAddress, Values.empty()
                    .andOptional("force", usernameIsDn.force));

            addCache(batch, usernameIsDnAddress, usernameIsDn.cache);
        }

        ops.batch(batch);

    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        if (ctx.version.greaterThanOrEqualTo(ServerVersion.VERSION_18_0_0)) {
            throw new AssertionError("Legacy security was removed in WildFly 25.");
        }

        if (groupToPrincipal != null) {
            if (groupToPrincipal.preferOriginalConnection != null) {
                if (ctx.version.lessThan(ServerVersion.VERSION_1_7_0)
                        || ctx.version.inRange(ServerVersion.VERSION_2_0_0, ServerVersion.VERSION_2_2_0)) {
                    throw new AssertionError("Option prefer-original-connection for group-to-principal is available since WildFly 9 or in EAP 6.4.x.");
                }
            }
        }

        if (principalToGroup != null) {
            if (principalToGroup.skipMissingGroups != null) {
                if (ctx.version.lessThan(ServerVersion.VERSION_1_7_0)
                        || ctx.version.inRange(ServerVersion.VERSION_2_0_0, ServerVersion.VERSION_2_2_0)) {
                    throw new AssertionError("Option skip-missing-groups for principal-to-group is available since WildFly 9 or in EAP 6.4.x.");
                }
            }
            if (principalToGroup.preferOriginalConnection != null) {
                if (ctx.version.lessThan(ServerVersion.VERSION_1_7_0)
                        || ctx.version.equalTo(ServerVersion.VERSION_2_0_0)) {
                    throw new AssertionError("Option prefer-original-connection for principal-to-group is available since WildFly 8.1.0 or in EAP 6.4.x.");
                }
            }
            if (principalToGroup.cache != null) {
                if (ctx.version.lessThan(ServerVersion.VERSION_1_7_0)
                        || ctx.version.inRange(ServerVersion.VERSION_2_0_0, ServerVersion.VERSION_2_2_0)) {
                    throw new AssertionError("Cache for principal-to-group is available since WildFly 9 or in EAP 6.4.x.");
                }

            }
        }

        ctx.client.apply(GroovyXmlTransform.of(AddLdapAuthorization.class)
                .subtree("management", Subtree.management())
                .parameter("atrSecurityRealmName", securityRealmName)
                .parameter("atrConnection", connection)
                .parameter("atrReplaceExisting", replaceExisting)
                .parameter("atrGroupToPrincipal", groupToPrincipal)
                .parameter("atrPrincipalToGroup", principalToGroup)
                .parameter("atrAdvancedFilter", advancedFilter)
                .parameter("atrUsernameFilter", usernameFilter)
                .parameter("atrUsernameIsDn", usernameIsDn)
                .build());
    }

    private void addCache(Batch batch, Address address, LdapCache cache) {
        if (cache != null) {
            String cacheType = null;
            if (cache.getByAccessTime()) {
                cacheType = "by-access-time";
            } else if (cache.getBySearchTime()) {
                cacheType = "by-search-time";
            }
            batch.add(address.and("cache", cacheType), Values.empty()
                    .andOptional("cache-failures", cache.getCacheFailures())
                    .andOptional("eviction-time", cache.getEvictionTime())
                    .andOptional("max-cache-size", cache.getMaxCacheSize()));
        }
    }

    public static final class Builder extends AbstractAddSecurityRealmSubElement.Builder<Builder> {

        private String connection;
        private GroupToPrincipal groupToPrincipal;
        private PrincipalToGroup principalToGroup;
        private AdvancedFilter advancedFilter;
        private UsernameFilter usernameFilter;
        private UsernameIsDn usernameIsDn;

        public Builder(String securityRealmName) {
            super(securityRealmName);
        }

        public Builder connection(String connection) {
            this.connection = connection;
            return this;
        }

        public Builder groupToPrincipal(GroupToPrincipal groupToPrincipal) {
            this.groupToPrincipal = groupToPrincipal;
            return this;
        }

        public Builder principalToGroup(PrincipalToGroup principalToGroup) {
            this.principalToGroup = principalToGroup;
            return this;
        }

        public Builder advancedFilter(AdvancedFilter advancedFilter) {
            this.advancedFilter = advancedFilter;
            return this;
        }

        public Builder usernameFilter(UsernameFilter usernameFilter) {
            this.usernameFilter = usernameFilter;
            return this;
        }

        public Builder usernameIsDn(UsernameIsDn usernameIsDn) {
            this.usernameIsDn = usernameIsDn;
            return this;
        }

        @Override
        public AddLdapAuthorization build() {
            if (connection == null) {
                throw new IllegalArgumentException("Connection must be specified as non null value");
            }
            if (connection.isEmpty()) {
                throw new IllegalArgumentException("Connection must not be empty value");
            }
            if (groupToPrincipal != null && principalToGroup != null) {
                throw new IllegalArgumentException("Only one of 'group-to-principal' or 'principal-to-group' is required.");
            }
            if (groupToPrincipal == null && principalToGroup == null) {
                throw new IllegalArgumentException("One of 'group-to-principal' or 'principal-to-group' is required.");
            }
            if ((advancedFilter != null && (usernameFilter != null || usernameIsDn != null))
                    || (usernameFilter != null && (advancedFilter != null || usernameIsDn != null))
                    || (usernameIsDn != null && (advancedFilter != null || usernameFilter != null))) {
                throw new IllegalArgumentException("Only one of 'advanced-filter','username-filter','username-is-dn' is allowed.");
            }
            return new AddLdapAuthorization(this);
        }
    }
}
