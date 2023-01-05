package org.wildfly.extras.creaper.commands.security;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.CliException;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;

/**
 * Remove given security domain.
 */
public final class RemoveSecurityDomain implements OnlineCommand, OfflineCommand {

    private final String securityDomainName;

    public RemoveSecurityDomain(String securityDomainName) {
        if (securityDomainName == null) {
            throw new IllegalArgumentException("Name of the security-domain must be specified as non null value");
        }
        if (securityDomainName.isEmpty()) {
            throw new IllegalArgumentException("Name of the security-domain must not be empty value");
        }
        this.securityDomainName = securityDomainName;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws CliException, CommandFailedException, IOException,
            TimeoutException, InterruptedException {
        if (ctx.version.greaterThanOrEqualTo(ServerVersion.VERSION_18_0_0)) {
            throw new AssertionError("Legacy security was removed in WildFly 25.");
        }

        Operations ops = new Operations(ctx.client);
        ops.remove(Address.subsystem("security").and("security-domain", securityDomainName));
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {
        if (ctx.version.greaterThanOrEqualTo(ServerVersion.VERSION_18_0_0)) {
            throw new AssertionError("Legacy security was removed in WildFly 25.");
        }

        ctx.client.apply(GroovyXmlTransform.of(RemoveSecurityDomain.class)
                .subtree("securitySubsystem", Subtree.subsystem("security"))
                .parameter("atrSecurityDomainName", securityDomainName)
                .build());
    }

}
