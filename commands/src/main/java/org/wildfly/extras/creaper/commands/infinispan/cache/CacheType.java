package org.wildfly.extras.creaper.commands.infinispan.cache;

public enum CacheType {

    DISTRIBUTED_CACHE("distributed-cache"),
    INVALIDATION_CACHE("invalidation-cache"),
    LOCAL_CACHE("local-cache"),
    REPLICATED_CACHE("replicated-cache");

    CacheType(String type) {
        this.type = type;
    }

    private String type;

    public String getType() {
        return type;
    }

}
