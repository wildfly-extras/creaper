def securityRealm = management.'security-realms'.'security-realm'
def isSecurityRealmExist = securityRealm.any { it.'@name' == realmName }
if (!isSecurityRealmExist) {
    throw new IllegalStateException("Security realm with name $realmName does not exist.")
}

def engineAttrs = [:]
if (enabledCipherSuites != null) engineAttrs['enabled-cipher-suites'] = ((List)enabledCipherSuites).join(" ")
if (enabledProtocols != null) engineAttrs['enabled-protocols'] = ((List)enabledProtocols).join(" ")

def keystoreAttrs = [:]
if (alias != null) keystoreAttrs['alias'] = alias
if (keyPassword != null) keystoreAttrs['key-password'] = keyPassword
if (keystorePassword != null) keystoreAttrs['keystore-password'] = keystorePassword
if (keystorePath != null) keystoreAttrs['path'] = keystorePath
if (keystoreProvider != null) keystoreAttrs['provider'] = keystoreProvider
if (keystoreRelativeTo != null) keystoreAttrs['relative-to'] = keystoreRelativeTo
if (protocol != null) keystoreAttrs['protocol'] = protocol

def sslDefinition = {
    'ssl' {
        if (!engineAttrs.isEmpty()) {
            'engine'(engineAttrs)
        }
        if (!keystoreAttrs.isEmpty()) {
            'keystore'(keystoreAttrs)
        }
    }
}

def usedSecurityRealm = securityRealm.find { it.'@name' == realmName }

def serverIdentities = usedSecurityRealm.'server-identities'
def isExistingServerIdentities = serverIdentities.any { it.name() == 'server-identities' }
if (isExistingServerIdentities) {
    def isExistingSecret = serverIdentities.ssl.any { it.name() == 'ssl' }
    if (isExistingSecret && !replaceExisting) {
        throw new IllegalStateException("SSL server identity already exists in security realm with name $realmName.")
    } else {
        if (isExistingSecret) {
            serverIdentities.ssl.find().replaceNode sslDefinition
        } else {
            serverIdentities.appendNode sslDefinition
        }
    }
} else {
    usedSecurityRealm.appendNode {
        'server-identities'(sslDefinition)
    }
}
