package org.wildfly.extras.creaper.commands.security.realms;

/**
 * Helper class which holds information principal-to-group group search in ldap authorization of security realm.
 */
public final class PrincipalToGroup {

    final String groupAttribute;
    final String groupDnAttribute;
    final String groupName;
    final String groupNameAttribute;
    final Boolean iterative;
    final Boolean preferOriginalConnection;
    final Boolean skipMissingGroups;
    final LdapCache cache;

    private PrincipalToGroup(Builder builder) {
        this.groupAttribute = builder.groupAttribute;
        this.groupDnAttribute = builder.groupDnAttribute;
        this.groupNameAttribute = builder.groupNameAttribute;
        this.groupName = builder.groupName;
        this.iterative = builder.iterative;
        this.preferOriginalConnection = builder.preferOriginalConnection;
        this.skipMissingGroups = builder.skipMissingGroups;
        this.cache = builder.cache;
    }

    public static final class Builder {

        private String groupAttribute;
        private String groupDnAttribute;
        private String groupNameAttribute;
        private String groupName;
        private Boolean iterative;
        private Boolean preferOriginalConnection;
        private Boolean skipMissingGroups;
        private LdapCache cache;

        public Builder groupAttribute(String groupAttribute) {
            this.groupAttribute = groupAttribute;
            return this;
        }

        public Builder groupDnAttribute(String groupDnAttribute) {
            this.groupDnAttribute = groupDnAttribute;
            return this;
        }

        public Builder groupNameAttribute(String groupNameAttribute) {
            this.groupNameAttribute = groupNameAttribute;
            return this;
        }

        public Builder groupName(String groupName) {
            this.groupName = groupName;
            return this;
        }

        public Builder iterative(Boolean iterative) {
            this.iterative = iterative;
            return this;
        }

        public Builder preferOriginalConnection(Boolean preferOriginalConnection) {
            this.preferOriginalConnection = preferOriginalConnection;
            return this;
        }

        public Builder skipMissingGroups(Boolean skipMissingGroups) {
            this.skipMissingGroups = skipMissingGroups;
            return this;
        }

        public Builder cache(LdapCache cache) {
            this.cache = cache;
            return this;
        }

        public PrincipalToGroup build() {
            return new PrincipalToGroup(this);
        }
    }
}
