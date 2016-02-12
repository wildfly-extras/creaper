package org.wildfly.extras.creaper.commands.security.realms;

import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.operations.Address;

/**
 * Abstract class for adding subelement part (authentication, authorization etc.) of security realms.
 */
abstract class AbstractAddSecurityRealmSubElement implements OnlineCommand, OfflineCommand {

    protected final String securityRealmName;
    protected final boolean replaceExisting;
    protected final Address securityRealmAddress;

    protected AbstractAddSecurityRealmSubElement(Builder builder) {
        this.securityRealmName = builder.securityRealmName;
        this.replaceExisting = builder.replaceExisting;

        securityRealmAddress = Address.coreService("management")
                .and("security-realm", securityRealmName);
    }

    /**
     * Builder for configuration attributes of particular security realm subelement. The {@code THIS} type parameter is
     * only meant to be used by subclasses.
     */
    abstract static class Builder<THIS extends Builder> {

        private String securityRealmName;
        private boolean replaceExisting;

        public Builder(String securityRealmName) {
            if (securityRealmName == null) {
                throw new IllegalArgumentException("Name of security realm must be specified as non null value");
            }
            if (securityRealmName.isEmpty()) {
                throw new IllegalArgumentException("Name of security realm must not be empty value");
            }
            this.securityRealmName = securityRealmName;
        }

        /**
         * <b>This can cause server reload!</b>
         */
        public final THIS replaceExisting() {
            this.replaceExisting = true;
            return (THIS) this;
        }

        public abstract AbstractAddSecurityRealmSubElement build();
    }
}
