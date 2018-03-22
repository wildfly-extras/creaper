package org.wildfly.extras.creaper.commands.elytron.tls;

import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddClientSSLContext extends AbstractAddSSLContext {

    private AddClientSSLContext(Builder builder) {
        super(builder);
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (ctx.version.lessThan(ServerVersion.VERSION_5_0_0)) {
            throw new AssertionError("Elytron is available since WildFly 11.");
        }

        Operations ops = new Operations(ctx.client);
        Address clientSSLContextAddress = Address.subsystem("elytron").and("client-ssl-context", name);
        if (replaceExisting) {
            ops.removeIfExists(clientSSLContextAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(clientSSLContextAddress, Values.empty()
                .andOptional("cipher-suite-filter", cipherSuiteFilter)
                .andOptional("key-manager", keyManager)
                .andOptional("trust-manager", trustManager)
                .andListOptional(String.class, "protocols", protocols));
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        if (ctx.version.lessThan(ServerVersion.VERSION_5_0_0)) {
            throw new AssertionError("Elytron is available since WildFly 11.");
        }

        ctx.client.apply(GroovyXmlTransform.of(AddClientSSLContext.class)
                .subtree("elytronSubsystem", Subtree.subsystem("elytron"))
                .parameter("atrName", name)
                .parameter("atrCipherSuiteFilter", cipherSuiteFilter)
                .parameter("atrKeyManager", keyManager)
                .parameter("atrTrustManager", trustManager)
                .parameter("atrProtocols", protocols != null ? joinList(protocols) : null)
                .parameter("atrProviders", providers)
                .parameter("atrProviderName", providerName)
                .parameter("atrReplaceExisting", replaceExisting)
                .build());
    }

    public static final class Builder extends AbstractAddSSLContext.Builder<Builder> {

        public Builder(String name) {
            super(name);
        }

        @Override
        public AddClientSSLContext build() {
            return new AddClientSSLContext(this);
        }

    }

}
