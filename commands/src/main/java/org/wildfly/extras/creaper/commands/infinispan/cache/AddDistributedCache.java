package org.wildfly.extras.creaper.commands.infinispan.cache;

import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.operations.Values;

public final class AddDistributedCache extends AbstractAddCache {
    private final Long remoteTimeout;
    private final Double capacityFactor;
    private final Long l1lifespan;
    private final Integer owners;
    private final Integer segments;

    private AddDistributedCache(Builder builder) {
        super(builder, CacheType.DISTRIBUTED_CACHE);
        this.remoteTimeout = builder.remoteTimeout;
        this.capacityFactor = builder.capacityFactor;
        this.l1lifespan = builder.l1lifespan;
        this.owners = builder.owners;
        this.segments = builder.segments;
    }

    @Override
    protected Values addValuesSpecificForCacheType(Values generalCacheValues, ServerVersion version) {
        return generalCacheValues
                .andOptional("remote-timeout", remoteTimeout)
                .andOptional("capacity-factor", capacityFactor)
                .andOptional("l1-lifespan", l1lifespan)
                .andOptional("owners", owners)
                .andOptional("segments", segments);
    }

    public static final class Builder extends AbstractAddCache.Builder<Builder> {
        private Long remoteTimeout;
        private Double capacityFactor;
        private Long l1lifespan;
        private Integer owners;
        private Integer segments;

        public Builder(String name) {
            super(name);
        }

        public Builder remoteTimeout(Long remoteTimeout) {
            this.remoteTimeout = remoteTimeout;
            return this;
        }

        public Builder capacityFactor(Double capacityFactor) {
            this.capacityFactor = capacityFactor;
            return this;
        }

        public Builder l1lifespan(Long l1lifespan) {
            this.l1lifespan = l1lifespan;
            return this;
        }

        public Builder owners(Integer owners) {
            this.owners = owners;
            return this;
        }

        public Builder segments(Integer segments) {
            this.segments = segments;
            return this;
        }

        public AddDistributedCache build() {
            return new AddDistributedCache(this);
        }
    }
}
