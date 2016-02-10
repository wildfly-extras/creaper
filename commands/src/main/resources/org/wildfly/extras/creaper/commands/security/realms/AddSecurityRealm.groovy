realmAttrs = ['name': atrSecurityRealmName]
if (atrMapGroupsToRoles != null) realmAttrs['map-groups-to-roles'] = atrMapGroupsToRoles

def realmDefinition = {
    'security-realm'(realmAttrs)
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
