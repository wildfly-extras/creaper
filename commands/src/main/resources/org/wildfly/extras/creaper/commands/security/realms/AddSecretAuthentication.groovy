def securityRealm = management.'security-realms'.'security-realm'
def isSecurityRealmExist = securityRealm.any { it.'@name' == realmName }
if (!isSecurityRealmExist) {
    throw new IllegalStateException("Security realm with name $realmName does not exist.")
}

def serverIdentityDefinition = {
    secret(value: password)
}

def usedSecurityRealm = securityRealm.find { it.'@name' == realmName }

def serverIdentities = usedSecurityRealm.'server-identities'
def isExistingServerIdentities = serverIdentities.any { it.name() == 'server-identities' }
if (isExistingServerIdentities) {
    def isExistingSecret = serverIdentities.secret.any { it.name() == 'secret' }
    if (isExistingSecret && !replaceExisting) {
        throw new IllegalStateException("Secret server identity already exists in security realm with name $realmName.")
    } else {
        if (isExistingSecret) {
            serverIdentities.secret.find().replaceNode serverIdentityDefinition
        } else {
            serverIdentities.appendNode serverIdentityDefinition
        }
    }
} else {
    usedSecurityRealm.appendNode {
        'server-identities'(serverIdentityDefinition)
    }
}
