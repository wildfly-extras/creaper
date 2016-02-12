def securityRealm = management.'security-realms'.'security-realm'
def isSecurityRealmExist = securityRealm.any { it.'@name' == atrSecurityRealmName }
if (!isSecurityRealmExist) {
    throw new IllegalStateException("Security realm with name $atrSecurityRealmName does not exist.")
}

jaasAttrs = ['name': atrName]
if (atrAssignGroups != null) jaasAttrs['assign-groups'] = atrAssignGroups

def jaasDefinition = {
    'jaas'(jaasAttrs)
}

def usedSecurityRealm = securityRealm.find { it.'@name' == atrSecurityRealmName }
def authenticationElement = usedSecurityRealm.authentication
def isExistingAuthentication = authenticationElement.any { it.name() == 'authentication' }

if (isExistingAuthentication) {
    def isPropertiesAuthnExist = authenticationElement.properties.any { it.name() == 'properties' }
    if (isPropertiesAuthnExist) {
        throw new IllegalStateException("Security realm with name $atrSecurityRealmName already contains username/password based authentication.")
    }
    def isLdapAuthnExist = authenticationElement.ldap.any { it.name() == 'ldap' }
    if (isLdapAuthnExist) {
        throw new IllegalStateException("Security realm with name $atrSecurityRealmName already contains username/password based authentication.")
    }
    def isJaasExists = authenticationElement.jaas.any { it.name() == 'jaas' }
    if (isJaasExists && !atrReplaceExisting) {
        throw new IllegalStateException("Jaas authnetication already exists in security realm with name $atrSecurityRealmName.")
    } else {
        if (isJaasExists) {
            authenticationElement.jaas.find().replaceNode jaasDefinition
        } else {
            authenticationElement.appendNode jaasDefinition
        }
    }
} else {
    usedSecurityRealm.appendNode {
        'authentication'(jaasDefinition)
    }
}
