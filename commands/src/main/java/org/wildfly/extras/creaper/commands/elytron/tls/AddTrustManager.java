package org.wildfly.extras.creaper.commands.elytron.tls;

import java.util.HashMap;
import java.util.Map;

import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddTrustManager implements OnlineCommand, OfflineCommand {

    private final String name;
    private final String algorithm;
    private final String aliasFilter;
    private final String keyStore;
    private final String providerName;
    private final String providers;
    private final CertificateRevocationList certificateRevocationList;
    private final boolean replaceExisting;

    private AddTrustManager(Builder builder) {
        this.name = builder.name;
        this.algorithm = builder.algorithm;
        this.aliasFilter = builder.aliasFilter;
        this.keyStore = builder.keyStore;
        this.providerName = builder.providerName;
        this.providers = builder.providers;
        this.certificateRevocationList = builder.certificateRevocationList;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address trustManagerAddress = Address.subsystem("elytron").and("trust-manager", name);
        if (replaceExisting) {
            ops.removeIfExists(trustManagerAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(trustManagerAddress, Values.empty()
                .and("name", name)
                .and("key-store", keyStore)
                .andOptional("algorithm", algorithm)
                .andOptional("alias-filter", aliasFilter)
                .andOptional("provider-name", providerName)
                .andOptional("providers", providers)
                .andObjectOptional("certificate-revocation-list",
                        certificateRevocationList != null ? certificateRevocationList.toValues() : null));
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        ctx.client.apply(GroovyXmlTransform.of(AddTrustManager.class)
                .subtree("elytronSubsystem", Subtree.subsystem("elytron"))
                .parameter("atrName", name)
                .parameter("atrAlgorithm", algorithm)
                .parameter("atrAliasFilter", aliasFilter)
                .parameter("atrKeyStore", keyStore)
                .parameter("atrProviderName", providerName)
                .parameter("atrProviders", providers)
                .parameters(certificateRevocationList != null
                        ? certificateRevocationList.toParameters() : CertificateRevocationList.EMPTY_PARAMETERS)
                .parameter("atrReplaceExisting", replaceExisting)
                .build());
    }

    public static final class Builder {

        private final String name;
        private String algorithm;
        private String aliasFilter;
        private String keyStore;
        private String providerName;
        private String providers;
        private CertificateRevocationList certificateRevocationList;
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Name of the trust-manager must be specified as non empty value");
            }
            this.name = name;
        }

        public Builder algorithm(String algorithm) {
            this.algorithm = algorithm;
            return this;
        }

        public Builder aliasFilter(String aliasFilter) {
            this.aliasFilter = aliasFilter;
            return this;
        }

        public Builder keyStore(String keyStore) {
            this.keyStore = keyStore;
            return this;
        }

        public Builder providerName(String providerName) {
            this.providerName = providerName;
            return this;
        }

        public Builder providers(String providers) {
            this.providers = providers;
            return this;
        }

        public Builder certificateRevocationList(CertificateRevocationList certificateRevocationList) {
            this.certificateRevocationList = certificateRevocationList;
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddTrustManager build() {
            return new AddTrustManager(this);
        }
    }

    public static final class CertificateRevocationList {

        static final Map<String, Object> EMPTY_PARAMETERS = new HashMap<String, Object>();

        static {
            EMPTY_PARAMETERS.put("atrCrl", false);
            EMPTY_PARAMETERS.put("atrCrlPath", null);
            EMPTY_PARAMETERS.put("atrCrlRelativeTo", null);
            EMPTY_PARAMETERS.put("atrCrlMaximumCertPath", null);
        }

        private final String path;
        private final String relativeTo;
        private final Integer maximumCertPath;

        private CertificateRevocationList(CertificateRevocationListBuilder builder) {
            this.path = builder.path;
            this.relativeTo = builder.relativeTo;
            this.maximumCertPath = builder.maximumCertPath;
        }

        public String getPath() {
            return path;
        }

        public String getRelativeTo() {
            return relativeTo;
        }

        public Integer getMaximumCertPath() {
            return maximumCertPath;
        }

        public Values toValues() {
            return Values.empty()
                    .andOptional("path", path)
                    .andOptional("relative-to", relativeTo)
                    .andOptional("maximum-cert-path", maximumCertPath);
        }

        public Map<String, Object> toParameters() {
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("atrCrl", true);
            parameters.put("atrCrlPath", path);
            parameters.put("atrCrlRelativeTo", relativeTo);
            parameters.put("atrCrlMaximumCertPath", maximumCertPath);
            return parameters;
        }
    }

    public static final class CertificateRevocationListBuilder {
        private String path;
        private String relativeTo;
        private Integer maximumCertPath;

        public CertificateRevocationListBuilder path(String path) {
            this.path = path;
            return this;
        }

        public CertificateRevocationListBuilder relativeTo(String relativeTo) {
            this.relativeTo = relativeTo;
            return this;
        }

        public CertificateRevocationListBuilder maximumCertPath(int maximumCertPath) {
            this.maximumCertPath = maximumCertPath;
            return this;
        }

        public CertificateRevocationList build() {
            if (relativeTo != null && path == null) {
                throw new IllegalArgumentException("relativeTo requires path to be set");
            }
            return new CertificateRevocationList(this);
        }

    }
}
