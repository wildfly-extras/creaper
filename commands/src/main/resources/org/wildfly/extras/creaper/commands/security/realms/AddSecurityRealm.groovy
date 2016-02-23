realmAttrs = ['name': atrSecurityRealmName]

def realmDefinition = {
    'security-realm'(realmAttrs) {
        if (atrMapGroupsToRoles != null) {
            'authorization'(['map-groups-to-roles': atrMapGroupsToRoles])
        }
    }
}

def existingSecurityRealm = management.'security-realms'.'security-realm'.find { it.'@name' == atrSecurityRealmName }
if (existingSecurityRealm && !atrReplaceExisting) {
    throw new IllegalStateException("Security realm with name $atrSecurityRealmName already exists in configuration. Use different name.")
} else {
    if (existingSecurityRealm) {
        existingSecurityRealm.replaceNode realmDefinition
    } else {
        management.'security-realms'.appendNode realmDefinition
    }
}
