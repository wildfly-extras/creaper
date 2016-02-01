package org.wildfly.extras.creaper.commands.security;

import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;

/**
 * Remove given authorization classic policy module.
 */
public final class RemoveAuthorizationModule implements OnlineCommand, OfflineCommand {

    private final String securityDomainName;
    private final String authorizationModuleName;

    public RemoveAuthorizationModule(String securityDomainName, String authorizationModuleName) {
        if (securityDomainName == null) {
            throw new IllegalArgumentException("Name of the security-domain must be specified as non null value");
        }
        if (securityDomainName.isEmpty()) {
            throw new IllegalArgumentException("Name of the security-domain must not be empty value");
        }
        if (authorizationModuleName == null) {
            throw new IllegalArgumentException("Name of the policy-module must be specified as non null value");
        }
        if (authorizationModuleName.isEmpty()) {
            throw new IllegalArgumentException("Name of the policy-module must not be empty value");
        }
        this.securityDomainName = securityDomainName;
        this.authorizationModuleName = authorizationModuleName;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        ops.remove(Address.subsystem("security")
                .and("security-domain", securityDomainName)
                .and("authorization", "classic")
                .and("policy-module", authorizationModuleName));
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        ctx.client.apply(GroovyXmlTransform.of(RemoveAuthorizationModule.class)
                .subtree("securitySubsystem", Subtree.subsystem("security"))
                .parameter("atrSecurityDomainName", securityDomainName)
                .parameter("atrAuthorizationModuleName", authorizationModuleName)
                .build());
    }

}
