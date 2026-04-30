package org.wildfly.extras.creaper.commands.elytron.realm;

import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddPropertiesRealm implements OnlineCommand {

    private static final String REALM_TYPE = "properties-realm";

    private final String name;
    private final String groupsAttribute;
    private final Boolean plainText;
    private final String digestRealmName;
    private final String userProperiesPath;
    private final String userPropertiesRelativeTo;
    private final String groupsProperiesPath;
    private final String groupsPropertiesRelativeTo;
    private final boolean replaceExisting;

    private AddPropertiesRealm(Builder builder) {
        this.name = builder.name;
        this.groupsAttribute = builder.groupsAttribute;
        this.plainText = builder.plainText;
        this.digestRealmName = builder.digestRealmName;
        this.userProperiesPath = builder.userProperiesPath;
        this.userPropertiesRelativeTo = builder.userPropertiesRelativeTo;
        this.groupsProperiesPath = builder.groupsProperiesPath;
        this.groupsPropertiesRelativeTo = builder.groupsPropertiesRelativeTo;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address securityRealmAddress = Address.subsystem("elytron").and(REALM_TYPE, name);
        if (replaceExisting) {
            ops.removeIfExists(securityRealmAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        Values groupsProperties = groupsProperiesPath != null
                ? Values.empty()
                .and("path", groupsProperiesPath)
                .andOptional("relative-to", groupsPropertiesRelativeTo)
                : null;

        ops.add(securityRealmAddress, Values.empty()
                .andOptional("groups-attribute", groupsAttribute)
                .andObject("users-properties", Values.empty()
                        .and("path", userProperiesPath)
                        .andOptional("relative-to", userPropertiesRelativeTo)
                        .andOptional("plain-text", plainText)
                        .andOptional("digest-realm-name", digestRealmName))
                .andObjectOptional("groups-properties", groupsProperties));

        new Administration(ctx.client).reloadIfRequired();
    }

    public static final class Builder {

        private final String name;
        private String groupsAttribute;
        private Boolean plainText;
        private String digestRealmName;
        private String userProperiesPath;
        private String userPropertiesRelativeTo;
        private String groupsProperiesPath;
        private String groupsPropertiesRelativeTo;
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the properties-realm must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the properties-realm must not be empty value");
            }
            this.name = name;
        }

        public Builder groupsAttribute(String groupsAttribute) {
            this.groupsAttribute = groupsAttribute;
            return this;
        }

        public Builder plainText(Boolean plainText) {
            this.plainText = plainText;
            return this;
        }

        public Builder digestRealmName(String digestRealmName) {
            this.digestRealmName = digestRealmName;
            return this;
        }

        public Builder userProperiesPath(String userProperiesPath) {
            this.userProperiesPath = userProperiesPath;
            return this;
        }

        public Builder userPropertiesRelativeTo(String userPropertiesRelativeTo) {
            this.userPropertiesRelativeTo = userPropertiesRelativeTo;
            return this;
        }

        public Builder groupsProperiesPath(String groupsProperiesPath) {
            this.groupsProperiesPath = groupsProperiesPath;
            return this;
        }

        public Builder groupsPropertiesRelativeTo(String groupsPropertiesRelativeTo) {
            this.groupsPropertiesRelativeTo = groupsPropertiesRelativeTo;
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddPropertiesRealm build() {
            if (userProperiesPath == null || userProperiesPath.isEmpty()) {
                throw new IllegalArgumentException("Path to users-properties must not be null and must have a minimum length of 1 characters");
            }
            if ((groupsProperiesPath == null || groupsProperiesPath.isEmpty())
                    && groupsPropertiesRelativeTo != null) {
                throw new IllegalArgumentException("relative-to for groups-properties can be set only if path is specified");
            }
            return new AddPropertiesRealm(this);
        }
    }

}
