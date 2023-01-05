package org.wildfly.extras.creaper.commands.security.realms;

import com.google.common.io.BaseEncoding;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;

public final class AddSecretAuthentication extends AbstractAddSecurityRealmSubElement {
    private final String password;

    protected AddSecretAuthentication(Builder builder) {
        super(builder);
        password = builder.password;
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        if (ctx.version.greaterThanOrEqualTo(ServerVersion.VERSION_18_0_0)) {
            throw new AssertionError("Legacy security was removed in WildFly 25.");
        }

        ctx.client.apply(GroovyXmlTransform.of(AddSecretAuthentication.class)
                .subtree("management", Subtree.management())
                .parameter("realmName", securityRealmName)
                .parameter("password", password)
                .parameter("replaceExisting", replaceExisting)
                .build());
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (ctx.version.greaterThanOrEqualTo(ServerVersion.VERSION_18_0_0)) {
            throw new AssertionError("Legacy security was removed in WildFly 25.");
        }

        Address secretServerIdentitiesAddress = securityRealmAddress.and("server-identity", "secret");

        Operations ops = new Operations(ctx.client);

        if (replaceExisting) {
            boolean secretServerIdentityExists = ops.exists(secretServerIdentitiesAddress);
            if (secretServerIdentityExists) {
                ops.remove(secretServerIdentitiesAddress);
            }
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(secretServerIdentitiesAddress, Values.empty()
                .and("value", password));
    }

    public static final class Builder extends AbstractAddSecurityRealmSubElement.Builder<Builder> {

        private String password;

        public Builder(String securityRealmName) {
            super(securityRealmName);
        }

        /**
         * This method will encode the provided {@code password} using Base64.
         * For the purpose of the Base64 encoding, the {@code password} is treated as a sequence of bytes in the
         * UTF-8 encoding.
         */
        public Builder password(String password) throws IOException {
            this.password = BaseEncoding.base64().encode(password.getBytes("UTF-8"));
            return this;
        }

        /**
         * This method expects the provided {@code password} to be already encoded using Base64.
         */
        public Builder passwordBase64(String password) throws IOException {
            this.password = password;
            return this;
        }

        @Override
        public AddSecretAuthentication build() {
            if (password == null) {
                throw new IllegalArgumentException("Password must be specified");
            }
            return new AddSecretAuthentication(this);
        }
    }
}
