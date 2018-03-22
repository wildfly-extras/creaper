package org.wildfly.extras.creaper.commands.elytron.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jboss.dmr.ModelNode;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddSecurityDomain implements OnlineCommand {

    private final String name;
    private final String defaultRealm;
    private final String preRealmPrincipalTransformer;
    private final String postRealmPrincipalTransformer;
    private final String principalDecoder;
    private final String realmMapper;
    private final String roleMapper;
    private final String permissionMapper;
    private final List<String> trustedSecurityDomains;
    private final Boolean outflowAnonymous;
    private final List<String> outflowSecurityDomains;
    private final String securityEventListener;
    private final List<Realm> realms;
    private final boolean replaceExisting;

    private AddSecurityDomain(Builder builder) {
        this.name = builder.name;
        this.defaultRealm = builder.defaultRealm;
        this.preRealmPrincipalTransformer = builder.preRealmPrincipalTransformer;
        this.postRealmPrincipalTransformer = builder.postRealmPrincipalTransformer;
        this.principalDecoder = builder.principalDecoder;
        this.realmMapper = builder.realmMapper;
        this.roleMapper = builder.roleMapper;
        this.permissionMapper = builder.permissionMapper;
        this.replaceExisting = builder.replaceExisting;
        this.realms = builder.realms;
        this.trustedSecurityDomains = builder.trustedSecurityDomains;
        this.outflowAnonymous = builder.outflowAnonymous;
        this.outflowSecurityDomains = builder.outflowSecurityDomains;
        this.securityEventListener = builder.securityEventListener;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (ctx.version.lessThan(ServerVersion.VERSION_5_0_0)) {
            throw new AssertionError("Elytron is available since WildFly 11.");
        }

        Operations ops = new Operations(ctx.client);
        Address securityDomainAddress = Address.subsystem("elytron").and("security-domain", name);
        if (replaceExisting) {
            ops.removeIfExists(securityDomainAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        List<ModelNode> realmsModelNodeList = new ArrayList<ModelNode>();
        for (Realm realm : realms) {
            ModelNode configNode = new ModelNode();
            configNode.add("realm", realm.getName());
            if (realm.getPrincipalTransformer() != null && !realm.getPrincipalTransformer().isEmpty()) {
                configNode.add("principal-transformer", realm.getPrincipalTransformer());
            }
            if (realm.getRoleDecoder() != null && !realm.getRoleDecoder().isEmpty()) {
                configNode.add("role-decoder", realm.getRoleDecoder());
            }
            if (realm.getRoleMapper() != null && !realm.getRoleMapper().isEmpty()) {
                configNode.add("role-mapper", realm.getRoleMapper());
            }
            configNode = configNode.asObject();
            realmsModelNodeList.add(configNode);
        }

        ops.add(securityDomainAddress, Values.empty()
                .andOptional("default-realm", defaultRealm)
                .andList(ModelNode.class, "realms", realmsModelNodeList)
                .andOptional("pre-realm-principal-transformer", preRealmPrincipalTransformer)
                .andOptional("post-realm-principal-transformer", postRealmPrincipalTransformer)
                .andOptional("principal-decoder", principalDecoder)
                .andOptional("realm-mapper", realmMapper)
                .andOptional("role-mapper", roleMapper)
                .andOptional("permission-mapper", permissionMapper)
                .andListOptional(String.class, "trusted-security-domains", trustedSecurityDomains)
                .andOptional("outflow-anonymous", outflowAnonymous)
                .andOptional("security-event-listener", securityEventListener)
                .andListOptional(String.class, "outflow-security-domains", outflowSecurityDomains));
    }

    public static final class Builder {

        private final String name;
        private String defaultRealm;
        private String preRealmPrincipalTransformer;
        private String postRealmPrincipalTransformer;
        private String principalDecoder;
        private String realmMapper;
        private String roleMapper;
        private String permissionMapper;
        private List<String> trustedSecurityDomains;
        private Boolean outflowAnonymous;
        private List<String> outflowSecurityDomains;
        private String securityEventListener;
        private List<Realm> realms;
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the security-domain must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the security-domain must not be empty value");
            }
            this.name = name;
        }

        public Builder defaultRealm(String defaultRealm) {
            this.defaultRealm = defaultRealm;
            return this;
        }

        public Builder preRealmPrincipalTransformer(String preRealmPrincipalTransformer) {
            this.preRealmPrincipalTransformer = preRealmPrincipalTransformer;
            return this;
        }

        public Builder postRealmPrincipalTransformer(String postRealmPrincipalTransformer) {
            this.postRealmPrincipalTransformer = postRealmPrincipalTransformer;
            return this;
        }

        public Builder principalDecoder(String principalDecoder) {
            this.principalDecoder = principalDecoder;
            return this;
        }

        public Builder realmMapper(String realmMapper) {
            this.realmMapper = realmMapper;
            return this;
        }

        public Builder roleMapper(String roleMapper) {
            this.roleMapper = roleMapper;
            return this;
        }

        public Builder permissionMapper(String permissionMapper) {
            this.permissionMapper = permissionMapper;
            return this;
        }

        public Builder trustedSecurityDomains(String... trustedSecurityDomains) {
            if (trustedSecurityDomains == null) {
                throw new IllegalArgumentException("Trusted Security Domains added to security-domain must not be null");
            }
            if (this.trustedSecurityDomains == null) {
                this.trustedSecurityDomains = new ArrayList<String>();
            }
            Collections.addAll(this.trustedSecurityDomains, trustedSecurityDomains);
            return this;
        }

        public Builder outflowAnonymous(Boolean outflowAnonymous) {
            this.outflowAnonymous = outflowAnonymous;
            return this;
        }

        public Builder outflowSecurityDomains(String... outflowSecurityDomains) {
            if (outflowSecurityDomains == null) {
                throw new IllegalArgumentException("Outflow Security Domains added to security-domain must not be null");
            }
            if (this.outflowSecurityDomains == null) {
                this.outflowSecurityDomains = new ArrayList<String>();
            }
            Collections.addAll(this.outflowSecurityDomains, outflowSecurityDomains);
            return this;
        }

        public Builder securityEventListener(String securityEventListener) {
            this.securityEventListener = securityEventListener;
            return this;
        }

        public Builder realms(Realm... realms) {
            if (realms == null) {
                throw new IllegalArgumentException("Realms added to security-domain must not be null");
            }
            if (this.realms == null) {
                this.realms = new ArrayList<Realm>();
            }
            Collections.addAll(this.realms, realms);
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddSecurityDomain build() {
            if (realms == null || realms.isEmpty()) {
                throw new IllegalArgumentException("realms must not be null and must include at least one entry");
            }
            return new AddSecurityDomain(this);
        }

    }

    public static final class Realm {

        private final String name;
        private final String principalTransformer;
        private final String roleDecoder;
        private final String roleMapper;

        private Realm(RealmBuilder builder) {
            this.name = builder.name;
            this.principalTransformer = builder.principalTransformer;
            this.roleDecoder = builder.roleDecoder;
            this.roleMapper = builder.roleMapper;
        }

        public String getName() {
            return name;
        }

        public String getPrincipalTransformer() {
            return principalTransformer;
        }

        public String getRoleDecoder() {
            return roleDecoder;
        }

        public String getRoleMapper() {
            return roleMapper;
        }

    }

    public static final class RealmBuilder {

        private final String name;
        private String principalTransformer;
        private String roleDecoder;
        private String roleMapper;

        public RealmBuilder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the realm in security-domain must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the realm in security-domain must not be empty value");
            }
            this.name = name;
        }

        public RealmBuilder principalTransformer(String principalTransformer) {
            this.principalTransformer = principalTransformer;
            return this;
        }

        public RealmBuilder roleDecoder(String roleDecoder) {
            this.roleDecoder = roleDecoder;
            return this;
        }

        public RealmBuilder roleMapper(String roleMapper) {
            this.roleMapper = roleMapper;
            return this;
        }

        public Realm build() {
            return new Realm(this);
        }

    }
}
