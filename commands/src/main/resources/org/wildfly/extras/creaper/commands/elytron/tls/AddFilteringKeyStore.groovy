keyStoreAttrs = ['name': atrName]
if (atrAliasFilter != null) keyStoreAttrs['alias-filter'] = atrAliasFilter
if (atrKeyStore != null) keyStoreAttrs['key-store'] = atrKeyStore

def keyStoreDefinition = {
    'filtering-key-store'(keyStoreAttrs)
}

def isExistingTls = elytronSubsystem.'tls'.any { it.name() == 'tls' }
if (! isExistingTls) {
    elytronSubsystem.appendNode { 'tls' { 'key-stores' keyStoreDefinition } }
    return
}

def isExistingKeyStores = elytronSubsystem.'tls'.'key-stores'.any { it.name() == 'key-stores' }
if (! isExistingKeyStores) {
    elytronSubsystem.'tls'.appendNode { 'key-stores' keyStoreDefinition }
    return
}

def existingKeyStore = elytronSubsystem.'tls'.'key-stores'.'filtering-key-store'.find { it.'@name' == atrName }
if (existingKeyStore && !atrReplaceExisting) {
    throw new IllegalStateException("KeyStore with name $atrName already exists in configuration. Use different name.")
} else {
    if (existingKeyStore) {
        existingKeyStore.replaceNode keyStoreDefinition
    } else {
        elytronSubsystem.'tls'.'key-stores'.appendNode keyStoreDefinition
    }
}
