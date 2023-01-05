package org.wildfly.extras.creaper.commands.security.realms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Batch;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

/**
 * Add a new LDAP outbound connection.
 */
public final class AddLdapConnection implements OnlineCommand, OfflineCommand {

    private final String connectionName;
    private final List<String> handlesReferralsFor;
    private final String initialContextFactory;
    private final String referrals;
    private final String searchCredential;
    private final String searchDn;
    private final String securityRealm;
    private final String url;
    private final boolean replaceExisting;
    private final Map<String, String> properties;

    private AddLdapConnection(Builder builder) {
        this.connectionName = builder.connectionName;
        this.handlesReferralsFor = builder.handlesReferralsFor;
        this.initialContextFactory = builder.initialContextFactory;
        this.referrals = builder.referrals;
        this.searchCredential = builder.searchCredential;
        this.searchDn = builder.searchDn;
        this.securityRealm = builder.securityRealm;
        this.url = builder.url;
        this.replaceExisting = builder.replaceExisting;
        this.properties = builder.properties;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (ctx.version.greaterThanOrEqualTo(ServerVersion.VERSION_18_0_0)) {
            throw new AssertionError("Legacy security was removed in WildFly 25.");
        }

        Operations ops = new Operations(ctx.client);
        Address ldapConnection = Address.coreService("management").and("ldap-connection", connectionName);
        if (replaceExisting) {
            try {
                ops.removeIfExists(ldapConnection);
                new Administration(ctx.client).reloadIfRequired();
            } catch (OperationException e) {
                throw new IOException("Failed to LDAP outbound connection " + connectionName, e);
            }
        }

        List<String> resolvedHandlesReferralsFor = (handlesReferralsFor != null && !handlesReferralsFor.isEmpty())
                ? handlesReferralsFor
                : null;

        Batch batch = new Batch();
        batch.add(ldapConnection, Values.empty()
                .andOptional("name", connectionName)
                .andOptional("handles-referrals-for", initialContextFactory)
                .andOptional("initial-context-factory", initialContextFactory)
                .andOptional("referrals", referrals)
                .andOptional("search-credential", searchCredential)
                .andOptional("search-dn", searchDn)
                .andOptional("security-realm", securityRealm)
                .andOptional("url", url)
                .andListOptional(String.class, "handles-referrals-for", resolvedHandlesReferralsFor));

        if (properties != null && !properties.isEmpty()) {
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                batch.add(ldapConnection.and("property", entry.getKey()), Values.empty()
                        .and("value", entry.getValue())
                );
            }
        }

        ops.batch(batch);
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        if (ctx.version.greaterThanOrEqualTo(ServerVersion.VERSION_18_0_0)) {
            throw new AssertionError("Legacy security was removed in WildFly 25.");
        }

        ctx.client.apply(GroovyXmlTransform.of(AddLdapConnection.class)
                .subtree("management", Subtree.management())
                .parameter("atrConnectionName", connectionName)
                .parameter("atrHandlesReferralsFor", handlesReferralsFor)
                .parameter("atrInitialContextFactory", initialContextFactory)
                .parameter("atrReferrals", referrals)
                .parameter("atrSearchCredential", searchCredential)
                .parameter("atrSearchDn", searchDn)
                .parameter("atrSecurityRealm", securityRealm)
                .parameter("atrUrl", url)
                .parameter("atrReplaceExisting", replaceExisting)
                .parameter("atrProperties", properties)
                .build());
    }

    public static final class Builder {

        private final String connectionName;
        private List<String> handlesReferralsFor = new ArrayList<String>();
        private String initialContextFactory;
        private String referrals;
        private String searchCredential;
        private String searchDn;
        private String securityRealm;
        private String url;
        private boolean replaceExisting;
        private Map<String, String> properties = new LinkedHashMap<String, String>();

        public Builder(String connectionName) {
            if (connectionName == null) {
                throw new IllegalArgumentException("Connection name must be specified as non null value");
            }
            if (connectionName.isEmpty()) {
                throw new IllegalArgumentException("Connection name must not be empty value");
            }
            this.connectionName = connectionName;
        }

        public Builder initialContextFactory(String initialContextFactory) {
            this.initialContextFactory = initialContextFactory;
            return this;
        }

        public Builder referrals(String referrals) {
            this.referrals = referrals;
            return this;
        }

        public Builder searchCredential(String searchCredential) {
            this.searchCredential = searchCredential;
            return this;
        }

        public Builder searchDn(String searchDn) {
            this.searchDn = searchDn;
            return this;
        }

        public Builder securityRealm(String securityRealm) {
            this.securityRealm = securityRealm;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        /**
         * Add value for handles-referrals-for. Use this method for every particular handles-referrals-for URL.
         */
        public Builder addHandlesReferralsFor(String handlesReferralsFor) {
            this.handlesReferralsFor.add(handlesReferralsFor);
            return this;
        }

        /**
         * Set values of handles-referrals-for. It rewrites current handles-referrals-for values set in builder.
         */
        public Builder setHandlesReferralsFor(List<String> handlesReferralsFor) {
            this.handlesReferralsFor = handlesReferralsFor;
            return this;
        }

        /**
         * <b>This can cause server reload!</b>
         */
        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        /**
         * Add one property to properties.
         */
        public Builder addProperty(String name, String value) {
            properties.put(name, value);
            return this;
        }

        /**
         * Set properties. It rewrite properties. Properties set before are removed.
         */
        public Builder setProperties(Map<String, String> properties) {
            this.properties = properties;
            return this;
        }

        public AddLdapConnection build() {
            if (url == null) {
                throw new IllegalArgumentException("Url attribute must be specified as non null value");
            }
            if (url.isEmpty()) {
                throw new IllegalArgumentException("Url attribute must not be empty value");
            }
            return new AddLdapConnection(this);
        }
    }
}
