package org.wildfly.extras.creaper.commands.elytron.credentialstore;

import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;

public final class RemoveCredentialStore implements OnlineCommand {

    private final String credentialStoreName;

    public RemoveCredentialStore(String credentialStoreName) {
        if (credentialStoreName == null) {
            throw new IllegalArgumentException("Name of the credential-store must be specified as non null value");
        }
        if (credentialStoreName.isEmpty()) {
            throw new IllegalArgumentException("Name of the credential-store must not be empty value");
        }

        this.credentialStoreName = credentialStoreName;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (ctx.version.lessThan(ServerVersion.VERSION_5_0_0)) {
            throw new AssertionError("Elytron is available since WildFly 11.");
        }

        Address credentialStoreAddress = Address.subsystem("elytron").and("credential-store", credentialStoreName);

        Operations ops = new Operations(ctx.client);
        ops.remove(credentialStoreAddress);
    }

}
