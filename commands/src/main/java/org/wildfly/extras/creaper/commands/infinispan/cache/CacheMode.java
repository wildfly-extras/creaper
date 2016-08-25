package org.wildfly.extras.creaper.commands.infinispan.cache;

public enum CacheMode {

    SYNC("SYNC"),
    ASYNC("ASYNC");

    CacheMode(String mode) {
        this.mode = mode;
    }

    private String mode;

    public String getMode() {
        return mode;
    }

}
