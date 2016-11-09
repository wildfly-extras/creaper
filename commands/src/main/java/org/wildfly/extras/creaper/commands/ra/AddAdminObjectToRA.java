package org.wildfly.extras.creaper.commands.ra;

import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Batch;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.util.HashMap;
import java.util.Map;

/**
 * Adds admin object to existing resource adapter
 */
public final class AddAdminObjectToRA implements OnlineCommand {
    private final String poolName;
    private final String localJndiName;
    private final String className;
    private final String resourceAdapterId;
    private final boolean useJavaContext;
    private final Map<String, String> properties;

    private AddAdminObjectToRA(Builder builder) {
        this.poolName = builder.poolName;
        this.localJndiName = builder.localJndiName;
        this.className = builder.className;
        this.resourceAdapterId = builder.resourceAdapterId;
        this.useJavaContext = builder.useJavaContext;
        this.properties = builder.properties;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Batch batch = new Batch();
        Address address = Address.subsystem("resource-adapters").and("resource-adapter", resourceAdapterId)
                .and("admin-objects", poolName);
        batch.add(address, Values.of("class-name", className)
                .and("jndi-name", localJndiName).and("use-java-context", useJavaContext));
        for (Map.Entry<String, String> pair : properties.entrySet()) {
            Address configPropsAddress = Address.subsystem("resource-adapters")
                    .and("resource-adapter", resourceAdapterId)
                    .and("admin-objects", poolName).and("config-properties", pair.getKey());
            batch.add(configPropsAddress, Values.of("value", pair.getValue()));
        }
        new Operations(ctx.client).batch(batch);
    }

    public static final class Builder {
        private final String poolName;
        private final String localJndiName;
        private final String resourceAdapterId;
        private final Map<String, String> properties = new HashMap<String, String>();
        private String className;
        private boolean useJavaContext;

        public Builder(String poolName, String localJndiName, String resourceAdapterId) {
            if (poolName == null) {
                throw new IllegalArgumentException("poolName must be specified");
            }
            if (localJndiName == null) {
                throw new IllegalArgumentException("localJndiName must be specified");
            }

            if (resourceAdapterId == null) {
                throw new IllegalArgumentException("resourceAdapterId must be specified");
            }
            this.poolName = poolName;
            this.localJndiName = localJndiName;
            this.resourceAdapterId = resourceAdapterId;
        }

        /**
         * Other properties depending on particular resource adapter requirements
         *
         * @param name  name of property
         * @param value value
         */
        public Builder addProperty(String name, String value) {
            properties.put(name, value);
            return this;
        }

        public Builder setUseJavaContext(boolean useJavaContext) {
            this.useJavaContext = useJavaContext;
            return this;
        }

        public Builder setClassName(String className) {
            this.className = className;
            return this;
        }

        public AddAdminObjectToRA build() {
            if (className == null) {
                throw new IllegalArgumentException("className must be specified");
            }
            return new AddAdminObjectToRA(this);
        }
    }

    @Override
    public String toString() {
        return "AddAdminObjectToRA " + poolName;
    }
}
