def securityRealm = management.'security-realms'.'security-realm'
def isSecurityRealmExist = securityRealm.any { it.'@name' == atrSecurityRealmName }
if (!isSecurityRealmExist) {
    throw new IllegalStateException("Security realm with name $atrSecurityRealmName does not exist.")
}

localAttrs = [:]
if (atrAllowedUsers != null) localAttrs['allowed-users'] = atrAllowedUsers
if (atrDefaultUser != null) localAttrs['default-user'] = atrDefaultUser
if (atrSkipGroupLoading != null) localAttrs['skip-group-loading'] = atrSkipGroupLoading

def localDefinition = {
    'local'(localAttrs)
}

def usedSecurityRealm = securityRealm.find { it.'@name' == atrSecurityRealmName }
def authenticationElement = usedSecurityRealm.authentication
def isExistingAuthentication = authenticationElement.any { it.name() == 'authentication' }

if (isExistingAuthentication) {
    def isLocalExists = authenticationElement.local.any { it.name() == 'local' }
    if (isLocalExists && !atrReplaceExisting) {
        throw new IllegalStateException("Local authnetication already exists in security realm with name $atrSecurityRealmName.")
    } else {
        if (isLocalExists) {
            authenticationElement.local.find().replaceNode localDefinition
        } else {
            authenticationElement.appendNode localDefinition
        }
    }
} else {
    usedSecurityRealm.appendNode {
        'authentication'(localDefinition)
    }
}
