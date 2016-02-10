connectionAttrs = ['name': atrConnectionName]
if (atrInitialContextFactory != null) connectionAttrs['initial-context-factory'] = atrInitialContextFactory
if (atrReferrals != null) connectionAttrs['referrals'] = atrReferrals
if (atrSearchCredential != null) connectionAttrs['search-credential'] = atrSearchCredential
if (atrSearchDn != null) connectionAttrs['search-dn'] = atrSearchDn
if (atrSecurityRealm != null) connectionAttrs['security-realm'] = atrSecurityRealm
if (atrUrl != null) connectionAttrs['url'] = atrUrl
if (atrHandlesReferralsFor != null && !atrHandlesReferralsFor.isEmpty()) connectionAttrs['handles-referrals-for'] = resolveHandlesReferralsFor(atrHandlesReferralsFor)

def connectionDefinition = {
    'ldap' (connectionAttrs) {
        if (atrProperties != null && !atrProperties.isEmpty()) {
            'properties' {
                for (Map.Entry<String, String> entry : atrProperties.entrySet()) {
                    'property'('name': entry.key, 'value': entry.value)
                }
            }
        }
    }
}

def connections = management.'outbound-connections'
def isExistingConnetions = connections.any()
if (isExistingConnetions) {
    def connectionElement = connections.ldap
    def existingConnection = connectionElement.find { it.'@name' == atrConnectionName }

    if (existingConnection && !atrReplaceExisting) {
        throw new IllegalStateException("Ldap outbound connection with name $atrConnectionName already exists in configuration. Use different name.")
    } else {
        if (existingConnection) {
            existingConnection.replaceNode connectionDefinition
        } else {
            connections.appendNode connectionDefinition
        }
    }
} else {
    management.appendNode {
        'outbound-connections'(connectionDefinition)
    }
}

def resolveHandlesReferralsFor(List<String> list) {
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
