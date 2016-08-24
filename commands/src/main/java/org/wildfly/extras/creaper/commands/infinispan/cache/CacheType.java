package org.wildfly.extras.creaper.commands.infinispan.cache;

public enum CacheType {
    LOCAL_CACHE("local-cache"),
    REPLICATED_CACHE("replicated-cache"),
    INVALIDATION_CACHE("invalidation-cache"),
    DISTRIBUTED_CACHE("distributed-cache"),
    ;

    CacheType(String type) {
        this.type = type;
    }

    private String type;

    String getType() {
        return type;
    }
}
