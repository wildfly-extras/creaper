def securityRealm = management.'security-realms'.'security-realm'
def isSecurityRealmExist = securityRealm.any { it.'@name' == atrSecurityRealmName }
if (!isSecurityRealmExist) {
    throw new IllegalStateException("Security realm with name $atrSecurityRealmName does not exist.")
}

ldapAttrs = ['connection': atrConnection]

def ldapDefinition = {
    'ldap'(ldapAttrs) {
        if (atrAdvancedFilter != null) {
            usernameToDnAttrs = [:]
            if (atrAdvancedFilter.force != null) usernameToDnAttrs['force'] = atrAdvancedFilter.force
            'username-to-dn'(usernameToDnAttrs) {
                cache = atrAdvancedFilter.cache
                if (cache) {
                    'cache'(resolveCacheAttrs(cache))
                }
                advancedFilterAttrs = ['base-dn': atrAdvancedFilter.baseDn, 'filter': atrAdvancedFilter.filter]
                if (atrAdvancedFilter.recursive != null) advancedFilterAttrs['recursive'] = atrAdvancedFilter.recursive
                if (atrAdvancedFilter.userDnAttribute != null) advancedFilterAttrs['user-dn-attribute'] = atrAdvancedFilter.userDnAttribute
                'advanced-filter'(advancedFilterAttrs)
            }
        }
        if (atrUsernameFilter != null) {
            usernameToDnAttrs = [:]
            if (atrUsernameFilter.force != null) usernameToDnAttrs['force'] = atrUsernameFilter.force
            'username-to-dn'(usernameToDnAttrs) {
                cache = atrUsernameFilter.cache
                if (cache) {
                    'cache'(resolveCacheAttrs(cache))
                }
                usernameFilterAttrs = ['base-dn': atrUsernameFilter.baseDn, 'attribute': atrUsernameFilter.attribute]
                if (atrUsernameFilter.recursive != null) usernameFilterAttrs['recursive'] = atrUsernameFilter.recursive
                if (atrUsernameFilter.userDnAttribute != null) usernameFilterAttrs['user-dn-attribute'] = atrUsernameFilter.userDnAttribute
                'username-filter'(usernameFilterAttrs)
            }
        }
        if (atrUsernameIsDn != null) {
            usernameToDnAttrs = [:]
            if (atrUsernameIsDn.force != null) usernameToDnAttrs['force'] = atrUsernameIsDn.force
            'username-to-dn'(usernameToDnAttrs) {
                cache = atrUsernameIsDn.cache
                if (cache) {
                    'cache'(resolveCacheAttrs(cache))
                }
                'username-is-dn' {}
            }
        }
        if (atrGroupToPrincipal != null) {
            groupSearchAttrs = [:]
            if (atrGroupToPrincipal.groupDnAttribute != null) groupSearchAttrs['group-dn-attribute'] = atrGroupToPrincipal.groupDnAttribute
            if (atrGroupToPrincipal.groupName != null) groupSearchAttrs['group-name'] = atrGroupToPrincipal.groupName
            if (atrGroupToPrincipal.groupNameAttribute != null) groupSearchAttrs['group-name-attribute'] = atrGroupToPrincipal.groupNameAttribute
            if (atrGroupToPrincipal.iterative != null) groupSearchAttrs['iterative'] = atrGroupToPrincipal.iterative
            'group-search'(groupSearchAttrs) {
                cache = atrGroupToPrincipal.cache
                if (cache) {
                    'cache'(resolveCacheAttrs(cache))
                }
                groupToPrincipalAttrs = ['base-dn': atrGroupToPrincipal.baseDn]
                if (atrGroupToPrincipal.preferOriginalConnection != null) groupToPrincipalAttrs['prefer-original-connection'] = atrGroupToPrincipal.preferOriginalConnection
                if (atrGroupToPrincipal.recursive != null) groupToPrincipalAttrs['recursive'] = atrGroupToPrincipal.recursive
                if (atrGroupToPrincipal.searchBy != null) groupToPrincipalAttrs['search-by'] = atrGroupToPrincipal.searchBy
                'group-to-principal'(groupToPrincipalAttrs) {
                    membershipFilterAttrs = [:]
                    if (atrGroupToPrincipal.principalAttribute != null) membershipFilterAttrs['principal-attribute'] = atrGroupToPrincipal.principalAttribute
                    'membership-filter'(membershipFilterAttrs)
                }
            }
        }
        if (atrPrincipalToGroup != null) {
            groupSearchAttrs = [:]
            if (atrPrincipalToGroup.groupDnAttribute != null) groupSearchAttrs['group-dn-attribute'] = atrPrincipalToGroup.groupDnAttribute
            if (atrPrincipalToGroup.groupName != null) groupSearchAttrs['group-name'] = atrPrincipalToGroup.groupName
            if (atrPrincipalToGroup.groupNameAttribute != null) groupSearchAttrs['group-name-attribute'] = atrPrincipalToGroup.groupNameAttribute
            if (atrPrincipalToGroup.iterative != null) groupSearchAttrs['iterative'] = atrPrincipalToGroup.iterative
            'group-search'(groupSearchAttrs) {
                cache = atrPrincipalToGroup.cache
                if (cache) {
                    'cache'(resolveCacheAttrs(cache))
                }
                principalToGroupAttrs = [:]
                if (atrPrincipalToGroup.groupAttribute != null) principalToGroupAttrs['group-attribute'] = atrPrincipalToGroup.groupAttribute
                if (atrPrincipalToGroup.preferOriginalConnection != null) principalToGroupAttrs['prefer-original-connection'] = atrPrincipalToGroup.preferOriginalConnection
                if (atrPrincipalToGroup.skipMissingGroups != null) principalToGroupAttrs['skip-missing-groups'] = atrPrincipalToGroup.skipMissingGroups
                'principal-to-group'(principalToGroupAttrs)
            }
        }
    }
}

def usedSecurityRealm = securityRealm.find { it.'@name' == atrSecurityRealmName }
def authorizationElement = usedSecurityRealm.authorization
def isExistingAuthorization = authorizationElement.any { it.name() == 'authorization' }

if (isExistingAuthorization) {
    def isPropertiesAuthnExist = authorizationElement.properties.any { it.name() == 'properties' }
    if (isPropertiesAuthnExist) {
        throw new IllegalStateException("Security realm with name $atrSecurityRealmName already contains authorization configuration. Only one is allowed.")
    }
    def isLdapExists = authorizationElement.ldap.any { it.name() == 'ldap' }
    if (isLdapExists && !atrReplaceExisting) {
        throw new IllegalStateException("Ldap authorization already exists in security realm with name $atrSecurityRealmName.")
    } else {
        if (isLdapExists) {
            authorizationElement.ldap.find().replaceNode ldapDefinition
        } else {
            authorizationElement.appendNode ldapDefinition
        }
    }
} else {
    usedSecurityRealm.appendNode {
        'authorization'(ldapDefinition)
    }
}

def resolveCacheAttrs(org.wildfly.extras.creaper.commands.security.realms.LdapCache cache) {
    cacheAttrs = [:]
    if (cache.byAccessTime) cacheAttrs['type'] = 'by-access-time'
    if (cache.bySearchTime) cacheAttrs['type'] = 'by-search-time'
    if (cache.cacheFailures != null) cacheAttrs['cache-failures'] = cache.cacheFailures
    if (cache.evictionTime != null) cacheAttrs['eviction-time'] = cache.evictionTime
    if (cache.maxCacheSize != null) cacheAttrs['max-cache-size'] = cache.maxCacheSize
    return cacheAttrs
}
