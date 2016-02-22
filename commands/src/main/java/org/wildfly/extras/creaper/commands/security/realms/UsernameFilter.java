package org.wildfly.extras.creaper.commands.security.realms;

/**
 * Helper class which holds information username-filter username to dn in ldap authorization of security realm.
 */
public final class UsernameFilter {

    final String attribute;
    final String baseDn;
    final Boolean force;
    final Boolean recursive;
    final String userDnAttribute;
    final LdapCache cache;

    private UsernameFilter(Builder builder) {
        this.baseDn = builder.baseDn;
        this.attribute = builder.attribute;
        this.recursive = builder.recursive;
        this.userDnAttribute = builder.userDnAttribute;
        this.force = builder.force;
        this.cache = builder.cache;
    }

    public static final class Builder {

        private String attribute;
        private String baseDn;
        private Boolean force;
        private Boolean recursive;
        private String userDnAttribute;
        private LdapCache cache;

        public Builder attribute(String attribute) {
            this.attribute = attribute;
            return this;
        }

        public Builder baseDn(String baseDn) {
            this.baseDn = baseDn;
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

        public UsernameFilter build() {
            if (baseDn == null) {
                throw new IllegalArgumentException("base-dn must be specified as non null value");
            }
            if (baseDn.isEmpty()) {
                throw new IllegalArgumentException("base-dn must not be empty value");
            }
            if (attribute == null) {
                throw new IllegalArgumentException("attribute must be specified as non null value");
            }
            if (attribute.isEmpty()) {
                throw new IllegalArgumentException("attribute must not be empty value");
            }
            return new UsernameFilter(this);
        }
    }

}
