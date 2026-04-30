package org.wildfly.extras.creaper.commands.elytron.securityproperty;

import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;

public final class AddSecurityProperty implements OnlineCommand {

    private final String key;
    private final String value;

    private AddSecurityProperty(Builder builder) {
        this.key = builder.key;
        this.value = builder.value;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address securityPropertyAddress = Address.subsystem("elytron");
        ops.writeAttribute(securityPropertyAddress, "security-properties." + key, value);
    }

    public static final class Builder {

        private final String key;
        private String value;

        public Builder(String key) {
            if (key == null) {
                throw new IllegalArgumentException("Key of the security-property must be specified as non null value");
            }
            if (key.isEmpty()) {
                throw new IllegalArgumentException("Key of the security-property must not be empty value");
            }
            this.key = key;
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public AddSecurityProperty build() {
            if (value == null || value.isEmpty()) {
                throw new IllegalArgumentException("Value must not be null or empty");
            }

            return new AddSecurityProperty(this);
        }
    }
}
