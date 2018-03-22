package org.wildfly.extras.creaper.commands.elytron;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Mechanism {

    private final String mechanismName;
    private final String hostName;
    private final String protocol;
    private final String preRealmPrincipalTransformer;
    private final String postRealmPrincipalTransformer;
    private final String finalPrincipalTransformer;
    private final String realmMapper;
    private final String credentialSecurityFactory;
    private final List<MechanismRealm> mechanismRealmConfigurations;

    private Mechanism(Builder builder) {
        this.mechanismName = builder.mechanismName;
        this.hostName = builder.hostName;
        this.protocol = builder.protocol;
        this.preRealmPrincipalTransformer = builder.preRealmPrincipalTransformer;
        this.postRealmPrincipalTransformer = builder.postRealmPrincipalTransformer;
        this.finalPrincipalTransformer = builder.finalPrincipalTransformer;
        this.realmMapper = builder.realmMapper;
        this.credentialSecurityFactory = builder.credentialSecurityFactory;
        this.mechanismRealmConfigurations = builder.mechanismRealmConfigurations;
    }

    public String getMechanismName() {
        return mechanismName;
    }

    public String getHostName() {
        return hostName;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getPreRealmPrincipalTransformer() {
        return preRealmPrincipalTransformer;
    }

    public String getPostRealmPrincipalTransformer() {
        return postRealmPrincipalTransformer;
    }

    public String getFinalPrincipalTransformer() {
        return finalPrincipalTransformer;
    }

    public String getRealmMapper() {
        return realmMapper;
    }

    public String getCredentialSecurityFactory() {
        return credentialSecurityFactory;
    }

    public List<MechanismRealm> getMechanismRealmConfigurations() {
        return mechanismRealmConfigurations;
    }

    public static final class Builder {

        private String mechanismName;
        private String hostName;
        private String protocol;
        private String preRealmPrincipalTransformer;
        private String postRealmPrincipalTransformer;
        private String finalPrincipalTransformer;
        private String realmMapper;
        private String credentialSecurityFactory;
        private List<MechanismRealm> mechanismRealmConfigurations = new ArrayList<MechanismRealm>();

        public Builder mechanismName(String mechanismName) {
            this.mechanismName = mechanismName;
            return this;
        }

        public Builder hostName(String hostName) {
            this.hostName = hostName;
            return this;
        }

        public Builder protocol(String protocol) {
            this.protocol = protocol;
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

        public Builder finalPrincipalTransformer(String finalPrincipalTransformer) {
            this.finalPrincipalTransformer = finalPrincipalTransformer;
            return this;
        }

        public Builder realmMapper(String realmMapper) {
            this.realmMapper = realmMapper;
            return this;
        }

        public Builder credentialSecurityFactory(String credentialSecurityFactory) {
            this.credentialSecurityFactory = credentialSecurityFactory;
            return this;
        }

        public Builder addMechanismRealmConfigurations(MechanismRealm... mechanismRealms) {
            if (mechanismRealms == null) {
                throw new IllegalArgumentException("mechanism-realm-configuration added must not be null");
            }
            Collections.addAll(this.mechanismRealmConfigurations, mechanismRealms);
            return this;
        }

        public Mechanism build() {
            return new Mechanism(this);
        }

    }

    public static final class MechanismRealm {

        private final String realmName;
        private final String preRealmPrincipalTransformer;
        private final String postRealmPrincipalTransformer;
        private final String finalPrincipalTransformer;
        private final String realmMapper;

        private MechanismRealm(MechanismRealmBuilder builder) {
            this.realmName = builder.realmName;
            this.preRealmPrincipalTransformer = builder.preRealmPrincipalTransformer;
            this.postRealmPrincipalTransformer = builder.postRealmPrincipalTransformer;
            this.finalPrincipalTransformer = builder.finalPrincipalTransformer;
            this.realmMapper = builder.realmMapper;
        }

        public String getRealmName() {
            return realmName;
        }

        public String getPreRealmPrincipalTransformer() {
            return preRealmPrincipalTransformer;
        }

        public String getPostRealmPrincipalTransformer() {
            return postRealmPrincipalTransformer;
        }

        public String getFinalPrincipalTransformer() {
            return finalPrincipalTransformer;
        }

        public String getRealmMapper() {
            return realmMapper;
        }

    }

    public static final class MechanismRealmBuilder {

        private String realmName;
        private String preRealmPrincipalTransformer;
        private String postRealmPrincipalTransformer;
        private String finalPrincipalTransformer;
        private String realmMapper;

        public MechanismRealmBuilder realmName(String realmName) {
            this.realmName = realmName;
            return this;
        }

        public MechanismRealmBuilder preRealmPrincipalTransformer(String preRealmPrincipalTransformer) {
            this.preRealmPrincipalTransformer = preRealmPrincipalTransformer;
            return this;
        }

        public MechanismRealmBuilder postRealmPrincipalTransformer(String postRealmPrincipalTransformer) {
            this.postRealmPrincipalTransformer = postRealmPrincipalTransformer;
            return this;
        }

        public MechanismRealmBuilder finalPrincipalTransformer(String finalPrincipalTransformer) {
            this.finalPrincipalTransformer = finalPrincipalTransformer;
            return this;
        }

        public MechanismRealmBuilder realmMapper(String realmMapper) {
            this.realmMapper = realmMapper;
            return this;
        }

        public MechanismRealm build() {
            if (realmName == null || realmName.isEmpty()) {
                throw new IllegalArgumentException("realm-name of mechanism realm must not be null and must include at least one entry");
            }
            return new MechanismRealm(this);
        }
    }
}
