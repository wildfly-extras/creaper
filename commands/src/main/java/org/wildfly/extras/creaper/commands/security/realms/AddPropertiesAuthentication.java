package org.wildfly.extras.creaper.commands.security.realms;

import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

/**
 * Add properties authentication to security realm.
 */
public final class AddPropertiesAuthentication extends AbstractAddSecurityRealmSubElement {

    private final String path;
    private final String relativeTo;
    private final Boolean plainText;

    private AddPropertiesAuthentication(Builder builder) {
        super(builder);
        this.path = builder.path;
        this.relativeTo = builder.relativeTo;
        this.plainText = builder.plainText;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address securityRealmPropertiesAuthnAddress = securityRealmAddress.and("authentication", "properties");
        if (replaceExisting) {
            ops.removeIfExists(securityRealmPropertiesAuthnAddress);
            new Administration(ctx.client).reloadIfRequired();
        }
        ops.add(securityRealmPropertiesAuthnAddress, Values.empty()
                .andOptional("path", path)
                .andOptional("relative-to", relativeTo)
                .andOptional("plain-text", plainText));
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        ctx.client.apply(GroovyXmlTransform.of(AddPropertiesAuthentication.class)
                .subtree("management", Subtree.management())
                .parameter("atrSecurityRealmName", securityRealmName)
                .parameter("atrPath", path)
                .parameter("atrRelativeTo", relativeTo)
                .parameter("atrPlainText", plainText)
                .parameter("atrReplaceExisting", replaceExisting)
                .build());
    }

    public static final class Builder extends AbstractAddSecurityRealmSubElement.Builder<Builder> {

        private String path;
        private String relativeTo;
        private Boolean plainText;

        public Builder(String securityRealmName) {
            super(securityRealmName);
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder relativeTo(String relativeTo) {
            this.relativeTo = relativeTo;
            return this;
        }

        public Builder plainText(Boolean plainText) {
            this.plainText = plainText;
            return this;
        }

        @Override
        public AddPropertiesAuthentication build() {
            if (path == null) {
                throw new IllegalArgumentException("Path of the security domain must be specified as non null value");
            }
            if (path.isEmpty()) {
                throw new IllegalArgumentException("Path of the security domain must not be empty value");
            }
            return new AddPropertiesAuthentication(this);
        }
    }
}
