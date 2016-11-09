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
 * Adds new connection factory to existing resource adapter
 */
public final class AddConnectionFactoryToRA implements OnlineCommand {
    private final String poolName;
    private final String localJndiName;
    private final String resourceAdapterId;
    private final String userName;
    private final String password;
    private final Map<String, String> properties;
    private final boolean security;
    private final boolean tracking;

    private AddConnectionFactoryToRA(Builder builder) {
        this.poolName = builder.poolName;
        this.localJndiName = builder.localJndiName;
        this.resourceAdapterId = builder.resourceAdapterId;
        this.userName = builder.userName;
        this.password = builder.password;
        this.security = builder.security;
        this.properties = builder.properties;
        this.tracking = builder.tracking;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Batch batch = new Batch();
        Address address = Address.subsystem("resource-adapters").and("resource-adapter", resourceAdapterId)
                .and("connection-definitions", poolName);
        batch.add(address, Values.of("class-name", "org.jboss.resource.adapter.jms.JmsManagedConnectionFactory")
                .and("jndi-name", localJndiName));
        if (security) {
            batch.writeAttribute(address, "security-application", true);
        }
        if (userName != null) {
            batch.writeAttribute(address, "recovery-username", userName);
        }
        if (password != null) {
            batch.writeAttribute(address, "recovery-password", password);
        }
        batch.writeAttribute(address, "tracking", tracking);
        for (Map.Entry<String, String> pair : properties.entrySet()) {
            Address configPropsAddress = Address.subsystem("resource-adapters")
                    .and("resource-adapter", resourceAdapterId)
                    .and("connection-definitions", poolName).and("config-properties", pair.getKey());
            batch.add(configPropsAddress, Values.of("value", pair.getValue()));
        }
        new Operations(ctx.client).batch(batch);
    }

    public static final class Builder {
        private final String poolName;
        private final String localJndiName;
        private final String resourceAdapterId;
        private String userName;
        private String password;
        private boolean security = false;
        private boolean tracking = true;
        private Map<String, String> properties = new HashMap<String, String>();

        /**
         * @param poolName          unique id for connection factory (not used for jndi lookup)
         * @param localJndiName     jndi name for connection factory used for jndi lookup from apps
         * @param resourceAdapterId id of the resource adapter where this connection factory will be added
         */
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
         * XA recovery credentials
         *
         * @param userName user name
         * @param password password
         */
        public Builder addXARecovery(String userName, String password) {
            this.userName = userName;
            this.password = password;
            return this;
        }

        /**
         * @param applicationSecurity sets security element if true
         */
        public Builder setApplicationSecurity(boolean applicationSecurity) {
            this.security = applicationSecurity;
            return this;
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

        /**
         * Defines whether IronJacamar should track connection handles across transaction boundaries
         *
         * @param tracking default is true
         */
        public Builder setTracking(boolean tracking) {
            this.tracking = tracking;
            return this;
        }

        public AddConnectionFactoryToRA build() {
            return new AddConnectionFactoryToRA(this);
        }
    }

    @Override
    public String toString() {
        return "AddConnectionFactoryToRA " + poolName;
    }
}
