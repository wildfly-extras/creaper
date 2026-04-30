package org.wildfly.extras.creaper.commands.elytron.authenticationclient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddAuthenticationContext implements OnlineCommand {

    private final String name;
    private final String extend;
    private final boolean replaceExisting;
    private final List<MatchRule> matchRules;

    private AddAuthenticationContext(Builder builder) {
        this.name = builder.name;
        this.extend = builder.extend;
        this.replaceExisting = builder.replaceExisting;
        this.matchRules = builder.matchRules;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address realmAddress = Address.subsystem("elytron").and("authentication-context", name);
        if (replaceExisting) {
            ops.removeIfExists(realmAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        List<ModelNode> matchRulesNodeList = null;
        if (matchRules != null && !matchRules.isEmpty()) {
            matchRulesNodeList = new ArrayList<ModelNode>();
            for (MatchRule matchRule : matchRules) {
                ModelNode matchRuleNode = new ModelNode();
                addOptional(matchRuleNode, "match-abstract-type", matchRule.matchAbstractType);
                addOptional(matchRuleNode, "match-abstract-type-authority", matchRule.matchAbstractTypeAuthority);
                addOptional(matchRuleNode, "match-host", matchRule.matchHost);
                addOptional(matchRuleNode, "match-local-security-domain", matchRule.matchLocalSecurityDomain);
                addOptional(matchRuleNode, "match-no-user", matchRule.matchNoUser);
                addOptional(matchRuleNode, "match-path", matchRule.matchPath);
                addOptional(matchRuleNode, "match-port", matchRule.matchPort);
                addOptional(matchRuleNode, "match-protocol", matchRule.matchProtocol);
                addOptional(matchRuleNode, "match-urn", matchRule.matchUrn);
                addOptional(matchRuleNode, "match-user", matchRule.matchUser);
                addOptional(matchRuleNode, "authentication-configuration", matchRule.authenticationConfiguration);
                addOptional(matchRuleNode, "ssl-context", matchRule.sslContext);
                matchRulesNodeList.add(matchRuleNode.asObject());
            }
        }

        ops.add(realmAddress, Values.empty()
                .andOptional("extends", extend)
                .andListOptional(ModelNode.class, "match-rules", matchRulesNodeList));
    }

    private void addOptional(ModelNode node, String name, String value) {
        if (value != null && !value.isEmpty()) {
            node.add(name, value);
        }
    }

    private void addOptional(ModelNode node, String name, Boolean value) {
        if (value != null) {
            node.add(name, value);
        }
    }

    private void addOptional(ModelNode node, String name, Integer value) {
        if (value != null) {
            node.add(name, value);
        }
    }

    public static final class Builder {

        private final String name;
        private String extend;
        private boolean replaceExisting;
        private List<MatchRule> matchRules = new ArrayList<MatchRule>();

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the authentication-context must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the authentication-context must not be empty value");
            }
            this.name = name;
        }

        public Builder extend(String extend) {
            this.extend = extend;
            return this;
        }

        public Builder addMatchRules(MatchRule... matchRule) {
            if (matchRule == null) {
                throw new IllegalArgumentException("MatchRule added to authentication-context must not be null");
            }
            Collections.addAll(this.matchRules, matchRule);
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddAuthenticationContext build() {
            return new AddAuthenticationContext(this);
        }
    }

    public static final class MatchRule {

        private final String matchAbstractType;
        private final String matchAbstractTypeAuthority;
        private final String matchHost;
        private final String matchLocalSecurityDomain;
        private final Boolean matchNoUser;
        private final String matchPath;
        private final Integer matchPort;
        private final String matchProtocol;
        private final String matchUrn;
        private final String matchUser;
        private final String authenticationConfiguration;
        private final String sslContext;

        private MatchRule(MatchRuleBuilder builder) {
            this.matchAbstractType = builder.matchAbstractType;
            this.matchAbstractTypeAuthority = builder.matchAbstractTypeAuthority;
            this.matchHost = builder.matchHost;
            this.matchLocalSecurityDomain = builder.matchLocalSecurityDomain;
            this.matchNoUser = builder.matchNoUser;
            this.matchPath = builder.matchPath;
            this.matchPort = builder.matchPort;
            this.matchProtocol = builder.matchProtocol;
            this.matchUrn = builder.matchUrn;
            this.matchUser = builder.matchUser;
            this.authenticationConfiguration = builder.authenticationConfiguration;
            this.sslContext = builder.sslContext;
        }

        public String getMatchAbstractType() {
            return matchAbstractType;
        }

        public String getMatchAbstractTypeAuthority() {
            return matchAbstractTypeAuthority;
        }

        public String getMatchHost() {
            return matchHost;
        }

        public String getMatchLocalSecurityDomain() {
            return matchLocalSecurityDomain;
        }

        public Boolean getMatchNoUser() {
            return matchNoUser;
        }

        public String getMatchPath() {
            return matchPath;
        }

        public Integer getMatchPort() {
            return matchPort;
        }

        public String getMatchProtocol() {
            return matchProtocol;
        }

        public String getMatchUrn() {
            return matchUrn;
        }

        public String getMatchUser() {
            return matchUser;
        }

        public String getAuthenticationConfiguration() {
            return authenticationConfiguration;
        }

        public String getSslContext() {
            return sslContext;
        }

    }

    public static final class MatchRuleBuilder {

        private String matchAbstractType;
        private String matchAbstractTypeAuthority;
        private String matchHost;
        private String matchLocalSecurityDomain;
        private Boolean matchNoUser;
        private String matchPath;
        private Integer matchPort;
        private String matchProtocol;
        private String matchUrn;
        private String matchUser;
        private String authenticationConfiguration;
        private String sslContext;

        public MatchRuleBuilder matchAbstractType(String matchAbstractType) {
            this.matchAbstractType = matchAbstractType;
            return this;
        }

        public MatchRuleBuilder matchAbstractTypeAuthority(String matchAbstractTypeAuthority) {
            this.matchAbstractTypeAuthority = matchAbstractTypeAuthority;
            return this;
        }

        public MatchRuleBuilder matchHost(String matchHost) {
            this.matchHost = matchHost;
            return this;
        }

        public MatchRuleBuilder matchLocalSecurityDomain(String matchLocalSecurityDomain) {
            this.matchLocalSecurityDomain = matchLocalSecurityDomain;
            return this;
        }

        public MatchRuleBuilder matchNoUser(Boolean matchNoUser) {
            this.matchNoUser = matchNoUser;
            return this;
        }

        public MatchRuleBuilder matchPath(String matchPath) {
            this.matchPath = matchPath;
            return this;
        }

        public MatchRuleBuilder matchPort(Integer matchPort) {
            this.matchPort = matchPort;
            return this;
        }

        public MatchRuleBuilder matchProtocol(String matchProtocol) {
            this.matchProtocol = matchProtocol;
            return this;
        }

        public MatchRuleBuilder matchUrn(String matchUrn) {
            this.matchUrn = matchUrn;
            return this;
        }

        public MatchRuleBuilder matchUser(String matchUser) {
            this.matchUser = matchUser;
            return this;
        }

        public MatchRuleBuilder authenticationConfiguration(String authenticationConfiguration) {
            this.authenticationConfiguration = authenticationConfiguration;
            return this;
        }

        public MatchRuleBuilder sslContext(String sslContext) {
            this.sslContext = sslContext;
            return this;
        }

        public MatchRule build() {
            return new MatchRule(this);
        }
    }
}
