package org.wildfly.extras.creaper.commands.management;

import java.io.IOException;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

/**
 * <b>Only for tests!</b>
 */
public final class AddNativeManagementInterface implements OnlineCommand, OfflineCommand {

    private final String securityRealm;
    private final String socketBinding;
    private final boolean replaceExisting;

    private AddNativeManagementInterface(Builder builder) {
        this.securityRealm = builder.securityRealm;
        this.socketBinding = builder.socketBinding;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address interfaceAddress = Address.coreService("management").and("management-interface", "native-interface");
        if (this.replaceExisting) {
            try {
                ops.removeIfExists(interfaceAddress);
                new Administration(ctx.client).reloadIfRequired();
            } catch (OperationException e) {
                throw new IOException("Failed to remove existing native-interface.", e);
            }
        }
        ops.add(interfaceAddress,
                Values.of("security-realm", this.securityRealm).and("socket-binding", this.socketBinding));
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        ctx.client.apply(GroovyXmlTransform.of(AddNativeManagementInterface.class)
                .subtree("management", Subtree.management())
                .parameter("atrSecurityRealm", this.securityRealm)
                .parameter("atrSocketBinding", this.socketBinding)
                .parameter("atrReplaceExisting", this.replaceExisting)
                .build());
    }

    public static final class Builder {

        private final String securityRealm;
        private final String socketBinding;
        private boolean replaceExisting;

        public Builder(String securityRealm, String socketBinding) {
            if (securityRealm == null || securityRealm.isEmpty()) {
                throw new IllegalArgumentException("security-realm must be provided.");
            }
            if (socketBinding == null || socketBinding.isEmpty()) {
                throw new IllegalArgumentException("socket-binding must be provided.");
            }
            this.securityRealm = securityRealm;
            this.socketBinding = socketBinding;
        }

        /**
         * <b>This can cause server reload!</b>
         */
        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddNativeManagementInterface build() {
            return new AddNativeManagementInterface(this);
        }
    }
}
