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
public final class AddHttpManagementInterface implements OnlineCommand, OfflineCommand {

    private final String securityRealm;
    private final String socketBinding;
    private final String secureSocketBinding;
    private final Boolean httpUpgradeEnabled;
    private final boolean replaceExisting;

    private AddHttpManagementInterface(Builder builder) {
        this.securityRealm = builder.securityRealm;
        this.socketBinding = builder.socketBinding;
        this.secureSocketBinding = builder.secureSocketBinding;
        this.httpUpgradeEnabled = builder.httpUpgradeEnabled;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address interfaceAddress = Address.coreService("management").and("management-interface", "http-interface");
        if (this.replaceExisting) {
            try {
                ops.removeIfExists(interfaceAddress);
                new Administration(ctx.client).reloadIfRequired();
            } catch (OperationException e) {
                throw new IOException("Failed to remove existing http-interface.", e);
            }
        }
        ops.add(interfaceAddress, Values.of("security-realm", this.securityRealm)
                .andOptional("socket-binding", this.socketBinding)
                .andOptional("secure-socket-binding", this.secureSocketBinding)
                .andOptional("http-upgrade-enabled", this.httpUpgradeEnabled));
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        ctx.client.apply(GroovyXmlTransform.of(AddHttpManagementInterface.class)
                .subtree("management", Subtree.management())
                .parameter("atrSecurityRealm", this.securityRealm)
                .parameter("atrSocketBinding", this.socketBinding)
                .parameter("atrSecureSocketBinding", this.secureSocketBinding)
                .parameter("atrHttpUpgradeEnabled", this.httpUpgradeEnabled)
                .parameter("atrReplaceExisting", this.replaceExisting)
                .build());
    }

    public static final class Builder {

        private final String securityRealm;
        private String socketBinding;
        private String secureSocketBinding;
        private Boolean httpUpgradeEnabled;
        private boolean replaceExisting;

        public Builder(String securityRealm) {
            if (securityRealm == null || securityRealm.isEmpty()) {
                throw new IllegalArgumentException("security-realm must be provided.");
            }
            this.securityRealm = securityRealm;
        }

        public Builder socketBinding(String socketBinding) {
            if (socketBinding == null || socketBinding.isEmpty()) {
                throw new IllegalArgumentException("socket-binding must be provided non-empty.");
            }
            this.socketBinding = socketBinding;
            return this;
        }

        public Builder secureSocketBinding(String secureSocketBinding) {
            if (secureSocketBinding == null || secureSocketBinding.isEmpty()) {
                throw new IllegalArgumentException("secure-socket-binding must be provided non-empty.");
            }
            this.secureSocketBinding = secureSocketBinding;
            return this;
        }

        public Builder httpUpgradeEnabled(Boolean httpUpgradeEnabled) {
            this.httpUpgradeEnabled = httpUpgradeEnabled;
            return this;
        }

        /**
         * <b>This can cause server reload!</b>
         */
        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddHttpManagementInterface build() {
            if (this.socketBinding == null && this.secureSocketBinding == null) {
                throw new IllegalArgumentException("(secure-)socket-binding must be provided.");
            }
            return new AddHttpManagementInterface(this);
        }
    }
}
