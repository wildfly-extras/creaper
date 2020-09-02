package org.wildfly.extras.creaper.commands.elytron;

import java.util.Arrays;
import java.util.List;

import org.wildfly.extras.creaper.commands.elytron.CredentialRef.CredentialRefBuilder;
import org.wildfly.extras.creaper.commands.elytron.tls.AddKeyManager;
import org.wildfly.extras.creaper.commands.elytron.tls.AddKeyStore;
import org.wildfly.extras.creaper.commands.elytron.tls.AddServerSSLContext;
import org.wildfly.extras.creaper.commands.elytron.tls.AddTrustManager;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;

/**
 * Composite creaper command. Takes flat parameters and compose server ssl
 * context with all dependent resource hierarchy.
 *
 * <p>
 * It should be possible to configure most of use cases atomic creaper commands
 * allows.
 * </p>
 *
 */
public final class CreateServerSSLContext implements OnlineCommand {

    private static final String KEY_STORE_NAME = "key-store-name";
    private static final String TRUST_STORE_NAME = "trust-store-name";
    private static final String KEY_MANAGER_NAME = "key-manager-name";
    private static final String TRUST_MANAGER_NAME = "trust-manager-name";

    // SSL Context
    private final String name;
    protected final List<String> protocols;
    private final String cipherSuiteFilter;
    private final String cipherSuiteNames;
    private final Boolean needClientAuth;
    private final Boolean wantClientAuth;
    private final Boolean authenticationOptional;
    private final String securityDomain;
    private final Integer maximumSessionCacheSize;
    private final Integer sessionTimeout;
    private final String providers;
    // Keystore
    private final String keyStoreType;
    private final String keyStorePath;
    private final String keyStorePassword;
    private final String keyPassword;
    private final String keyStoreAlias;
    private final String keyStoreRelativeTo;
    private final Boolean keyStoreRequired;
    private final String keyStoreProviders;
    private final String keyManagerProviders;
    // Truststore
    private final String trustStoreType;
    private final String trustStorePath;
    private final String trustStorePassword;
    private final String trustStoreAlias;
    private final String trustStoreRelativeTo;
    private final Boolean trustStoreRequired;
    private final String trustStoreProviders;
    private final String trustManagerProviders;

    // Multiple usage
    private final String algorithm;         // keystore manager, truststore manager

    // Default set of cipher suites for TLSv1.3 to be set in 'cipher-suite-names' attribute.
    public static final String TLS13_CIPHER_SUITE_NAMES =
            "TLS_AES_256_GCM_SHA384:TLS_CHACHA20_POLY1305_SHA256:TLS_AES_128_GCM_SHA256";


    private CreateServerSSLContext(Builder builder) {
        this.name = builder.name;
        this.keyStoreType = builder.keyStoreType;
        this.keyStorePath = builder.keyStorePath;
        this.keyStorePassword = builder.keyStorePassword;
        this.keyPassword = builder.keyPassword;
        this.trustStoreType = builder.trustStoreType;
        this.trustStorePath = builder.trustStorePath;
        this.trustStorePassword = builder.trustStorePassword;
        this.protocols = builder.protocols;
        this.cipherSuiteFilter = builder.cipherSuiteFilter;
        this.cipherSuiteNames = builder.cipherSuiteNames;
        this.needClientAuth = builder.needClientAuth;
        this.wantClientAuth = builder.wantClientAuth;
        this.authenticationOptional = builder.authenticationOptional;
        this.securityDomain = builder.securityDomain;
        this.maximumSessionCacheSize = builder.maximumSessionCacheSize;
        this.sessionTimeout = builder.sessionTimeout;
        this.keyStoreAlias = builder.keyStoreAlias;
        this.keyStoreRelativeTo = builder.keyStoreRelativeTo;
        this.keyStoreRequired = builder.keyStoreRequired;
        this.trustStoreAlias = builder.trustStoreAlias;
        this.trustStoreRelativeTo = builder.trustStoreRelativeTo;
        this.trustStoreRequired = builder.trustStoreRequired;
        this.algorithm = builder.algorithm;
        this.trustStoreProviders = builder.trustStoreProviders;
        this.keyStoreProviders = builder.keyStoreProviders;
        this.keyManagerProviders = builder.keyManagerProviders;
        this.trustManagerProviders = builder.trustManagerProviders;
        this.providers = builder.providers;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {

        AddKeyStore addKeyStore = new AddKeyStore.Builder(getUniqueName(KEY_STORE_NAME))
                .type(keyStoreType)
                .path(keyStorePath)
                .relativeTo(keyStoreRelativeTo)
                .required(keyStoreRequired)
                .aliasFilter(keyStoreAlias)
                .providers(keyStoreProviders)
                .credentialReference(new CredentialRefBuilder()
                    .clearText(keyStorePassword)
                    .build())
                .build();


        AddKeyManager addKeyManager = new AddKeyManager.Builder(getUniqueName(KEY_MANAGER_NAME))
                .keyStore(getUniqueName(KEY_STORE_NAME))
                .algorithm(algorithm)
                .providers(keyManagerProviders)
                .credentialReference(new CredentialRefBuilder()
                    .clearText(keyPassword)
                    .build())
                .build();

        AddKeyStore addTrustStore = null;
        AddTrustManager addTrustManager = null;
        // Trust Store is optional
        if (isTrustStoreConfigured()) {
            addTrustStore = new AddKeyStore.Builder(getUniqueName(TRUST_STORE_NAME))
                    .type(trustStoreType)
                    .path(trustStorePath)
                    .relativeTo(trustStoreRelativeTo)
                    .required(trustStoreRequired)
                    .aliasFilter(trustStoreAlias)
                    .providers(trustStoreProviders)
                    .credentialReference(new CredentialRefBuilder()
                        .clearText(trustStorePassword)
                        .build())
                    .build();

            addTrustManager = new AddTrustManager.Builder(getUniqueName(TRUST_MANAGER_NAME))
                    .algorithm(algorithm)
                    .providers(trustManagerProviders)
                    .keyStore(getUniqueName(TRUST_STORE_NAME))
                    .build();
        }

        AddServerSSLContext.Builder sslServerContextBuilder = new AddServerSSLContext.Builder(name)
                .protocols((protocols == null) ? null : protocols.toArray(new String[protocols.size()]))
                .cipherSuiteFilter(cipherSuiteFilter)
                .cipherSuiteNames(cipherSuiteNames)
                .needClientAuth(needClientAuth)
                .sessionTimeout(sessionTimeout)
                .maximumSessionCacheSize(maximumSessionCacheSize)
                .securityDomain(securityDomain)
                .authenticationOptional(authenticationOptional)
                .wantClientAuth(wantClientAuth)
                .providers(providers)
                .keyManager(getUniqueName(KEY_MANAGER_NAME));

        if (isTrustStoreConfigured()) {
            sslServerContextBuilder
                .trustManager(getUniqueName(TRUST_MANAGER_NAME));
        }

        ctx.client.apply(addKeyStore);
        ctx.client.apply(addKeyManager);
        if (isTrustStoreConfigured()) {
            ctx.client.apply(addTrustStore);
            ctx.client.apply(addTrustManager);
        }
        ctx.client.apply(sslServerContextBuilder.build());
    }

    /**
     * Get name of trust manager associated with server ssl context.
     *
     * @param serverSslContextName server SSL context name
     * @return trust manager name
     */
    public static String getTrustManagerName(String serverSslContextName) {
        return getUniqueName(TRUST_MANAGER_NAME, serverSslContextName);
    }

    /**
     * Get name of key manager associated with server ssl context.
     *
     * @param serverSslContextName server SSL context name
     * @return key manager name
     */
    public static String getKeyManagerName(String serverSslContextName) {
        return getUniqueName(KEY_MANAGER_NAME, serverSslContextName);
    }

    /**
     * Get name of key store associated with server ssl context.
     *
     * @param serverSslContext server SSL context name
     * @return key store name
     */
    public static String getKeyStoreName(String serverSslContext) {
        return getUniqueName(KEY_STORE_NAME, serverSslContext);
    }

    /**
     * Get name of trust store associated with server ssl context.
     *
     * @param serverSslContext server SSL context name
     * @return trust store name
     */
    public static String getTrustStoreName(String serverSslContext) {
        return getUniqueName(TRUST_STORE_NAME, serverSslContext);
    }

    /**
     * @return if trust store is configured or not
     */
    private boolean isTrustStoreConfigured() {
        return trustStorePassword != null && !trustStorePassword.isEmpty();
    }

    /**
     * It will generate unique name by adding name of server ssl context.
     *
     * <p>
     * It is ensured server ssl context has to be unique.
     * </p>
     *
     * @param resourceNameBase , e.g. key-store
     * @return name suffixed with ssl context name
     */
    private String getUniqueName(String resourceNameBase) {
        return getUniqueName(resourceNameBase, this.name);
    }

    private static String getUniqueName(String resourceNameBase, String sslContextName) {
        return resourceNameBase + "_" + sslContextName;
    }

    public static final class Builder {

        // SSL Context
        private String name;
        private List<String> protocols;
        private String cipherSuiteFilter;
        private String cipherSuiteNames;
        private Boolean needClientAuth;
        private Boolean wantClientAuth;
        private Boolean authenticationOptional;
        private String securityDomain;
        private Integer maximumSessionCacheSize;
        private Integer sessionTimeout;
        private String providers;
        // Keystore
        private String keyStoreType = "JKS";
        private String keyStorePath;
        private String keyStorePassword;
        private String keyPassword;
        private String keyStoreAlias;
        private String keyStoreRelativeTo;
        private Boolean keyStoreRequired;
        private String keyStoreProviders;
        private String keyManagerProviders;
        // Truststore
        private String trustStoreType = "JKS";
        private String trustStorePath;
        private String trustStorePassword;
        private String trustStoreAlias;
        private String trustStoreRelativeTo;
        private Boolean trustStoreRequired;
        private String trustStoreProviders;
        private String trustManagerProviders;
        // Multiple usage
        private String algorithm;  // keystore manager, truststore manager

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the ssl-context must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the ssl-context must not be empty value");
            }
            this.name = name;
        }

        public Builder keyStoreType(String keyStoreType) {
            this.keyStoreType = keyStoreType;
            return this;
        }

        public Builder keyStorePath(String keyStorePath) {
            this.keyStorePath = keyStorePath;
            return this;
        }

        public Builder keyStorePassword(String keyStorePassword) {
            this.keyStorePassword = keyStorePassword;
            return this;
        }

        public Builder keyPassword(String keyPassword) {
            this.keyPassword = keyPassword;
            return this;
        }

        public Builder trustStoreType(String trustStoreType) {
            this.trustStoreType = trustStoreType;
            return this;
        }

        public Builder trustStorePath(String trustStorePath) {
            this.trustStorePath = trustStorePath;
            return this;
        }

        public Builder trustStorePassword(String trustStorePassword) {
            this.trustStorePassword = trustStorePassword;
            return this;
        }

        public Builder protocols(String... protocols) {
            if (protocols != null && protocols.length > 0) {
                this.protocols = Arrays.asList(protocols);
            }
            return this;
        }

        public Builder cipherSuiteFilter(String cipherSuiteFilter) {
            this.cipherSuiteFilter = cipherSuiteFilter;
            return this;
        }

        public Builder cipherSuiteNames(String cipherSuiteNames) {
            this.cipherSuiteNames = cipherSuiteNames;
            return this;
        }

        public Builder needClientAuth(Boolean needClientAuth) {
            this.needClientAuth = needClientAuth;
            return this;
        }

        public Builder wantClientAuth(Boolean wantClientAuth) {
            this.wantClientAuth = wantClientAuth;
            return this;
        }

        public Builder authenticationOptional(Boolean authenticationOptional) {
            this.authenticationOptional = authenticationOptional;
            return this;
        }

        public Builder securityDomain(String securityDomain) {
            this.securityDomain = securityDomain;
            return this;
        }

        public Builder maximumSessionCacheSize(Integer maximumSessionCacheSize) {
            this.maximumSessionCacheSize = maximumSessionCacheSize;
            return this;
        }

        public Builder sessionTimeout(Integer sessionTimeout) {
            this.sessionTimeout = sessionTimeout;
            return this;
        }

        public Builder keyStoreAlias(String keyStoreAlias) {
            this.keyStoreAlias = keyStoreAlias;
            return this;
        }

        public Builder keyStoreRelativeTo(String keyStoreRelativeTo) {
            this.keyStoreRelativeTo = keyStoreRelativeTo;
            return this;
        }

        public Builder keyStoreRequired(Boolean keyStoreRequired) {
            this.keyStoreRequired = keyStoreRequired;
            return this;
        }

        public Builder trustStoreAlias(String trustStoreAlias) {
            this.trustStoreAlias = trustStoreAlias;
            return this;
        }

        public Builder trustStoreRelativeTo(String trustStoreRelativeTo) {
            this.trustStoreRelativeTo = trustStoreRelativeTo;
            return this;
        }

        public Builder trustStoreRequired(Boolean trustStoreRequired) {
            this.trustStoreRequired = trustStoreRequired;
            return this;
        }

        public Builder algorithm(String algorithm) {
            this.algorithm = algorithm;
            return this;
        }

        public Builder trustStoreProviders(String trustStoreProviders) {
            this.trustStoreProviders = trustStoreProviders;
            return this;
        }

        public Builder keyStoreProviders(String keyStoreProviders) {
            this.keyStoreProviders = keyStoreProviders;
            return this;
        }

        public Builder keyManagerProviders(String keyManagerProviders) {
            this.keyManagerProviders = keyManagerProviders;
            return this;
        }

        public Builder trustManagerProviders(String trustManagerProviders) {
            this.trustManagerProviders = trustManagerProviders;
            return this;
        }

        public Builder providers(String providers) {
            this.providers = providers;
            return this;
        }

        /**
         * Set this providers to all server ssl context resources which can
         * specify that (key-store, key-manager, trust-manager)
         *
         * @param providersAll
         *            - providers to ser
         * @return builder instance
         */
        public Builder providersAll(String providersAll) {
            this.keyStoreProviders = providersAll;
            this.trustStoreProviders = providersAll;
            this.keyManagerProviders = providersAll;
            this.trustManagerProviders = providersAll;
            this.providers = providersAll;
            return this;
        }

        public CreateServerSSLContext build() {
            if (keyStorePassword == null || keyStorePassword.isEmpty()) {
                throw new IllegalArgumentException("Key store password of the ssl-context must not be empty value");
            }
            if (keyPassword == null || keyPassword.isEmpty()) {
                throw new IllegalArgumentException("Key store item password of the ssl-context must not be empty value");
            }
            return new CreateServerSSLContext(this);
        }

    }

}
