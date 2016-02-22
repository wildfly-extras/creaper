package org.wildfly.extras.creaper.commands.security.realms;

/**
 * Helper class which holds information advanced-filter username to dn in ldap authorization of security realm.
 */
public final class AdvancedFilter {

    final String baseDn;
    final String filter;
    final Boolean force;
    final Boolean recursive;
    final String userDnAttribute;
    final LdapCache cache;

    private AdvancedFilter(Builder builder) {
        this.baseDn = builder.baseDn;
        this.filter = builder.filter;
        this.force = builder.force;
        this.recursive = builder.recursive;
        this.userDnAttribute = builder.userDnAttribute;
        this.cache = builder.cache;
    }

    public static final class Builder {

        private String baseDn;
        private String filter;
        private Boolean force;
        private Boolean recursive;
        private String userDnAttribute;
        private LdapCache cache;

        public Builder baseDn(String baseDn) {
            this.baseDn = baseDn;
            return this;
        }

        public Builder filter(String filter) {
            this.filter = filter;
            return this;
        }

        public Builder force(Boolean force) {
            this.force = force;
            return this;
        }

        public Builder userDnAttribute(String userDnAttribute) {
            this.userDnAttribute = userDnAttribute;
            return this;
        }

        public Builder recursive(Boolean recursive) {
            this.recursive = recursive;
            return this;
        }

        public Builder cache(LdapCache cache) {
            this.cache = cache;
            return this;
        }

        public AdvancedFilter build() {
            if (baseDn == null) {
                throw new IllegalArgumentException("base-dn must be specified as non null value");
            }
            if (baseDn.isEmpty()) {
                throw new IllegalArgumentException("base-dn must not be empty value");
            }
            if (filter == null) {
                throw new IllegalArgumentException("filter must be specified as non null value");
            }
            if (filter.isEmpty()) {
                throw new IllegalArgumentException("filter must not be empty value");
            }
            return new AdvancedFilter(this);
        }
    }
}
