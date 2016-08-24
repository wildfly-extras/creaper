package org.wildfly.extras.creaper.commands.infinispan.cache;

import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;

public final class RemoveCache implements OnlineCommand {

    private final String cacheContainer;
    private final CacheType cacheType;
    private final String cacheName;

    private final Address address;

    public RemoveCache(String cacheContainer,
                       CacheType cacheType, String cacheName) {
        if (cacheContainer == null) {
            throw new IllegalArgumentException("Cache container is required");
        }
        if (cacheType == null) {
            throw new IllegalArgumentException("Cache type is required");
        }
        if (cacheName == null) {
            throw new IllegalArgumentException("Cache name is required");
        }
        this.cacheContainer = cacheContainer;
        this.cacheType = cacheType;
        this.cacheName = cacheName;
        address = Address.subsystem("infinispan")
                .and("cache-container", cacheContainer)
                .and(cacheType.getType(), cacheName);
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        ops.remove(address);
    }

}
