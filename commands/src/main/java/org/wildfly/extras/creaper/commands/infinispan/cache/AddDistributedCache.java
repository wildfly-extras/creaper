package org.wildfly.extras.creaper.commands.infinispan.cache;

import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.operations.Values;

public final class AddDistributedCache extends AbstractAddCache {
    private final Boolean asyncMarshalling;
    private final CacheMode mode;
    private final Long queueFlushInterval;
    private final Long remoteTimeout;
    private final Double capacityFactor;
    private final ConsistentHashStrategy consistentHashStrategy;
    private final Long l1lifespan;
    private final Integer owners;
    private final Integer segments;

    private AddDistributedCache(Builder builder) {
        super(builder, CacheType.DISTRIBUTED_CACHE);
        this.asyncMarshalling = builder.asyncMarshalling;
        this.mode = builder.mode;
        this.queueFlushInterval = builder.queueFlushInterval;
        this.remoteTimeout = builder.remoteTimeout;
        this.capacityFactor = builder.capacityFactor;
        this.consistentHashStrategy = builder.consistentHashStrategy;
        this.l1lifespan = builder.l1lifespan;
        this.owners = builder.owners;
        this.segments = builder.segments;
    }

    @Override
    protected Values addValuesSpecificForCacheType(Values generalCacheValues, ServerVersion version) {
        if (version.greaterThanOrEqualTo(ServerVersion.VERSION_3_0_0)) {  // WildFly 9+ only
            generalCacheValues = generalCacheValues
                    .andOptional("capacity-factor", capacityFactor)
                    .andOptional("consistent-hash-strategy",
                            consistentHashStrategy != null ? consistentHashStrategy.getType() : null);
        }

        return generalCacheValues
                .andOptional("async-marshalling", asyncMarshalling)
                .andOptional("mode", mode != null ? mode.getMode() : null)
                .andOptional("queue-flush-interval", queueFlushInterval)
                .andOptional("remote-timeout", remoteTimeout)
                .andOptional("l1-lifespan", l1lifespan)
                .andOptional("owners", owners)
                .andOptional("segments", segments);
    }

    public static final class Builder extends AbstractAddCache.Builder<Builder> {
        private Boolean asyncMarshalling;
        private CacheMode mode;
        private Long queueFlushInterval;
        private Long remoteTimeout;
        private Double capacityFactor;
        private ConsistentHashStrategy consistentHashStrategy;
        private Long l1lifespan;
        private Integer owners;
        private Integer segments;

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

        /**
         * {@code capacity-factor} is only supported on WildFly 9 and above,
         * this setting will be ignored on previous versions.
         */
        public Builder capacityFactor(Double capacityFactor) {
            this.capacityFactor = capacityFactor;
            return this;
        }

        /**
         * {@code consistent-hash-strategy} is only supported on WildFly 9 and above,
         * this setting will be ignored on previous versions.
         */
        public Builder consistentHashStrategy(ConsistentHashStrategy consistentHashStrategy) {
            this.consistentHashStrategy = consistentHashStrategy;
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
