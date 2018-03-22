package org.wildfly.extras.creaper.commands.elytron.credentialstore;

import java.io.IOException;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddCredentialStoreAlias implements OnlineCommand {

    private final String alias;
    private final String credentialStore;
    private final EntryType entryType;
    private final String secretValue;
    private final boolean replaceExisting;

    private AddCredentialStoreAlias(Builder builder) {
        this.alias = builder.alias;
        this.credentialStore = builder.credentialStore;
        this.entryType = builder.entryType;
        this.secretValue = builder.secretValue;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (ctx.version.lessThan(ServerVersion.VERSION_5_0_0)) {
            throw new AssertionError("Elytron is available since WildFly 11.");
        }

        Operations ops = new Operations(ctx.client);
        Address credentialStoreAddress = Address.subsystem("elytron")
                .and("credential-store", credentialStore);
        if (replaceExisting) {
            if (aliasExists(ops, credentialStoreAddress, alias)) {
                ops.invoke("remove-alias", credentialStoreAddress, Values.empty().and("alias", alias));
            }
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.invoke("add-alias", credentialStoreAddress, Values.empty()
                .and("secret-value", secretValue)
                .and("alias", alias)
                .andOptional("entry-type", entryType == null ? null : entryType.getEntryType()));
    }

    private boolean aliasExists(Operations ops, Address credentialStore, String alias) throws IOException {
        ModelNodeResult result = ops.invoke("read-aliases", credentialStore);
        ModelNode modelNode = result != null ? result.value() : null;
        if (result == null || !result.isSuccess() || modelNode.asList().isEmpty()) {
            return false;
        }
        List<ModelNode> aliasList = modelNode.asList();
        for (ModelNode aliasName : aliasList) {
            if (alias.equals(aliasName.asString())) {
                return true;
            }
        }

        return false;
    }

    public static final class Builder {

        private final String alias;
        private String credentialStore;
        private EntryType entryType;
        private String secretValue;
        private boolean replaceExisting;

        public Builder(String alias) {
            if (alias == null || alias.isEmpty()) {
                throw new IllegalArgumentException("Name of the kerberos-security-factory must be specified as non empty value");
            }
            this.alias = alias;
        }

        public Builder credentialStore(String credentialStore) {
            this.credentialStore = credentialStore;
            return this;
        }

        public Builder entryType(EntryType entryType) {
            this.entryType = entryType;
            return this;
        }

        public Builder secretValue(String secretValue) {
            this.secretValue = secretValue;
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddCredentialStoreAlias build() {
            if (credentialStore == null || credentialStore.isEmpty()) {
                throw new IllegalArgumentException("credential-store must be specified as non empty value");
            }
            if (secretValue == null || secretValue.isEmpty()) {
                throw new IllegalArgumentException("secret-value must be specified as non empty value");
            }

            return new AddCredentialStoreAlias(this);
        }
    }

    public static enum EntryType {

        OTHER("Other"), PASSWORD_CREDENTIAL("org.wildfly.security.credential.PasswordCredential");

        private final String entryTypeName;

        private EntryType(String entryTypeName) {
            this.entryTypeName = entryTypeName;
        }

        public String getEntryType() {
            return entryTypeName;
        }
    }
}
