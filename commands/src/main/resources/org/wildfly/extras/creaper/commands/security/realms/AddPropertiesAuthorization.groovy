def securityRealm = management.'security-realms'.'security-realm'
def isSecurityRealmExist = securityRealm.any { it.'@name' == atrSecurityRealmName }
if (!isSecurityRealmExist) {
    throw new IllegalStateException("Security realm with name $atrSecurityRealmName does not exist.")
}

propertiesAttrs = ['path': atrPath]
if (atrRelativeTo != null) propertiesAttrs['relative-to'] = atrRelativeTo

def propertiesDefinition = {
    'properties'(propertiesAttrs)
}

def usedSecurityRealm = securityRealm.find { it.'@name' == atrSecurityRealmName }
def authorizationElement = usedSecurityRealm.authorization
def isExistingAuthentication = authorizationElement.any { it.name() == 'authorization' }

if (isExistingAuthentication) {
    def isLdapAuthzExist = authorizationElement.ldap.any { it.name() == 'ldap' }
    if (isLdapAuthzExist) {
        throw new IllegalStateException("Security realm with name $atrSecurityRealmName already contains authorization configurations. Only one is allowed.")
    }
    def isPropertiesExists = authorizationElement.properties.any { it.name() == 'properties' }
    if (isPropertiesExists && !atrReplaceExisting) {
        throw new IllegalStateException("Properties authorization already exists in security realm with name $atrSecurityRealmName.")
    } else {
        if (isPropertiesExists) {
            authorizationElement.properties.find().replaceNode propertiesDefinition
        } else {
            authorizationElement.appendNode propertiesDefinition
        }
    }
} else {
    usedSecurityRealm.appendNode {
        'authorization'(propertiesDefinition)
    }
}
