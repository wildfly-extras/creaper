package org.wildfly.extras.creaper.commands.security;

import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;

/**
 * Remove given mapping classic mapping module.
 */
public final class RemoveMappingModule implements OnlineCommand, OfflineCommand {

    private final String securityDomainName;
    private final String mappingModuleName;

    public RemoveMappingModule(String securityDomainName, String mappingModuleName) {
        if (securityDomainName == null) {
            throw new IllegalArgumentException("Name of the security-domain must be specified as non null value");
        }
        if (securityDomainName.isEmpty()) {
            throw new IllegalArgumentException("Name of the security-domain must not be empty value");
        }
        if (mappingModuleName == null) {
            throw new IllegalArgumentException("Name of the mapping-module must be specified as non null value");
        }
        if (mappingModuleName.isEmpty()) {
            throw new IllegalArgumentException("Name of the mapping-module must not be empty value");
        }
        this.securityDomainName = securityDomainName;
        this.mappingModuleName = mappingModuleName;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (ctx.version.greaterThanOrEqualTo(ServerVersion.VERSION_18_0_0)) {
            throw new AssertionError("Legacy security was removed in WildFly 25.");
        }

        Operations ops = new Operations(ctx.client);
        ops.remove(Address.subsystem("security")
                .and("security-domain", securityDomainName)
                .and("mapping", "classic")
                .and("mapping-module", mappingModuleName));
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        if (ctx.version.greaterThanOrEqualTo(ServerVersion.VERSION_18_0_0)) {
            throw new AssertionError("Legacy security was removed in WildFly 25.");
        }

        ctx.client.apply(GroovyXmlTransform.of(RemoveMappingModule.class)
                .subtree("securitySubsystem", Subtree.subsystem("security"))
                .parameter("atrSecurityDomainName", securityDomainName)
                .parameter("atrMappingModuleName", mappingModuleName)
                .build());
    }

}
