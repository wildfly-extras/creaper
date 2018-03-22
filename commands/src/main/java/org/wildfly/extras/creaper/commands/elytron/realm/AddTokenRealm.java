package org.wildfly.extras.creaper.commands.elytron.realm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.wildfly.extras.creaper.core.ServerVersion;

import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddTokenRealm implements OnlineCommand {

    private final String name;
    private final Jwt jwt;
    private final Oauth2Introspection oauth2Introspection;
    private final String principalClaim;
    private final boolean replaceExisting;

    private AddTokenRealm(Builder builder) {
        this.name = builder.name;
        this.principalClaim = builder.principalClaim;
        this.jwt = builder.jwt;
        this.oauth2Introspection = builder.oauth2Introspection;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (ctx.version.lessThan(ServerVersion.VERSION_5_0_0)) {
            throw new AssertionError("Elytron is available since WildFly 11.");
        }

        Operations ops = new Operations(ctx.client);
        Address tokenRealmAddress = Address.subsystem("elytron")
                .and("token-realm", name);
        if (replaceExisting) {
            ops.removeIfExists(tokenRealmAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        Values jwtProperties = jwt != null
                ? Values.empty()
                .andListOptional(String.class, "issuer", jwt.getIssuer())
                .andListOptional(String.class, "audience", jwt.getAudience())
                .andOptional("public-key", jwt.getPublicKey())
                .andOptional("key-store", jwt.getKeyStore())
                .andOptional("certificate", jwt.getCertificate())
                : null;

        Values oauth2IntrospectionProperties = oauth2Introspection != null
                ? Values.empty()
                .and("client-id", oauth2Introspection.getClientId())
                .and("client-secret", oauth2Introspection.getClientSecret())
                .and("introspection-url", oauth2Introspection.getIntrospectionUrl())
                .andOptional("client-ssl-context", oauth2Introspection.getClientSslContext())
                .andOptional("host-name-verification-policy", oauth2Introspection.getHostNameVerificationPolicy())
                : null;

        ops.add(tokenRealmAddress, Values.empty()
                .andOptional("principal-claim", principalClaim)
                .andObjectOptional("jwt", jwtProperties)
                .andObjectOptional("oauth2-introspection", oauth2IntrospectionProperties));
    }

    public static final class Builder {

        private final String name;
        private Jwt jwt;
        private Oauth2Introspection oauth2Introspection;
        private String principalClaim;
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the token-realm must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the token-realm must not be empty value");
            }
            this.name = name;
        }

        public Builder jwt(Jwt jwt) {
            if (jwt == null) {
                throw new IllegalArgumentException("Jwt added to token-realm must not be null");
            }
            this.jwt = jwt;
            return this;
        }

        public Builder oauth2Introspection(Oauth2Introspection oauth2Introspection) {
            if (oauth2Introspection == null) {
                throw new IllegalArgumentException("OAuth2-introspection added to token-realm must not be null");
            }
            this.oauth2Introspection = oauth2Introspection;
            return this;
        }

        public Builder principalClaim(String principalClaim) {
            if (principalClaim == null || principalClaim.isEmpty()) {
                throw new IllegalArgumentException("Principal-claim must not be null and must have a minimum length of 1 character");
            }
            this.principalClaim = principalClaim;
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddTokenRealm build() {
            if (jwt == null && oauth2Introspection == null) {
                throw new IllegalArgumentException("Jwt or oauth2-introspection must not be null");
            }
            if (jwt != null && oauth2Introspection != null) {
                throw new IllegalArgumentException("It is not possible to define both jwt and oauth2-introspection");
            }

            return new AddTokenRealm(this);
        }
    }

    public static final class Jwt {

        private final List<String> issuer;
        private final List<String> audience;
        private final String publicKey;
        private final String keyStore;
        private final String certificate;

        private Jwt(JwtBuilder builder) {
            this.issuer = builder.issuer;
            this.audience = builder.audience;
            this.publicKey = builder.publicKey;
            this.keyStore = builder.keyStore;
            this.certificate = builder.certificate;
        }

        public List<String> getIssuer() {
            return issuer;
        }

        public List<String> getAudience() {
            return audience;
        }

        public String getPublicKey() {
            return publicKey;
        }

        public String getKeyStore() {
            return keyStore;
        }

        public String getCertificate() {
            return certificate;
        }

    }

    public static final class JwtBuilder {

        private List<String> issuer;
        private List<String> audience;
        private String publicKey;
        private String keyStore;
        private String certificate;

        public JwtBuilder addIssuer(String... issuer) {
            if (issuer == null) {
                throw new IllegalArgumentException("Issuer added to token-realm must not be null");
            }
            if (this.issuer == null) {
                this.issuer = new ArrayList<String>();
            }
            Collections.addAll(this.issuer, issuer);
            return this;
        }

        public JwtBuilder addAudience(String... audience) {
            if (audience == null) {
                throw new IllegalArgumentException("Audience added to token-realm must not be null");
            }

            if (this.audience == null) {
                this.audience = new ArrayList<String>();
            }

            Collections.addAll(this.audience, audience);
            return this;
        }

        public JwtBuilder publicKey(String publicKey) {
            this.publicKey = publicKey;
            return this;
        }

        public JwtBuilder keyStore(String keyStore) {
            this.keyStore = keyStore;
            return this;
        }

        public JwtBuilder certificate(String certificate) {
            this.certificate = certificate;
            return this;
        }

        public Jwt build() {
            return new Jwt(this);
        }
    }

    public static final class Oauth2Introspection {

        private final String clientId;
        private final String clientSecret;
        private final String introspectionUrl;
        private final String clientSslContext;
        private final String hostNameVerificationPolicy;

        private Oauth2Introspection(Oauth2IntrospectionBuilder builder) {
            this.clientId = builder.clientId;
            this.clientSecret = builder.clientSecret;
            this.introspectionUrl = builder.introspectionUrl;
            this.clientSslContext = builder.clientSslContext;
            this.hostNameVerificationPolicy = builder.hostNameVerificationPolicy;
        }

        public String getClientId() {
            return clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public String getIntrospectionUrl() {
            return introspectionUrl;
        }

        public String getClientSslContext() {
            return clientSslContext;
        }

        public String getHostNameVerificationPolicy() {
            return hostNameVerificationPolicy;
        }

    }

    public static final class Oauth2IntrospectionBuilder {

        private String clientId;
        private String clientSecret;
        private String introspectionUrl;
        private String clientSslContext;
        private String hostNameVerificationPolicy;

        public Oauth2IntrospectionBuilder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Oauth2IntrospectionBuilder clientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public Oauth2IntrospectionBuilder introspectionUrl(String introspectionUrl) {
            this.introspectionUrl = introspectionUrl;
            return this;
        }

        public Oauth2IntrospectionBuilder clientSslContext(String clientSslContext) {
            if (clientSslContext == null || clientSslContext.isEmpty()) {
                throw new IllegalArgumentException("Client-ssl-context added to token-realm must not be null and must have a minimum length of 1 character");
            }
            this.clientSslContext = clientSslContext;
            return this;
        }

        public Oauth2IntrospectionBuilder hostNameVerificationPolicy(String hostNameVerificationPolicy) {
            if (hostNameVerificationPolicy == null || hostNameVerificationPolicy.isEmpty()) {
                throw new IllegalArgumentException("Host-name-verification-policy added to token-realm must not be null and must have a minimum length of 1 character");
            }
            this.hostNameVerificationPolicy = hostNameVerificationPolicy;
            return this;
        }

        public Oauth2Introspection build() {
            if (clientId == null || clientId.isEmpty()) {
                throw new IllegalArgumentException("Client-id must not be null and must have a minimum length of 1 character");
            }
            if (clientSecret == null || clientSecret.isEmpty()) {
                throw new IllegalArgumentException("Client-secret must not be null and must have at least 1 entry");
            }
            if (introspectionUrl == null || introspectionUrl.isEmpty()) {
                throw new IllegalArgumentException("Introspection-url must not be null and must have a minimum length of 1 character");
            }

            return new Oauth2Introspection(this);
        }
    }
}
