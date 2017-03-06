package org.wildfly.extras.creaper.commands.infinispan.cache;

import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Headers;
import org.wildfly.extras.creaper.core.online.operations.Operations;

/**
 * Removes an Infinispan cache.
 * Since WildFly 11 / EAP 7.1, this operation requires a reload, and this command
 * automatically appends the allow-resource-service-restart header.
 */
public final class RemoveCache implements OnlineCommand {
    private final Address address;

    public RemoveCache(String cacheContainer, CacheType cacheType, String cacheName) {
        if (cacheContainer == null) {
            throw new IllegalArgumentException("Cache container is required");
        }
        if (cacheType == null) {
            throw new IllegalArgumentException("Cache type is required");
        }
        if (cacheName == null) {
            throw new IllegalArgumentException("Cache name is required");
        }

        this.address = Address.subsystem("infinispan")
                .and("cache-container", cacheContainer)
                .and(cacheType.getType(), cacheName);
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        if (ctx.version.greaterThanOrEqualTo(ServerVersion.VERSION_5_0_0))
            ops = ops.headers(Headers.allowResourceServiceRestart());
        ops.remove(address);
    }
}
