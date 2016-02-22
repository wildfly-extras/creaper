package org.wildfly.extras.creaper.commands.security.realms;

/**
 * Helper class which holds information group-to-principal group search in ldap authorization of security realm.
 */
public final class GroupToPrincipal {

    final String baseDn;
    final String groupDnAttribute;
    final String groupName;
    final String groupNameAttribute;
    final Boolean iterative;
    final Boolean preferOriginalConnection;
    final String principalAttribute;
    final Boolean recursive;
    final String searchBy;
    final LdapCache cache;

    private GroupToPrincipal(Builder builder) {
        this.baseDn = builder.baseDn;
        this.groupDnAttribute = builder.groupDnAttribute;
        this.groupName = builder.groupName;
        this.groupNameAttribute = builder.groupNameAttribute;
        this.iterative = builder.iterative;
        this.preferOriginalConnection = builder.preferOriginalConnection;
        this.principalAttribute = builder.principalAttribute;
        this.recursive = builder.recursive;
        this.searchBy = builder.searchBy;
        this.cache = builder.cache;
    }

    public static final class Builder {

        private String baseDn;
        private String groupDnAttribute;
        private String groupNameAttribute;
        private String groupName;
        private Boolean iterative;
        private Boolean preferOriginalConnection;
        private String principalAttribute;
        private Boolean recursive;
        private String searchBy;
        private LdapCache cache;

        public Builder baseDn(String baseDn) {
            this.baseDn = baseDn;
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

        public Builder principalAttribute(String principalAttribute) {
            this.principalAttribute = principalAttribute;
            return this;
        }

        public Builder recursive(Boolean recursive) {
            this.recursive = recursive;
            return this;
        }

        public Builder searchBy(String searchBy) {
            this.searchBy = searchBy;
            return this;
        }

        public Builder cache(LdapCache cache) {
            this.cache = cache;
            return this;
        }

        public GroupToPrincipal build() {
            if (baseDn == null) {
                throw new IllegalArgumentException("base-dn must be specified as non null value");
            }
            if (baseDn.isEmpty()) {
                throw new IllegalArgumentException("base-dn must not be empty value");
            }
            return new GroupToPrincipal(this);
        }

    }
}
