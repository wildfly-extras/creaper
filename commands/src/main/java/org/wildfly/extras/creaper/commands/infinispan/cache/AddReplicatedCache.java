package org.wildfly.extras.creaper.commands.infinispan.cache;

import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.operations.Values;

public final class AddReplicatedCache extends AbstractAddCache {
    private final Long remoteTimeout;

    private AddReplicatedCache(Builder builder) {
        super(builder, CacheType.REPLICATED_CACHE);
        this.remoteTimeout = builder.remoteTimeout;
    }

    @Override
    protected Values addValuesSpecificForCacheType(Values generalCacheValues, ServerVersion version) {
        return generalCacheValues
                .andOptional("remote-timeout", remoteTimeout);
    }

    public static final class Builder extends AbstractAddCache.Builder<Builder> {
        private Long remoteTimeout;

        public Builder(String name) {
            super(name);
        }

        public Builder remoteTimeout(Long remoteTimeout) {
            this.remoteTimeout = remoteTimeout;
            return this;
        }

        public AddReplicatedCache build() {
            return new AddReplicatedCache(this);
        }
    }
}
