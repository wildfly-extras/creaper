keyStoreAttrs = ['name': atrName, 'dir-context': atrDirContext]

newItemTemplateAttrs = [:]
if (atrNewItemPath != null) newItemTemplateAttrs['new-item-path'] = atrNewItemPath
if (atrNewItemRdn != null) newItemTemplateAttrs['new-item-rdn'] = atrNewItemRdn

searchAttrs = [:]
if (atrSearchPath != null) searchAttrs['path'] = atrSearchPath
if (atrSearchRecursive != null) searchAttrs['recursive'] = atrSearchRecursive
if (atrSearchTimeLimit != null) searchAttrs['time-limit'] = atrSearchTimeLimit
if (atrFilterAlias != null) searchAttrs['filter-alias'] = atrFilterAlias
if (atrFilterCertificate != null) searchAttrs['filter-certificate'] = atrFilterCertificate
if (atrFilterIterate != null) searchAttrs['filter-iterate'] = atrFilterIterate

attributeMappingAttrs = [:]
if (atrAliasAttribute != null) attributeMappingAttrs['alias-attribute'] = atrAliasAttribute
if (atrCertificateAttribute != null) attributeMappingAttrs['certificate-attribute'] = atrCertificateAttribute
if (atrCertificateType != null) attributeMappingAttrs['certificate-type'] = atrCertificateType
if (atrCertificateChainAttribute != null) attributeMappingAttrs['certificate-chain-attribute'] = atrCertificateChainAttribute
if (atrCertificateChainEncoding != null) attributeMappingAttrs['certificate-chain-encoding'] = atrCertificateChainEncoding
if (atrKeyAttribute != null) attributeMappingAttrs['key-attribute'] = atrKeyAttribute
if (atrKeyType != null) attributeMappingAttrs['key-type'] = atrKeyType

def keyStoreDefinition = {
    'ldap-key-store'(keyStoreAttrs) {
        'search'(searchAttrs)
        if (!attributeMappingAttrs.isEmpty()) {
            'attribute-mapping'(attributeMappingAttrs)
        }
        if (!newItemTemplateAttrs.isEmpty()) {
            'new-item-template'(newItemTemplateAttrs) {
                if (atrNewItemAttributes != null && !atrNewItemAttributes.isEmpty()) {
                    for (newItemAttribute in atrNewItemAttributes) {
                        'attribute'(['name': newItemAttribute.name, 'value': resolveNewItemAttribute(newItemAttribute.values)])
                    }
                }
            }
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

def existingKeyStore = elytronSubsystem.'tls'.'key-stores'.'ldap-key-store'.find { it.'@name' == atrName }
if (existingKeyStore && !atrReplaceExisting) {
    throw new IllegalStateException("LdapKeyStore with name $atrName already exists in configuration. Use different name.")
} else {
    if (existingKeyStore) {
        existingKeyStore.replaceNode keyStoreDefinition
    } else {
        elytronSubsystem.'tls'.'key-stores'.appendNode keyStoreDefinition
    }
}

def resolveNewItemAttribute(List<String> list) {
    if (list == null || list.isEmpty()) {
        return null
    }
    StringBuilder sb = new StringBuilder("")
    for (String host : list) {
        sb.append(host)
        sb.append(" ")
    }
    sb.deleteCharAt(sb.length() - 1)
    return sb.toString()
}
