keyManagerAttrs = ['name': atrName, 'key-store': atrKeyStore]
if (atrAlgorithm != null) keyManagerAttrs['algorithm'] = atrAlgorithm
if (atrAliasFilter != null) keyManagerAttrs['alias-filter'] = atrAliasFilter
if (atrProviderName != null) keyManagerAttrs['provider-name'] = atrProviderName
if (atrProviders != null) keyManagerAttrs['providers'] = atrProviders

credentialReferenceAttrs = [:]
if (atrCredentialRefAlias != null) credentialReferenceAttrs['alias'] = atrCredentialRefAlias
if (atrCredentialRefType != null) credentialReferenceAttrs['type'] = atrCredentialRefType
if (atrCredentialRefStore != null) credentialReferenceAttrs['store'] = atrCredentialRefStore
if (atrCredentialRefClearText != null) credentialReferenceAttrs['clear-text'] = atrCredentialRefClearText

def keyManagerDefinition = {
    'key-manager'(keyManagerAttrs) {
        'credential-reference'(credentialReferenceAttrs)
    }
}

def isExistingTls = elytronSubsystem.'tls'.any { it.name() == 'tls' }
if (! isExistingTls) {
    elytronSubsystem.appendNode { 'tls' { 'key-managers' keyManagerDefinition } }
    return
}

def isExistingKeyManagers = elytronSubsystem.'tls'.'key-managers'.any { it.name() == 'key-managers' }
if (! isExistingKeyManagers) {
    elytronSubsystem.'tls'.appendNode { 'key-managers' keyManagerDefinition }
    return
}

def existingKeyManager = elytronSubsystem.'tls'.'key-managers'.'key-manager'.find { it.'@name' == atrName }
if (existingKeyManager && !atrReplaceExisting) {
    throw new IllegalStateException("KeyManager with name $atrName already exists in configuration. Use different name.")
} else {
    if (existingKeyManager) {
        existingKeyManager.replaceNode keyManagerDefinition
    } else {
        elytronSubsystem.'tls'.'key-managers'.appendNode keyManagerDefinition
    }
}
