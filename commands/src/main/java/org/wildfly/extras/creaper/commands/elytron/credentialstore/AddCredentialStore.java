package org.wildfly.extras.creaper.commands.elytron.credentialstore;

import java.util.LinkedHashMap;
import java.util.Map;

import org.wildfly.extras.creaper.commands.elytron.CredentialRef;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddCredentialStore implements OnlineCommand {

    private final String name;
    private final String type;
    private final String providerName;
    private final String providers;
    private final String otherProviders;
    private final String relativeTo;
    private final CredentialRef credentialReference;
    private final Boolean create;
    private final Map<String, String> implementationProperties;
    private final String location;
    private final Boolean modifiable;
    private final boolean replaceExisting;

    private AddCredentialStore(Builder builder) {
        this.name = builder.name;
        this.type = builder.type;
        this.providerName = builder.providerName;
        this.providers = builder.providers;
        this.otherProviders = builder.otherProviders;
        this.relativeTo = builder.relativeTo;
        this.credentialReference = builder.credentialReference;
        this.create = builder.create;
        this.implementationProperties = builder.implementationProperties;
        this.location = builder.location;
        this.modifiable = builder.modifiable;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (ctx.version.lessThan(ServerVersion.VERSION_5_0_0)) {
            throw new AssertionError("Elytron is available since WildFly 11.");
        }

        Operations ops = new Operations(ctx.client);
        Address credentialStoreAddress = Address.subsystem("elytron").and("credential-store", name);
        if (replaceExisting) {
            ops.removeIfExists(credentialStoreAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(credentialStoreAddress, Values.empty()
                .andObject("credential-reference", credentialReference.toValues())
                .andOptional("type", type)
                .andOptional("provider-name", providerName)
                .andOptional("providers", providers)
                .andOptional("other-providers", otherProviders)
                .andOptional("relative-to", relativeTo)
                .andOptional("create", create)
                .andOptional("location", location)
                .andOptional("modifiable", modifiable)
                .andObjectOptional("implementation-properties", Values.fromMap(implementationProperties)));
    }

    public static final class Builder {

        private final String name;
        private String type;
        private String providerName;
        private String providers;
        private String otherProviders;
        private String relativeTo;
        private CredentialRef credentialReference;
        private Boolean create;
        private final Map<String, String> implementationProperties = new LinkedHashMap<String, String>();
        private String location;
        private Boolean modifiable;
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Name of the credential-store must be specified as non empty value");
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

        public Builder otherProviders(String otherProviders) {
            this.otherProviders = otherProviders;
            return this;
        }

        public Builder relativeTo(String relativeTo) {
            this.relativeTo = relativeTo;
            return this;
        }

        public Builder credentialReference(CredentialRef credentialReference) {
            this.credentialReference = credentialReference;
            return this;
        }

        public Builder create(boolean create) {
            this.create = create;
            return this;
        }

        public Builder addImplementationProperties(String name, String value) {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Name of the implementation-property of the credential-store must not be null");
            }
            if (value == null || value.isEmpty()) {
                throw new IllegalArgumentException("Value of the implementation-property of the credential-store must not be null");
            }
            implementationProperties.put(name, value);
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder modifiable(boolean modifiable) {
            this.modifiable = modifiable;
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddCredentialStore build() {
            if (credentialReference == null) {
                throw new IllegalArgumentException("Credential-reference of the credential-store must be specified");
            }

            return new AddCredentialStore(this);
        }
    }
}
