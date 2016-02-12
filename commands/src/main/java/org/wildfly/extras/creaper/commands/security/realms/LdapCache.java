package org.wildfly.extras.creaper.commands.security.realms;

/**
 * Helper class which holds information about Ldap Cache.
 */
public final class LdapCache {

    private final Boolean cacheFailures;
    private final Integer evictionTime;
    private final Integer maxCacheSize;
    private final boolean byAccessTime;
    private final boolean bySearchTime;

    private LdapCache(Builder builder) {
        this.cacheFailures = builder.cacheFailures;
        this.evictionTime = builder.evictionTime;
        this.maxCacheSize = builder.maxCacheSize;
        this.byAccessTime = builder.byAccessTime;
        this.bySearchTime = builder.bySearchTime;
    }

    public Boolean getCacheFailures() {
        return cacheFailures;
    }

    public Integer getEvictionTime() {
        return evictionTime;
    }

    public Integer getMaxCacheSize() {
        return maxCacheSize;
    }

    public boolean getByAccessTime() {
        return byAccessTime;
    }

    public boolean getBySearchTime() {
        return bySearchTime;
    }

    public static final class Builder {

        private Boolean cacheFailures;
        private Integer evictionTime;
        private Integer maxCacheSize;
        private boolean byAccessTime = false;
        private boolean bySearchTime = false;

        public Builder cacheFailures(Boolean cacheFailures) {
            this.cacheFailures = cacheFailures;
            return this;
        }

        public Builder evictionTime(Integer evictionTime) {
            this.evictionTime = evictionTime;
            return this;
        }

        public Builder maxCacheSize(Integer maxCacheSize) {
            this.maxCacheSize = maxCacheSize;
            return this;
        }

        public Builder byAccessTime() {
            byAccessTime = true;
            return this;
        }

        public Builder bySearchTime() {
            bySearchTime = true;
            return this;
        }

        public LdapCache build() {
            if (byAccessTime == true && bySearchTime == true) {
                throw new IllegalArgumentException("Only one of by-access-time and by-search-time can be set for cache.");
            }
            if (byAccessTime == false && bySearchTime == false) {
                throw new IllegalArgumentException("One of by-access-time or by-search-time must be set for cache.");
            }
            return new LdapCache(this);
        }
    }

}
