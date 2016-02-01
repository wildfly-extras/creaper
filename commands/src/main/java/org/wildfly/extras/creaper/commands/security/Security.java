package org.wildfly.extras.creaper.commands.security;

/**
 * A slightly more convenient way to create commands that affect the security subsystem.
 */
public class Security {

    private Security() {
    }

    public static SecurityDomain securityDomain(String name) {
        return new SecurityDomain(name);
    }

    public static final class SecurityDomain {

        private final String name;

        private SecurityDomain(String name) {
            this.name = name;
        }

        public AddSecurityDomain.Builder add() {
            return new AddSecurityDomain.Builder(name);
        }

        public RemoveSecurityDomain remove() {
            return new RemoveSecurityDomain(name);
        }

        public AddLoginModule.Builder addLoginModule(String loginModuleCode) {
            return new AddLoginModule.Builder(loginModuleCode).securityDomainName(name);
        }

        public AddLoginModule.Builder addLoginModule(String loginModuleCode, String loginModuleName) {
            return new AddLoginModule.Builder(loginModuleCode, loginModuleName).securityDomainName(name);
        }

        public RemoveLoginModule removeLoginModule(String loginModuleName) {
            return new RemoveLoginModule(name, loginModuleName);
        }

        public AddAuthorizationModule.Builder addAuthorizationModule(String loginModuleCode) {
            return new AddAuthorizationModule.Builder(loginModuleCode).securityDomainName(name);
        }

        public AddAuthorizationModule.Builder addAuthorizationModule(String loginModuleCode,
                String authorizationModuleName) {
            return new AddAuthorizationModule.Builder(loginModuleCode, authorizationModuleName)
                    .securityDomainName(name);
        }

        public RemoveAuthorizationModule removeAuthorizationModule(String authorizationModuleName) {
            return new RemoveAuthorizationModule(name, authorizationModuleName);
        }

        public AddMappingModule.Builder addMappingModule(String mappingModuleCode) {
            return new AddMappingModule.Builder(mappingModuleCode).securityDomainName(name);
        }

        public AddMappingModule.Builder addMappingModule(String loginModuleCode, String mappingModuleName) {
            return new AddMappingModule.Builder(loginModuleCode, mappingModuleName).securityDomainName(name);
        }

        public RemoveMappingModule removeMappingModule(String mappingModuleName) {
            return new RemoveMappingModule(name, mappingModuleName);
        }

        public AddLoginModule.UsersRolesBuilder addUsersRolesLoginModule() {
            return new AddLoginModule.UsersRolesBuilder().securityDomainName(name);
        }

        public AddLoginModule.UsersRolesBuilder addUsersRolesLoginModule(String loginModuleCode) {
            return new AddLoginModule.UsersRolesBuilder(loginModuleCode).securityDomainName(name);
        }

        public AddLoginModule.AdvancedLdapBuilder addAdvancedLdapLoginModule() {
            return new AddLoginModule.AdvancedLdapBuilder().securityDomainName(name);
        }

        public AddLoginModule.AdvancedLdapBuilder addAdvancedLdapLoginModule(String loginModuleCode) {
            return new AddLoginModule.AdvancedLdapBuilder(loginModuleCode).securityDomainName(name);
        }

        public AddLoginModule.CertificateBuilder addCertificateLoginModule() {
            return new AddLoginModule.CertificateBuilder().securityDomainName(name);
        }

        public AddLoginModule.CertificateBuilder addCertificateLoginModule(String loginModuleCode) {
            return new AddLoginModule.CertificateBuilder(loginModuleCode).securityDomainName(name);
        }

        public AddLoginModule.CertificateRolesBuilder addCertificateRolesLoginModule() {
            return new AddLoginModule.CertificateRolesBuilder().securityDomainName(name);
        }

        public AddLoginModule.CertificateRolesBuilder addCertificateRolesLoginModule(String loginModuleCode) {
            return new AddLoginModule.CertificateRolesBuilder(loginModuleCode).securityDomainName(name);
        }

        public AddLoginModule.DatabaseCertificateBuilder addDatabaseCertificateLoginModule() {
            return new AddLoginModule.DatabaseCertificateBuilder().securityDomainName(name);
        }

        public AddLoginModule.DatabaseCertificateBuilder addDatabaseCertificateLoginModule(String loginModuleCode) {
            return new AddLoginModule.DatabaseCertificateBuilder(loginModuleCode).securityDomainName(name);
        }

        public AddLoginModule.DatabaseBuilder addDatabaseLoginModule() {
            return new AddLoginModule.DatabaseBuilder().securityDomainName(name);
        }

        public AddLoginModule.DatabaseBuilder addDatabaseLoginModule(String loginModuleCode) {
            return new AddLoginModule.DatabaseBuilder(loginModuleCode).securityDomainName(name);
        }

        public AddLoginModule.KerberosBuilder addKerberosLoginModule() {
            return new AddLoginModule.KerberosBuilder().securityDomainName(name);
        }

        public AddLoginModule.KerberosBuilder addKerberosLoginModule(String loginModuleCode) {
            return new AddLoginModule.KerberosBuilder(loginModuleCode).securityDomainName(name);
        }

        public AddLoginModule.LdapExtendedBuilder addLdapExtendedLoginModule() {
            return new AddLoginModule.LdapExtendedBuilder().securityDomainName(name);
        }

        public AddLoginModule.LdapExtendedBuilder addLdapExtendedLoginModule(String loginModuleCode) {
            return new AddLoginModule.LdapExtendedBuilder(loginModuleCode).securityDomainName(name);
        }

        public AddLoginModule.LdapBuilder addLdapLoginModule() {
            return new AddLoginModule.LdapBuilder().securityDomainName(name);
        }

        public AddLoginModule.LdapBuilder addLdapLoginModule(String loginModuleCode) {
            return new AddLoginModule.LdapBuilder(loginModuleCode).securityDomainName(name);
        }

        public AddLoginModule.RealmDirectBuilder addRealmDirectLoginModule() {
            return new AddLoginModule.RealmDirectBuilder().securityDomainName(name);
        }

        public AddLoginModule.RealmDirectBuilder addRealmDirectLoginModule(String loginModuleCode) {
            return new AddLoginModule.RealmDirectBuilder(loginModuleCode).securityDomainName(name);
        }

        public AddLoginModule.SpnegoBuilder addSpnegoLoginModule() {
            return new AddLoginModule.SpnegoBuilder().securityDomainName(name);
        }

        public AddLoginModule.SpnegoBuilder addSpnegoLoginModule(String loginModuleCode) {
            return new AddLoginModule.SpnegoBuilder(loginModuleCode).securityDomainName(name);
        }

    }

}
