package org.wildfly.extras.creaper.commands.infinispan.cache;

public enum CacheMode {
    SYNC("SYNC"),
    ASYNC("ASYNC"),
    ;

    CacheMode(String mode) {
        this.mode = mode;
    }

    private String mode;

    String getMode() {
        return mode;
    }
}
