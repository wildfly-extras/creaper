def securityRealm = management.'security-realms'.'security-realm'
def isSecurityRealmExist = securityRealm.any { it.'@name' == atrSecurityRealmName }
if (!isSecurityRealmExist) {
    throw new IllegalStateException("Security realm with name $atrSecurityRealmName does not exist.")
}

ldapAttrs = ['connection': atrConnection, 'base-dn': atrBaseDn]
if (atrAllowEmptyPasswords != null) ldapAttrs['allow-empty-passwords'] = atrAllowEmptyPasswords
if (atrRecursive != null) ldapAttrs['recursive'] = atrRecursive
if (atrUserDn != null) ldapAttrs['user-dn'] = atrUserDn
if (atrUsernameLoad != null) ldapAttrs['username-load'] = atrUsernameLoad

def ldapDefinition = {
    'ldap'(ldapAttrs) {
        if (atrCache) {
            cacheAttrs=[:]
            if (atrCache.byAccessTime) cacheAttrs['type'] = 'by-access-time'
            if (atrCache.bySearchTime) cacheAttrs['type'] = 'by-search-time'
            if (atrCache.cacheFailures != null) cacheAttrs['cache-failures'] = atrCache.cacheFailures
            if (atrCache.evictionTime != null) cacheAttrs['eviction-time'] = atrCache.evictionTime
            if (atrCache.maxCacheSize != null) cacheAttrs['max-cache-size'] = atrCache.maxCacheSize
            'cache'(cacheAttrs)
        }
        if (atrAdvancedFilter != null) 'advanced-filter'(['filter': atrAdvancedFilter])
        if (atrUsernameAttribute != null) 'username-filter'(['attribute': atrUsernameAttribute])
    }
}

def usedSecurityRealm = securityRealm.find { it.'@name' == atrSecurityRealmName }
def authenticationElement = usedSecurityRealm.authentication
def isExistingAuthentication = authenticationElement.any { it.name() == 'authentication' }

if (isExistingAuthentication) {
    def isPropertiesAuthnExist = authenticationElement.properties.any { it.name() == 'properties' }
    if (isPropertiesAuthnExist) {
        throw new IllegalStateException("Security realm with name $atrSecurityRealmName already contains username/password based authentication.")
    }
    def isJaasAuthnExist = authenticationElement.jaas.any { it.name() == 'jaas' }
    if (isJaasAuthnExist) {
        throw new IllegalStateException("Security realm with name $atrSecurityRealmName already contains username/password based authentication.")
    }
    def isLdapExists = authenticationElement.ldap.any { it.name() == 'ldap' }
    if (isLdapExists && !atrReplaceExisting) {
        throw new IllegalStateException("Ldap authnetication already exists in security realm with name $atrSecurityRealmName.")
    } else {
        if (isLdapExists) {
            authenticationElement.ldap.find().replaceNode ldapDefinition
        } else {
            authenticationElement.appendNode ldapDefinition
        }
    }
} else {
    usedSecurityRealm.appendNode {
        'authentication'(ldapDefinition)
    }
}
