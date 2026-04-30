package org.wildfly.extras.creaper.commands.elytron.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jboss.dmr.ModelNode;
import org.wildfly.extras.creaper.commands.elytron.Mechanism;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddHttpAuthenticationFactory implements OnlineCommand {

    private final String name;
    private final String securityDomain;
    private final String httpServerMechanismFactory;
    private final List<Mechanism> mechanismConfigurations;
    private final boolean replaceExisting;

    private AddHttpAuthenticationFactory(Builder builder) {
        this.name = builder.name;
        this.securityDomain = builder.securityDomain;
        this.httpServerMechanismFactory = builder.httpServerMechanismFactory;
        this.mechanismConfigurations = builder.mechanismConfigurations;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address factoryAddress = Address.subsystem("elytron")
                .and("http-authentication-factory", name);
        if (replaceExisting) {
            ops.removeIfExists(factoryAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        List<ModelNode> mechanismConfigurationsNodeList = null;
        if (mechanismConfigurations != null && !mechanismConfigurations.isEmpty()) {
            mechanismConfigurationsNodeList = new ArrayList<ModelNode>();
            for (Mechanism mechanismConfiguration : mechanismConfigurations) {
                ModelNode mechanismNode = new ModelNode();
                addOptionalToModelNode(mechanismNode, "mechanism-name", mechanismConfiguration.getMechanismName());
                addOptionalToModelNode(mechanismNode, "host-name", mechanismConfiguration.getHostName());
                addOptionalToModelNode(mechanismNode, "protocol", mechanismConfiguration.getProtocol());
                addOptionalToModelNode(mechanismNode, "pre-realm-principal-transformer",
                        mechanismConfiguration.getPreRealmPrincipalTransformer());
                addOptionalToModelNode(mechanismNode, "post-realm-principal-transformer",
                        mechanismConfiguration.getPostRealmPrincipalTransformer());
                addOptionalToModelNode(mechanismNode, "final-principal-transformer",
                        mechanismConfiguration.getFinalPrincipalTransformer());
                addOptionalToModelNode(mechanismNode, "realm-mapper", mechanismConfiguration.getRealmMapper());
                addOptionalToModelNode(mechanismNode, "credential-security-factory",
                        mechanismConfiguration.getCredentialSecurityFactory());

                List<ModelNode> mechanismRealmConfigurationsNodeList = null;
                if (mechanismConfiguration.getMechanismRealmConfigurations() != null
                        && !mechanismConfiguration.getMechanismRealmConfigurations().isEmpty()) {

                    mechanismRealmConfigurationsNodeList = new ArrayList<ModelNode>();
                    for (Mechanism.MechanismRealm mechanismRealm
                            : mechanismConfiguration.getMechanismRealmConfigurations()) {
                        ModelNode mechanismRealmNode = new ModelNode();
                        mechanismRealmNode.add("realm-name", mechanismRealm.getRealmName());
                        addOptionalToModelNode(mechanismRealmNode, "pre-realm-principal-transformer",
                                mechanismRealm.getPreRealmPrincipalTransformer());
                        addOptionalToModelNode(mechanismRealmNode, "post-realm-principal-transformer",
                                mechanismRealm.getPostRealmPrincipalTransformer());
                        addOptionalToModelNode(mechanismRealmNode, "final-principal-transformer",
                                mechanismRealm.getFinalPrincipalTransformer());
                        addOptionalToModelNode(mechanismRealmNode, "realm-mapper", mechanismRealm.getRealmMapper());
                        mechanismRealmNode = mechanismRealmNode.asObject();
                        mechanismRealmConfigurationsNodeList.add(mechanismRealmNode);
                    }
                    ModelNode mechanismRealmConfigurations = new ModelNode();
                    mechanismRealmConfigurations.set(mechanismRealmConfigurationsNodeList);
                    mechanismNode.add("mechanism-realm-configurations", mechanismRealmConfigurations);
                }

                mechanismNode = mechanismNode.asObject();
                mechanismConfigurationsNodeList.add(mechanismNode);
            }
        }

        ops.add(factoryAddress, Values.empty()
                .and("security-domain", securityDomain)
                .and("http-server-mechanism-factory", httpServerMechanismFactory)
                .andListOptional(ModelNode.class, "mechanism-configurations", mechanismConfigurationsNodeList));

    }

    private void addOptionalToModelNode(ModelNode node, String name, String value) {
        if (value != null && !value.isEmpty()) {
            node.add(name, value);
        }
    }

    public static final class Builder {

        private final String name;
        private String securityDomain;
        private String httpServerMechanismFactory;
        private List<Mechanism> mechanismConfigurations = new ArrayList<Mechanism>();
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the http-authentication-factory must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the http-authentication-factory must not be empty value");
            }
            this.name = name;
        }

        public Builder securityDomain(String securityDomain) {
            this.securityDomain = securityDomain;
            return this;
        }

        public Builder httpServerMechanismFactory(String httpServerMechanismFactory) {
            this.httpServerMechanismFactory = httpServerMechanismFactory;
            return this;
        }

        public Builder addMechanismConfigurations(Mechanism... mechanismConfigurations) {
            if (mechanismConfigurations == null) {
                throw new IllegalArgumentException("Mechanism added to mechanism-configuration must not be null");
            }
            Collections.addAll(this.mechanismConfigurations, mechanismConfigurations);
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddHttpAuthenticationFactory build() {
            if (securityDomain == null || securityDomain.isEmpty()) {
                throw new IllegalArgumentException("security-domain must not be null and must have a minimum length of 1 characters");
            }
            if (httpServerMechanismFactory == null || httpServerMechanismFactory.isEmpty()) {
                throw new IllegalArgumentException("http-server-mechanism-factory must not be null and must have a minimum length of 1 characters");
            }
            return new AddHttpAuthenticationFactory(this);
        }
    }
}
