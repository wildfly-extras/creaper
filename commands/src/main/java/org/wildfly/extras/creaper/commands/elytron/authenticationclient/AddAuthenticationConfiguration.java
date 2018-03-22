package org.wildfly.extras.creaper.commands.elytron.authenticationclient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.wildfly.extras.creaper.commands.elytron.CredentialRef;
import org.wildfly.extras.creaper.commands.elytron.Property;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddAuthenticationConfiguration implements OnlineCommand {

    private final String name;
    private final List<Property> mechanismProperties;
    private final CredentialRef credentialReference;
    private final Boolean anonymous;
    private final String authenticationName;
    private final String authorizationName;
    private final String extend;
    private final String host;
    private final Integer port;
    private final String protocol;
    private final String realm;
    private final String securityDomain;
    private final String saslMechanismSelector;
    private final String kerberosSecurityFactory;
    private final ForwardingMode forwardingMode;
    private final boolean replaceExisting;

    private AddAuthenticationConfiguration(Builder builder) {
        this.name = builder.name;
        this.mechanismProperties = builder.mechanismProperties;
        this.credentialReference = builder.credentialReference;
        this.anonymous = builder.anonymous;
        this.authenticationName = builder.authenticationName;
        this.authorizationName = builder.authorizationName;
        this.extend = builder.extend;
        this.host = builder.host;
        this.port = builder.port;
        this.protocol = builder.protocol;
        this.realm = builder.realm;
        this.securityDomain = builder.securityDomain;
        this.saslMechanismSelector = builder.saslMechanismSelector;
        this.kerberosSecurityFactory = builder.kerberosSecurityFactory;
        this.forwardingMode = builder.forwardingMode;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (ctx.version.lessThan(ServerVersion.VERSION_5_0_0)) {
            throw new AssertionError("Elytron is available since WildFly 11.");
        }

        Operations ops = new Operations(ctx.client);
        Address realmAddress = Address.subsystem("elytron").and("authentication-configuration", name);
        if (replaceExisting) {
            ops.removeIfExists(realmAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ModelNode mechanismPropertiesNode = null;
        if (mechanismProperties != null && !mechanismProperties.isEmpty()) {
            mechanismPropertiesNode = new ModelNode();
            for (Property property : mechanismProperties) {
                mechanismPropertiesNode.add(property.getKey(), property.getValue());
            }
            mechanismPropertiesNode = mechanismPropertiesNode.asObject();
        }

        Values credentialReferenceValues = credentialReference != null ? credentialReference.toValues() : null;
        String forwardingModeValue = forwardingMode == null ? null : forwardingMode.name().toLowerCase();

        ops.add(realmAddress, Values.empty()
                .andOptional("extends", extend)
                .andOptional("anonymous", anonymous)
                .andOptional("authentication-name", authenticationName)
                .andOptional("authorization-name", authorizationName)
                .andOptional("host", host)
                .andOptional("protocol", protocol)
                .andOptional("port", port)
                .andOptional("realm", realm)
                .andOptional("security-domain", securityDomain)
                .andOptional("mechanism-properties", mechanismPropertiesNode)
                .andOptional("sasl-mechanism-selector", saslMechanismSelector)
                .andOptional("kerberos-security-factory", kerberosSecurityFactory)
                .andOptional("forwarding-mode", forwardingModeValue)
                .andObjectOptional("credential-reference", credentialReferenceValues));
    }

    public static final class Builder {

        private String name;
        private List<Property> mechanismProperties = new ArrayList<Property>();
        private CredentialRef credentialReference;
        private Boolean anonymous;
        private String authenticationName;
        private String authorizationName;
        private String extend;
        private String host;
        private Integer port;
        private String protocol;
        private String realm;
        private String securityDomain;
        private String saslMechanismSelector;
        private String kerberosSecurityFactory;
        private ForwardingMode forwardingMode;
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the authentication-configuration must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the authentication-configuration must not be empty value");
            }
            this.name = name;
        }

        public Builder addMechanismProperties(Property... mechanismProperties) {
            if (mechanismProperties == null) {
                throw new IllegalArgumentException("MechanismProperties added to authentication-configuration must not be null");
            }
            Collections.addAll(this.mechanismProperties, mechanismProperties);
            return this;
        }

        public Builder credentialReference(CredentialRef credentialReference) {
            this.credentialReference = credentialReference;
            return this;
        }

        public Builder anonymous(Boolean anonymous) {
            this.anonymous = anonymous;
            return this;
        }

        public Builder authenticationName(String authenticationName) {
            this.authenticationName = authenticationName;
            return this;
        }

        public Builder authorizationName(String authorizationName) {
            this.authorizationName = authorizationName;
            return this;
        }

        public Builder extend(String extend) {
            this.extend = extend;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(Integer port) {
            this.port = port;
            return this;
        }

        public Builder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder realm(String realm) {
            this.realm = realm;
            return this;
        }

        public Builder securityDomain(String securityDomain) {
            this.securityDomain = securityDomain;
            return this;
        }

        public Builder saslMechanismSelector(String saslMechanismSelector) {
            this.saslMechanismSelector = saslMechanismSelector;
            return this;
        }

        public Builder kerberosSecurityFactory(String kerberosSecurityFactory) {
            this.kerberosSecurityFactory = kerberosSecurityFactory;
            return this;
        }

        public Builder forwardingMode(ForwardingMode forwardingMode) {
            this.forwardingMode = forwardingMode;
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddAuthenticationConfiguration build() {

            int authCounter = 0;
            if (authenticationName != null) {
                authCounter++;
            }
            if (anonymous != null) {
                authCounter++;
            }
            if (securityDomain != null) {
                authCounter++;
            }
            if (kerberosSecurityFactory != null) {
                authCounter++;
            }
            if (authCounter > 1) {
                throw new IllegalArgumentException("Only one of authentication-name, anonymous, security-domain and kerberos-security-factory can be set.");
            }
            return new AddAuthenticationConfiguration(this);
        }

    }

    public static enum ForwardingMode {

        AUTHENTICATION, AUTHORIZATION
    }
}
