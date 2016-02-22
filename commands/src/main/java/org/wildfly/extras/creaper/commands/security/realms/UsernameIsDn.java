package org.wildfly.extras.creaper.commands.security.realms;

/**
 * Helper class which holds information username-is-dn username to dn in ldap authorization of security realm.
 */
public final class UsernameIsDn {

    final Boolean force;
    final LdapCache cache;

    private UsernameIsDn(Builder builder) {
        this.force = builder.force;
        this.cache = builder.cache;
    }

    public static final class Builder {

        private Boolean force;
        private LdapCache cache;

        public Builder force(Boolean force) {
            this.force = force;
            return this;
        }

        public Builder cache(LdapCache cache) {
            this.cache = cache;
            return this;
        }

        public UsernameIsDn build() {
            return new UsernameIsDn(this);
        }
    }
}
