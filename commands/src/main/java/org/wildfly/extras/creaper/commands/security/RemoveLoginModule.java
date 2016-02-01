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
 * Remove given authentication classic login module.
 */
public final class RemoveLoginModule implements OnlineCommand, OfflineCommand {

    private final String securityDomainName;
    private final String loginModuleName;

    public RemoveLoginModule(String securityDomainName, String loginModuleName) {
        if (securityDomainName == null) {
            throw new IllegalArgumentException("Name of the security-domain must be specified as non null value");
        }
        if (securityDomainName.isEmpty()) {
            throw new IllegalArgumentException("Name of the security-domain must not be empty value");
        }
        if (loginModuleName == null) {
            throw new IllegalArgumentException("Name of the login-module must be specified as non null value");
        }
        if (loginModuleName.isEmpty()) {
            throw new IllegalArgumentException("Name of the login-module must not be empty value");
        }
        this.securityDomainName = securityDomainName;
        this.loginModuleName = loginModuleName;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        ops.remove(Address.subsystem("security")
                .and("security-domain", securityDomainName)
                .and("authentication", "classic")
                .and("login-module", loginModuleName));
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        ctx.client.apply(GroovyXmlTransform.of(RemoveLoginModule.class)
                .subtree("securitySubsystem", Subtree.subsystem("security"))
                .parameter("atrSecurityDomainName", securityDomainName)
                .parameter("atrLoginModuleName", loginModuleName)
                .build());
    }

}
