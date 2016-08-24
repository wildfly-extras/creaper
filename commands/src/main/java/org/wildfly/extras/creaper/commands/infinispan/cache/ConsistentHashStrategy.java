package org.wildfly.extras.creaper.commands.infinispan.cache;

public enum ConsistentHashStrategy {
    INTER_CACHE("INTER_CACHE"),
    INTRA_CACHE("INTRA_CACHE"),
    ;

    ConsistentHashStrategy(String type) {
        this.type = type;
    }

    private String type;

    String getType() {
        return type;
    }
}
