package org.wildfly.extras.creaper.commands.infinispan.cache;

import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.operations.Values;

public final class AddReplicatedCache extends AbstractAddCache {
    private final Boolean asyncMarshalling;
    private final CacheMode mode;
    private final Long queueFlushInterval;
    private final Long remoteTimeout;

    private AddReplicatedCache(Builder builder) {
        super(builder, CacheType.REPLICATED_CACHE);
        this.asyncMarshalling = builder.asyncMarshalling;
        this.mode = builder.mode;
        this.queueFlushInterval = builder.queueFlushInterval;
        this.remoteTimeout = builder.remoteTimeout;
    }

    @Override
    protected Values addValuesSpecificForCacheType(Values generalCacheValues, ServerVersion version) {
        return generalCacheValues
                .andOptional("async-marshalling", asyncMarshalling)
                .andOptional("mode", mode != null ? mode.getMode() : null)
                .andOptional("queue-flush-interval", queueFlushInterval)
                .andOptional("remote-timeout", remoteTimeout);
    }

    public static final class Builder extends AbstractAddCache.Builder<Builder> {
        private Boolean asyncMarshalling;
        private CacheMode mode;
        private Long queueFlushInterval;
        private Long remoteTimeout;

        public Builder(String name) {
            super(name);
        }

        public Builder asyncMarshalling(Boolean asyncMarshalling) {
            this.asyncMarshalling = asyncMarshalling;
            return this;
        }

        public Builder mode(CacheMode mode) {
            this.mode = mode;
            return this;
        }

        public Builder queueFlushInterval(Long queueFlushInterval) {
            this.queueFlushInterval = queueFlushInterval;
            return this;
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
