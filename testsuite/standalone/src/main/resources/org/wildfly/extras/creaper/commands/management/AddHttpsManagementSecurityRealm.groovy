def realmAttrs = ['name': atrSecurityRealmName]

def keyStoreAttrs = [:]
if (atrKeystorePassword != null) keyStoreAttrs['keystore-password'] = atrKeystorePassword
if (atrKeystorePath != null) keyStoreAttrs['path'] = atrKeystorePath
if (atrKeystoreRelativeTo != null) keyStoreAttrs['relative-to'] = atrKeystoreRelativeTo
if (atrKeystoreProvider != null) keyStoreAttrs['provider'] = atrKeystoreProvider
if (atrKeyAlias != null) keyStoreAttrs['alias'] = atrKeyAlias
if (atrKeyPassword != null) keyStoreAttrs['key-password'] = atrKeyPassword

def trustStoreAttrs = [:]
if (atrTruststorePassword != null) trustStoreAttrs['keystore-password'] = atrTruststorePassword
if (atrTruststorePath != null) trustStoreAttrs['path'] = atrTruststorePath
if (atrTruststoreRelativeTo != null) trustStoreAttrs['relative-to'] = atrTruststoreRelativeTo
if (atrTruststoreProvider != null) trustStoreAttrs['provider'] = atrTruststoreProvider

def realmDefinition = {
    'security-realm'(realmAttrs) {
        if (atrKeystorePassword != null) {
            'server-identities' {
                'ssl' {
                    'keystore'(keyStoreAttrs)
                }
            }
        }
        if (atrTruststorePassword != null) {
            'authentication' {
                'truststore'(trustStoreAttrs)
            }
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
