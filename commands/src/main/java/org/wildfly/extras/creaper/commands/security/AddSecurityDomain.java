package org.wildfly.extras.creaper.commands.security;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.CliException;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

/**
 * Add a new security domain.
 */
public class AddSecurityDomain implements OnlineCommand, OfflineCommand {

    private final String securityDomainName;
    private final String cacheType;
    private final boolean replaceExisting;

    private AddSecurityDomain(Builder builder) {
        this.securityDomainName = builder.securityDomainName;
        this.cacheType = builder.cacheType;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws CliException, CommandFailedException, IOException,
            TimeoutException, InterruptedException {
        Operations ops = new Operations(ctx.client);
        Address securityDomainAddress = Address.subsystem("security").and("security-domain", securityDomainName);
        if (replaceExisting) {
            try {
                ops.removeIfExists(securityDomainAddress);
                new Administration(ctx.client).reloadIfRequired();
            } catch (OperationException e) {
                throw new IOException("Failed to remove existing security domain " + securityDomainName, e);
            }
        }
        ops.add(securityDomainAddress, Values.empty()
                .andOptional("cache-type", cacheType));
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {
        ctx.client.apply(GroovyXmlTransform.of(AddSecurityDomain.class)
                .subtree("securitySubsystem", Subtree.subsystem("security"))
                .parameter("atrSecurityDomainName", securityDomainName)
                .parameter("atrCacheType", cacheType)
                .parameter("atrReplaceExisting", replaceExisting)
                .build());
    }

    public static final class Builder {

        private String securityDomainName;
        private String cacheType;
        private boolean replaceExisting;

        public Builder(String securityDomainName) {
            if (securityDomainName == null) {
                throw new IllegalArgumentException("Name of the security-domain must be specified as non null value");
            }
            if (securityDomainName.isEmpty()) {
                throw new IllegalArgumentException("Name of the security-domain must not be empty value");
            }
            this.securityDomainName = securityDomainName;
        }

        public Builder cacheType(String cacheType) {
            this.cacheType = cacheType;
            return this;
        }

        /**
         * <b>This can cause server reload!</b>
         */
        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddSecurityDomain build() {
            return new AddSecurityDomain(this);
        }

    }
}
