package org.wildfly.extras.creaper.commands.security;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.CliException;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

/**
 * Generic command for adding new authentication classic login module to given security domain. For commonly used
 * login-modules, it's preferred to use the static nested classes builders which extend {@code Builder}, because they
 * apply some common configuration. Original {@code Builder} class is supposed to be used for left over login modules or
 * when absolute control is desired.
 */
public final class AddLoginModule implements OnlineCommand, OfflineCommand {

    private final String securityDomainName;
    private final String name;
    private final String code;
    private final String flag;
    private final String module;
    private final Map<String, String> moduleOptions;
    private final boolean replaceExisting;

    protected AddLoginModule(Builder builder) {
        this.securityDomainName = builder.securityDomainName;
        this.name = builder.name;
        this.code = builder.code;
        this.flag = builder.flag;
        this.module = builder.module;
        this.moduleOptions = builder.moduleOptions;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws CliException, CommandFailedException, IOException,
            TimeoutException, InterruptedException {
        if (ctx.version.greaterThanOrEqualTo(ServerVersion.VERSION_18_0_0)) {
            throw new AssertionError("Legacy security was removed in WildFly 25.");
        }

        Operations ops = new Operations(ctx.client);
        Address authenticationClassicAddress = Address.subsystem("security")
                .and("security-domain", securityDomainName)
                .and("authentication", "classic");
        try {
            boolean exists = ops.exists(authenticationClassicAddress);
            if (!exists) {
                ops.add(authenticationClassicAddress);
            }
        } catch (OperationException e) {
            throw new IOException("Failed to access or create authentication=classic in security domain "
                    + securityDomainName, e);
        }

        Address loginModuleAddress = authenticationClassicAddress.and("login-module", name);

        if (replaceExisting) {
            try {
                ops.removeIfExists(loginModuleAddress);
            } catch (OperationException e) {
                throw new IOException("Failed to remove existing login module " + name + " in security domain "
                        + securityDomainName, e);
            }
        }

        ops.add(loginModuleAddress, Values.empty()
                .andOptional("code", code)
                .andOptional("flag", flag)
                .andOptional("module", module)
                .andObjectOptional("module-options", Values.fromMap(moduleOptions))
        );
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {
        if (ctx.version.greaterThanOrEqualTo(ServerVersion.VERSION_18_0_0)) {
            throw new AssertionError("Legacy security was removed in WildFly 25.");
        }

        ctx.client.apply(GroovyXmlTransform.of(AddLoginModule.class)
                .subtree("securitySubsystem", Subtree.subsystem("security"))
                .parameter("atrSecurityDomainName", securityDomainName)
                .parameter("atrName", name)
                .parameter("atrCode", code)
                .parameter("atrFlag", flag)
                .parameter("atrModule", module)
                .parameter("atrModuleOptions", moduleOptions)
                .parameter("atrReplaceExisting", replaceExisting)
                .build());
    }

    /**
     * Builder for configuration attributes of a Login Module. The {@code THIS} type parameter is only meant to be used
     * by subclasses. If you're not inheriting from this class, don't use it.
     */
    public static class Builder<THIS extends Builder> {

        private String securityDomainName;
        private String name;
        private String code;
        private String flag;
        private String module;
        private final Map<String, String> moduleOptions = new LinkedHashMap<String, String>();
        private boolean replaceExisting;

        /**
         * In case when you use this constructor then login module name is the same as its code.
         */
        public Builder(String code) {
            this(code, code);
        }

        public Builder(String code, String name) {
            if (code == null) {
                throw new IllegalArgumentException("Code of the login module must be specified as non null value");
            }
            if (name == null) {
                throw new IllegalArgumentException("Name of the login module must be specified as non null value");
            }
            if (code.isEmpty()) {
                throw new IllegalArgumentException("Code of the login module must not be empty value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the login module must not be empty value");
            }
            this.code = code;
            this.name = name;
        }

        public final THIS securityDomainName(String securityDomainName) {
            this.securityDomainName = securityDomainName;
            return (THIS) this;
        }

        public final THIS flag(String flag) {
            this.flag = flag;
            return (THIS) this;
        }

        public final THIS module(String module) {
            this.module = module;
            return (THIS) this;
        }

        public final THIS addModuleOption(String name, String value) {
            moduleOptions.put(name, value);
            return (THIS) this;
        }

        public final THIS replaceExisting() {
            this.replaceExisting = true;
            return (THIS) this;
        }

        public final AddLoginModule build() {
            if (securityDomainName == null) {
                throw new IllegalArgumentException("Name of the security-domain must be specified as non null value");
            }
            if (securityDomainName.isEmpty()) {
                throw new IllegalArgumentException("Name of the security-domain must not be empty value");
            }
            if (flag == null) {
                throw new IllegalArgumentException("Flag of the security-domain must be specified as non null value");
            }
            return new AddLoginModule(this);
        }

    }

    public static final class AdvancedLdapBuilder extends AddLoginModule.Builder<AdvancedLdapBuilder> {

        private static final String ADVANCED_LDAP = "AdvancedLdap";

        public AdvancedLdapBuilder() {
            super(ADVANCED_LDAP);
        }

        public AdvancedLdapBuilder(String name) {
            super(ADVANCED_LDAP, name);
        }

        public AdvancedLdapBuilder javaNamingFactoryInitial(String javaNamingFactoryInitial) {
            return addModuleOption("javaNamingFactoryInitial", javaNamingFactoryInitial);
        }

        public AdvancedLdapBuilder javaNamingProviderUrl(String javaNamingProviderUrl) {
            return addModuleOption("javaNamingProviderUrl", javaNamingProviderUrl);
        }

        public AdvancedLdapBuilder javaNamingSecurityAuthentication(String javaNamingSecurityAuthentication) {
            return addModuleOption("javaNamingSecurityAuthentication", javaNamingSecurityAuthentication);
        }

        public AdvancedLdapBuilder javaNamingSecurityProtocol(String javaNamingSecurityProtocol) {
            return addModuleOption("javaNamingSecurityProtocol", javaNamingSecurityProtocol);
        }

        public AdvancedLdapBuilder javaNamingSecurityPrincipal(String javaNamingSecurityPrincipal) {
            return addModuleOption("javaNamingSecurityPrincipal", javaNamingSecurityPrincipal);
        }

        public AdvancedLdapBuilder javaNamingSecurityCredentials(String javaNamingSecurityCredentials) {
            return addModuleOption("javaNamingSecurityCredentials", javaNamingSecurityCredentials);
        }

        public AdvancedLdapBuilder bindDN(String bindDN) {
            return addModuleOption("bindDN", bindDN);
        }

        public AdvancedLdapBuilder bindCredential(String bindCredential) {
            return addModuleOption("bindCredential", bindCredential);
        }

        public AdvancedLdapBuilder jaasSecurityDomain(String jaasSecurityDomain) {
            return addModuleOption("jaasSecurityDomain", jaasSecurityDomain);
        }

        public AdvancedLdapBuilder baseCtxDN(String baseCtxDN) {
            return addModuleOption("baseCtxDN", baseCtxDN);
        }

        public AdvancedLdapBuilder baseFilter(String baseFilter) {
            return addModuleOption("baseFilter", baseFilter);
        }

        public AdvancedLdapBuilder searchTimeLimit(String searchTimeLimit) {
            return addModuleOption("searchTimeLimit", searchTimeLimit);
        }

        public AdvancedLdapBuilder roleAttributeID(String roleAttributeID) {
            return addModuleOption("roleAttributeID", roleAttributeID);
        }

        public AdvancedLdapBuilder rolesCtxDN(String rolesCtxDN) {
            return addModuleOption("rolesCtxDN", rolesCtxDN);
        }

        public AdvancedLdapBuilder roleFilter(String roleFilter) {
            return addModuleOption("roleFilter", roleFilter);
        }

        public AdvancedLdapBuilder roleNameAttributeID(String roleNameAttributeID) {
            return addModuleOption("roleNameAttributeID", roleNameAttributeID);
        }

        public AdvancedLdapBuilder referralUserAttributeIDToCheck(String referralUserAttributeIDToCheck) {
            return addModuleOption("referralUserAttributeIDToCheck", referralUserAttributeIDToCheck);
        }

        public AdvancedLdapBuilder searchScope(String searchScope) {
            return addModuleOption("searchScope", searchScope);
        }

        public AdvancedLdapBuilder bindAuthentication(String bindAuthentication) {
            return addModuleOption("bindAuthentication", bindAuthentication);
        }

        public AdvancedLdapBuilder roleAttributeIsDN(boolean roleAttributeIsDN) {
            return addModuleOption("roleAttributeIsDN", Boolean.toString(roleAttributeIsDN));
        }

        public AdvancedLdapBuilder recurseRoles(boolean recurseRoles) {
            return addModuleOption("recurseRoles", Boolean.toString(recurseRoles));
        }

        public AdvancedLdapBuilder allowEmptyPassword(boolean allowEmptyPassword) {
            return addModuleOption("allowEmptyPassword", Boolean.toString(allowEmptyPassword));
        }

    }

    public static final class CertificateBuilder extends AddLoginModule.Builder<CertificateBuilder> {

        private static final String CERTIFICATE = "Certificate";

        public CertificateBuilder() {
            super(CERTIFICATE);
        }

        public CertificateBuilder(String name) {
            super(CERTIFICATE, name);
        }

        public CertificateBuilder securityDomain(String securityDomain) {
            return addModuleOption("securityDomain", securityDomain);
        }

        public CertificateBuilder verifier(String verifier) {
            return addModuleOption("verifier", verifier);
        }

    }

    public static final class CertificateRolesBuilder extends AddLoginModule.Builder<CertificateRolesBuilder> {

        private static final String CERTIFICATE_ROLES = "CertificateRoles";

        public CertificateRolesBuilder() {
            super(CERTIFICATE_ROLES);
        }

        public CertificateRolesBuilder(String name) {
            super(CERTIFICATE_ROLES, name);
        }

        public CertificateRolesBuilder securityDomain(String securityDomain) {
            return addModuleOption("securityDomain", securityDomain);
        }

        public CertificateRolesBuilder verifier(String verifier) {
            return addModuleOption("verifier", verifier);
        }

        public CertificateRolesBuilder rolesProperties(String rolesProperties) {
            return addModuleOption("rolesProperties", rolesProperties);
        }

        public CertificateRolesBuilder defaultRolesProperties(String defaultRolesProperties) {
            return addModuleOption("defaultRolesProperties", defaultRolesProperties);
        }

        public CertificateRolesBuilder roleGroupSeparator(String roleGroupSeparator) {
            return addModuleOption("roleGroupSeparator", roleGroupSeparator);
        }

    }

    public static final class DatabaseCertificateBuilder extends AddLoginModule.Builder<DatabaseCertificateBuilder> {

        private static final String DATABASE_CERTIFICATE = "DatabaseCertificate";

        public DatabaseCertificateBuilder() {
            super(DATABASE_CERTIFICATE);
        }

        public DatabaseCertificateBuilder(String name) {
            super(DATABASE_CERTIFICATE, name);
        }

        public DatabaseCertificateBuilder securityDomain(String securityDomain) {
            return addModuleOption("securityDomain", securityDomain);
        }

        public DatabaseCertificateBuilder verifier(String verifier) {
            return addModuleOption("verifier", verifier);
        }

        public DatabaseCertificateBuilder dsJndiName(String dsJndiName) {
            return addModuleOption("dsJndiName", dsJndiName);
        }

        public DatabaseCertificateBuilder rolesQuery(String rolesQuery) {
            return addModuleOption("rolesQuery", rolesQuery);
        }

        public DatabaseCertificateBuilder transactionManagerJndiName(String transactionManagerJndiName) {
            return addModuleOption("transactionManagerJndiName", transactionManagerJndiName);
        }

        public DatabaseCertificateBuilder suspendResume(boolean suspendResume) {
            return addModuleOption("suspendResume", Boolean.toString(suspendResume));
        }
    }

    public static final class DatabaseBuilder extends AddLoginModule.Builder<DatabaseBuilder> {

        private static final String DATABASE = "Database";

        public DatabaseBuilder() {
            super(DATABASE);
        }

        public DatabaseBuilder(String name) {
            super(DATABASE, name);
        }

        public DatabaseBuilder dsJndiName(String dsJndiName) {
            return addModuleOption("dsJndiName", dsJndiName);
        }

        public DatabaseBuilder principalsQuery(String principalsQuery) {
            return addModuleOption("principalsQuery", principalsQuery);
        }

        public DatabaseBuilder rolesQuery(String rolesQuery) {
            return addModuleOption("rolesQuery", rolesQuery);
        }

        public DatabaseBuilder transactionManagerJndiName(String transactionManagerJndiName) {
            return addModuleOption("transactionManagerJndiName", transactionManagerJndiName);
        }

        public DatabaseBuilder suspendResume(boolean suspendResume) {
            return addModuleOption("suspendResume", Boolean.toString(suspendResume));
        }
    }

    public static final class KerberosBuilder extends AddLoginModule.Builder<KerberosBuilder> {

        private static final String KERBEROS = "Kerberos";

        public KerberosBuilder() {
            super(KERBEROS);
        }

        public KerberosBuilder(String name) {
            super(KERBEROS, name);
        }

        public KerberosBuilder delegationCredential(String delegationCredential) {
            return addModuleOption("delegationCredential", delegationCredential);
        }

        public KerberosBuilder credentialLifetime(String credentialLifetime) {
            return addModuleOption("credentialLifetime", credentialLifetime);
        }

        public KerberosBuilder addGSSCredential(boolean addGSSCredential) {
            return addModuleOption("addGSSCredential", Boolean.toString(addGSSCredential));
        }

        public KerberosBuilder wrapGSSCredential(boolean wrapGSSCredential) {
            return addModuleOption("wrapGSSCredential", Boolean.toString(wrapGSSCredential));
        }

    }

    public static final class LdapExtendedBuilder extends AddLoginModule.Builder<LdapExtendedBuilder> {

        private static final String LDAP_EXT = "LdapExtended";

        public LdapExtendedBuilder() {
            super(LDAP_EXT);
        }

        public LdapExtendedBuilder(String name) {
            super(LDAP_EXT, name);
        }

        public LdapExtendedBuilder javaNamingFactoryInitial(String javaNamingFactoryInitial) {
            return addModuleOption("javaNamingFactoryInitial", javaNamingFactoryInitial);
        }

        public LdapExtendedBuilder javaNamingProviderUrl(String javaNamingProviderUrl) {
            return addModuleOption("javaNamingProviderUrl", javaNamingProviderUrl);
        }

        public LdapExtendedBuilder javaNamingSecurityAuthentication(String javaNamingSecurityAuthentication) {
            return addModuleOption("javaNamingSecurityAuthentication", javaNamingSecurityAuthentication);
        }

        public LdapExtendedBuilder javaNamingSecurityProtocol(String javaNamingSecurityProtocol) {
            return addModuleOption("javaNamingSecurityProtocol", javaNamingSecurityProtocol);
        }

        public LdapExtendedBuilder javaNamingSecurityPrincipal(String javaNamingSecurityPrincipal) {
            return addModuleOption("javaNamingSecurityPrincipal", javaNamingSecurityPrincipal);
        }

        public LdapExtendedBuilder javaNamingSecurityCredentials(String javaNamingSecurityCredentials) {
            return addModuleOption("javaNamingSecurityCredentials", javaNamingSecurityCredentials);
        }

        public LdapExtendedBuilder baseCtxDN(String baseCtxDN) {
            return addModuleOption("baseCtxDN", baseCtxDN);
        }

        public LdapExtendedBuilder bindCredential(String bindCredential) {
            return addModuleOption("bindCredential", bindCredential);
        }

        public LdapExtendedBuilder bindDN(String bindDN) {
            return addModuleOption("bindDN", bindDN);
        }

        public LdapExtendedBuilder baseFilter(String baseFilter) {
            return addModuleOption("baseFilter", baseFilter);
        }

        public LdapExtendedBuilder jaasSecurityDomain(String jaasSecurityDomain) {
            return addModuleOption("jaasSecurityDomain", jaasSecurityDomain);
        }

        public LdapExtendedBuilder rolesCtxDN(String rolesCtxDN) {
            return addModuleOption("rolesCtxDN", rolesCtxDN);
        }

        public LdapExtendedBuilder roleFilter(String roleFilter) {
            return addModuleOption("roleFilter", roleFilter);
        }

        public LdapExtendedBuilder roleAttributeID(String roleAttributeID) {
            return addModuleOption("roleAttributeID", roleAttributeID);
        }

        public LdapExtendedBuilder defaultRole(String defaultRole) {
            return addModuleOption("defaultRole", defaultRole);
        }

        public LdapExtendedBuilder usernameBeginString(String usernameBeginString) {
            return addModuleOption("usernameBeginString", usernameBeginString);
        }

        public LdapExtendedBuilder usernameEndString(String usernameEndString) {
            return addModuleOption("usernameEndString", usernameEndString);
        }

        public LdapExtendedBuilder roleNameAttributeID(String roleNameAttributeID) {
            return addModuleOption("roleNameAttributeID", roleNameAttributeID);
        }

        public LdapExtendedBuilder distinguishedNameAttribute(String distinguishedNameAttribute) {
            return addModuleOption("distinguishedNameAttribute", distinguishedNameAttribute);
        }

        public LdapExtendedBuilder roleRecursion(String roleRecursion) {
            return addModuleOption("roleRecursion", roleRecursion);
        }

        public LdapExtendedBuilder searchTimeLimit(String searchTimeLimit) {
            return addModuleOption("searchTimeLimit", searchTimeLimit);
        }

        public LdapExtendedBuilder searchScope(String searchScope) {
            return addModuleOption("searchScope", searchScope);
        }

        public LdapExtendedBuilder referralUserAttributeIDToCheck(String referralUserAttributeIDToCheck) {
            return addModuleOption("referralUserAttributeIDToCheck", referralUserAttributeIDToCheck);
        }

        public LdapExtendedBuilder roleAttributeIsDN(boolean roleAttributeIsDN) {
            return addModuleOption("roleAttributeIsDN", Boolean.toString(roleAttributeIsDN));
        }

        public LdapExtendedBuilder parseRoleNameFromDN(boolean parseRoleNameFromDN) {
            return addModuleOption("parseRoleNameFromDN", Boolean.toString(parseRoleNameFromDN));
        }

        public LdapExtendedBuilder parseUsername(boolean parseUsername) {
            return addModuleOption("parseUsername", Boolean.toString(parseUsername));
        }

        public LdapExtendedBuilder allowEmptyPasswords(boolean allowEmptyPasswords) {
            return addModuleOption("allowEmptyPasswords", Boolean.toString(allowEmptyPasswords));
        }

    }

    public static final class LdapBuilder extends AddLoginModule.Builder<LdapBuilder> {

        private static final String LDAP = "Ldap";

        public LdapBuilder() {
            super(LDAP);
        }

        public LdapBuilder(String name) {
            super(LDAP, name);
        }

        public LdapBuilder javaNamingFactoryInitial(String javaNamingFactoryInitial) {
            return addModuleOption("javaNamingFactoryInitial", javaNamingFactoryInitial);
        }

        public LdapBuilder javaNamingProviderUrl(String javaNamingProviderUrl) {
            return addModuleOption("javaNamingProviderUrl", javaNamingProviderUrl);
        }

        public LdapBuilder javaNamingSecurityAuthentication(String javaNamingSecurityAuthentication) {
            return addModuleOption("javaNamingSecurityAuthentication", javaNamingSecurityAuthentication);
        }

        public LdapBuilder javaNamingSecurityProtocol(String javaNamingSecurityProtocol) {
            return addModuleOption("javaNamingSecurityProtocol", javaNamingSecurityProtocol);
        }

        public LdapBuilder javaNamingSecurityPrincipal(String javaNamingSecurityPrincipal) {
            return addModuleOption("javaNamingSecurityPrincipal", javaNamingSecurityPrincipal);
        }

        public LdapBuilder javaNamingSecurityCredentials(String javaNamingSecurityCredentials) {
            return addModuleOption("javaNamingSecurityCredentials", javaNamingSecurityCredentials);
        }

        public LdapBuilder principalDNPrefix(String principalDNPrefix) {
            return addModuleOption("principalDNPrefix", principalDNPrefix);
        }

        public LdapBuilder principalDNSuffix(String principalDNSuffix) {
            return addModuleOption("principalDNSuffix", principalDNSuffix);
        }

        public LdapBuilder rolesCtxDN(String rolesCtxDN) {
            return addModuleOption("rolesCtxDN", rolesCtxDN);
        }

        public LdapBuilder userRolesCtxDNAttributeName(String userRolesCtxDNAttributeName) {
            return addModuleOption("userRolesCtxDNAttributeName", userRolesCtxDNAttributeName);
        }

        public LdapBuilder roleAttributeID(String roleAttributeID) {
            return addModuleOption("roleAttributeID", roleAttributeID);
        }

        public LdapBuilder roleAttributeIsDN(boolean roleAttributeIsDN) {
            return addModuleOption("roleAttributeIsDN", Boolean.toString(roleAttributeIsDN));
        }

        public LdapBuilder roleNameAttributeID(String roleNameAttributeID) {
            return addModuleOption("roleNameAttributeID", roleNameAttributeID);
        }

        public LdapBuilder uidAttributeID(String uidAttributeID) {
            return addModuleOption("uidAttributeID", uidAttributeID);
        }

        public LdapBuilder matchOnUserDN(boolean matchOnUserDN) {
            return addModuleOption("matchOnUserDN", Boolean.toString(matchOnUserDN));
        }

        public LdapBuilder allowEmptyPasswords(boolean allowEmptyPasswords) {
            return addModuleOption("allowEmptyPasswords", Boolean.toString(allowEmptyPasswords));
        }

        public LdapBuilder searchTimeLimit(String searchTimeLimit) {
            return addModuleOption("searchTimeLimit", searchTimeLimit);
        }

        public LdapBuilder searchScope(String searchScope) {
            return addModuleOption("searchScope", searchScope);
        }

        public LdapBuilder jaasSecurityDomain(String jaasSecurityDomain) {
            return addModuleOption("jaasSecurityDomain", jaasSecurityDomain);
        }

    }

    public static final class RealmDirectBuilder extends AddLoginModule.Builder<RealmDirectBuilder> {

        private static final String REALM_DIRECT = "RealmDirect";

        public RealmDirectBuilder() {
            super(REALM_DIRECT);
        }

        public RealmDirectBuilder(String name) {
            super(REALM_DIRECT, name);
        }

        public RealmDirectBuilder realm(String realm) {
            return addModuleOption("realm", realm);
        }

    }

    public static final class SpnegoBuilder extends AddLoginModule.Builder<SpnegoBuilder> {

        private static final String SPNEGO = "SPNEGO";

        public SpnegoBuilder() {
            super(SPNEGO);
        }

        public SpnegoBuilder(String name) {
            super(SPNEGO, name);
        }

        public SpnegoBuilder serverSecurityDomain(String serverSecurityDomain) {
            return addModuleOption("serverSecurityDomain", serverSecurityDomain);
        }

        public SpnegoBuilder usernamePasswordDomain(String usernamePasswordDomain) {
            return addModuleOption("usernamePasswordDomain", usernamePasswordDomain);
        }

        public SpnegoBuilder removeRealmFromPrincipal(boolean removeRealmFromPrincipal) {
            return addModuleOption("removeRealmFromPrincipal", Boolean.toString(removeRealmFromPrincipal));
        }

    }

    public static final class UsersRolesBuilder extends AddLoginModule.Builder<UsersRolesBuilder> {

        private static final String USERS_ROLES = "UsersRoles";

        public UsersRolesBuilder() {
            super(USERS_ROLES);
        }

        public UsersRolesBuilder(String name) {
            super(USERS_ROLES, name);
        }

        public UsersRolesBuilder usersProperties(String usersProperties) {
            return addModuleOption("usersProperties", usersProperties);
        }

        public UsersRolesBuilder rolesProperties(String rolesProperties) {
            return addModuleOption("rolesProperties", rolesProperties);
        }

        public UsersRolesBuilder defaultUsersProperties(String defaultUsersProperties) {
            return addModuleOption("defaultUsersProperties", defaultUsersProperties);
        }

        public UsersRolesBuilder defaultRolesProperties(String defaultRolesProperties) {
            return addModuleOption("defaultRolesProperties", defaultRolesProperties);
        }

        public UsersRolesBuilder roleGroupSeperator(String roleGroupSeperator) {
            return addModuleOption("roleGroupSeperator", roleGroupSeperator);
        }

    }

}
