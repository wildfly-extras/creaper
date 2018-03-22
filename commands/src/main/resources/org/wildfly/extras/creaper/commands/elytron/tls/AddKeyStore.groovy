keyStoreAttrs = ['name': atrName]
if (atrAliasFilter != null) keyStoreAttrs['alias-filter'] = atrAliasFilter

implementationAttrs = [:]
if (atrType != null) implementationAttrs['type'] = atrType
if (atrProviderName != null) implementationAttrs['provider-name'] = atrProviderName
if (atrProviders != null) implementationAttrs['providers'] = atrProviders

fileAttrs = [:]
if (atrPath != null) fileAttrs['path'] = atrPath
if (atrRelativeTo != null) fileAttrs['relative-to'] = atrRelativeTo
if (atrRequired != null) fileAttrs['required'] = atrRequired

credentialReferenceAttrs = [:]
if (atrCredentialRefAlias != null) credentialReferenceAttrs['alias'] = atrCredentialRefAlias
if (atrCredentialRefType != null) credentialReferenceAttrs['type'] = atrCredentialRefType
if (atrCredentialRefStore != null) credentialReferenceAttrs['store'] = atrCredentialRefStore
if (atrCredentialRefClearText != null) credentialReferenceAttrs['clear-text'] = atrCredentialRefClearText

def keyStoreDefinition = {
    'key-store'(keyStoreAttrs) {
        'credential-reference'(credentialReferenceAttrs)
        'implementation'(implementationAttrs)
        if (!fileAttrs.isEmpty()) {
            'file'(fileAttrs)
        }
    }
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

def existingKeyStore = elytronSubsystem.'tls'.'key-stores'.'key-store'.find { it.'@name' == atrName }
if (existingKeyStore && !atrReplaceExisting) {
    throw new IllegalStateException("KeyStore with name $atrName already exists in configuration. Use different name.")
} else {
    if (existingKeyStore) {
        existingKeyStore.replaceNode keyStoreDefinition
    } else {
        elytronSubsystem.'tls'.'key-stores'.appendNode keyStoreDefinition
    }
}
