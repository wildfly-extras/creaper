cachingRealmAttrs = ['name': atrName]
if (atrRealm != null) cachingRealmAttrs['realm'] = atrRealm
if (atrMaximumEntries != null) cachingRealmAttrs['maximum-entries'] = atrMaximumEntries
if (atrMaximumAge != null) cachingRealmAttrs['maximum-age'] = atrMaximumAge

def cachingRealmDefinition = {
    'caching-realm'(cachingRealmAttrs)
}

def isExistingSecurityRealms = elytronSubsystem.'security-realms'.any { it.name() == 'security-realms' }
if (! isExistingSecurityRealms) {
    elytronSubsystem.appendNode { 'security-realms' cachingRealmDefinition }
    return
}

def existingCachingRealm = elytronSubsystem.'security-realms'.'caching-realm'.find { it.'@name' == atrName }
if (existingCachingRealm && !atrReplaceExisting) {
    throw new IllegalStateException("caching-realm with name $atrName already exists in configuration. Use different name.")
} else {
    if (existingCachingRealm) {
        existingCachingRealm.replaceNode cachingRealmDefinition
    } else {
        elytronSubsystem.'security-realms'.appendNode cachingRealmDefinition
    }
}
