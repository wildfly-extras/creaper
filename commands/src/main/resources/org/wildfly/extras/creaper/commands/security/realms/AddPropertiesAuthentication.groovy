def securityRealm = management.'security-realms'.'security-realm'
def isSecurityRealmExist = securityRealm.any { it.'@name' == atrSecurityRealmName }
if (!isSecurityRealmExist) {
    throw new IllegalStateException("Security realm with name $atrSecurityRealmName does not exist.")
}

propertiesAttrs = ['path': atrPath]
if (atrRelativeTo != null) propertiesAttrs['relative-to'] = atrRelativeTo
if (atrPlainText != null) propertiesAttrs['plain-text'] = atrPlainText

def propertiesDefinition = {
    'properties'(propertiesAttrs)
}

def usedSecurityRealm = securityRealm.find { it.'@name' == atrSecurityRealmName }
def authenticationElement = usedSecurityRealm.authentication
def isExistingAuthentication = authenticationElement.any { it.name() == 'authentication' }

if (isExistingAuthentication) {
    def isJaasAuthnExist = authenticationElement.jaas.any { it.name() == 'jaas' }
    if (isJaasAuthnExist) {
        throw new IllegalStateException("Security realm with name $atrSecurityRealmName already contains username/password based authentication.")
    }
    def isLdapAuthnExist = authenticationElement.ldap.any { it.name() == 'ldap' }
    if (isLdapAuthnExist) {
        throw new IllegalStateException("Security realm with name $atrSecurityRealmName already contains username/password based authentication.")
    }
    def isPropertiesExists = authenticationElement.properties.any { it.name() == 'properties' }
    if (isPropertiesExists && !atrReplaceExisting) {
        throw new IllegalStateException("Properties authnetication already exists in security realm with name $atrSecurityRealmName.")
    } else {
        if (isPropertiesExists) {
            authenticationElement.properties.find().replaceNode propertiesDefinition
        } else {
            authenticationElement.appendNode propertiesDefinition
        }
    }
} else {
    usedSecurityRealm.appendNode {
        'authentication'(propertiesDefinition)
    }
}
