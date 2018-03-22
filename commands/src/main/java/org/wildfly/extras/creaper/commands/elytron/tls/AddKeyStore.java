package org.wildfly.extras.creaper.commands.elytron.tls;

import org.wildfly.extras.creaper.commands.elytron.CredentialRef;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddKeyStore implements OnlineCommand, OfflineCommand {

    private final String name;
    private final String type;
    private final String providerName;
    private final String providers;
    private final CredentialRef credentialReference;
    private final String aliasFilter;
    private final String path;
    private final String relativeTo;
    private final Boolean required;
    private final boolean replaceExisting;

    private AddKeyStore(Builder builder) {
        this.name = builder.name;
        this.type = builder.type;
        this.providerName = builder.providerName;
        this.providers = builder.providers;
        this.credentialReference = builder.credentialReference;
        this.aliasFilter = builder.aliasFilter;
        // File
        this.path = builder.path;
        this.relativeTo = builder.relativeTo;
        this.required = builder.required;
        // Replace existing
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (ctx.version.lessThan(ServerVersion.VERSION_5_0_0)) {
            throw new AssertionError("Elytron is available since WildFly 11.");
        }

        Operations ops = new Operations(ctx.client);
        Address keyStoreAddress = Address.subsystem("elytron").and("key-store", name);
        if (replaceExisting) {
            ops.removeIfExists(keyStoreAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(keyStoreAddress, Values.empty()
            .and("name", name)
            .and("type", type)
            .andObject("credential-reference", credentialReference.toValues())
            .andOptional("provider-name", providerName)
            .andOptional("providers", providers)
            .andOptional("alias-filter", aliasFilter)
            .andOptional("path", path)
            .andOptional("relative-to", relativeTo)
            .andOptional("required", required));
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        if (ctx.version.lessThan(ServerVersion.VERSION_5_0_0)) {
            throw new AssertionError("Elytron is available since WildFly 11.");
        }

        ctx.client.apply(GroovyXmlTransform.of(AddKeyStore.class)
                .subtree("elytronSubsystem", Subtree.subsystem("elytron"))
                .parameter("atrName", name)
                .parameter("atrType", type)
                .parameters(credentialReference.toParameters())
                .parameter("atrProviderName", providerName)
                .parameter("atrProviders", providers)
                .parameter("atrAliasFilter", aliasFilter)
                .parameter("atrPath", path)
                .parameter("atrRelativeTo", relativeTo)
                .parameter("atrRequired", required)
                .parameter("atrReplaceExisting", replaceExisting)
                .build());
    }

    public static final class Builder {

        private final String name;
        private String type;
        private String providerName;
        private String providers;
        private CredentialRef credentialReference;
        private String aliasFilter;
        private String path;
        private String relativeTo;
        private Boolean required;
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Name of the key-store must be specified as non empty value");
            }
            this.name = name;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder providerName(String providerName) {
            this.providerName = providerName;
            return this;
        }

        public Builder providers(String providers) {
            this.providers = providers;
            return this;
        }

        public Builder credentialReference(CredentialRef credentialReference) {
            this.credentialReference = credentialReference;
            return this;
        }

        public Builder aliasFilter(String aliasFilter) {
            this.aliasFilter = aliasFilter;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder relativeTo(String relativeTo) {
            this.relativeTo = relativeTo;
            return this;
        }

        public Builder required(Boolean required) {
            this.required = required;
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddKeyStore build() {
            if (type == null || type.isEmpty()) {
                throw new IllegalArgumentException("Type of the key-store must be specified as non empty value");
            }
            if (credentialReference == null) {
                throw new IllegalArgumentException("Credential reference of the key-store must be specified");
            }
            return new AddKeyStore(this);
        }
    }
}
