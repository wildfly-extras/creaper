def securityRealm = management.'security-realms'.'security-realm'
def isSecurityRealmExist = securityRealm.any { it.'@name' == atrSecurityRealmName }
if (!isSecurityRealmExist) {
    throw new IllegalStateException("Security realm with name $atrSecurityRealmName does not exist.")
}

kerberosAttrs = [:]
if (atrRemoveRealm != null) kerberosAttrs['remove-realm'] = atrRemoveRealm

def kerberosDefinition = {
    'kerberos'(kerberosAttrs)
}

def serverIdentityDefinition = {
    'kerberos' {
        atrKeytabs.each {
            keytabAttrs = ['principal': it.principal]
            if (it.path != null) keytabAttrs['path'] = it.path
            if (it.debug != null) keytabAttrs['debug'] = it.debug
            if (it.relativeTo != null) keytabAttrs['relative-to'] = it.relativeTo
            if (it.forHosts != null) keytabAttrs['for-hosts'] = resolveForHosts(it.forHosts)
            'keytab'(keytabAttrs)
        }
    }
}

def usedSecurityRealm = securityRealm.find { it.'@name' == atrSecurityRealmName }

def serverIdentities = usedSecurityRealm.'server-identities'
def isExistingServerIdentities = serverIdentities.any { it.name() == 'server-identities' }
if (isExistingServerIdentities) {
    def isExistingKerberos = serverIdentities.kerberos.any { it.name() == 'kerberos' }
    if (isExistingKerberos && !atrReplaceExisting) {
        throw new IllegalStateException("Kerberos server identity already exists in security realm with name $atrSecurityRealmName.")
    } else {
        if (isExistingKerberos) {
            serverIdentities.kerberos.find().replaceNode serverIdentityDefinition
        } else {
            serverIdentities.appendNode serverIdentityDefinition
        }
    }
} else {
    usedSecurityRealm.appendNode {
        'server-identities'(serverIdentityDefinition)
    }
}

def authenticationElement = usedSecurityRealm.authentication
def isExistingAuthentication = authenticationElement.any { it.name() == 'authentication' }
if (isExistingAuthentication) {
    def isKerberosExists = authenticationElement.kerberos.any { it.name() == 'kerberos' }
    if (isKerberosExists && !atrReplaceExisting) {
        throw new IllegalStateException("Kerberos authentication already exists in security realm with name $atrSecurityRealmName.")
    } else {
        if (isKerberosExists) {
            authenticationElement.kerberos.find().replaceNode kerberosDefinition
        } else {
            authenticationElement.appendNode kerberosDefinition
        }
    }
} else {
    usedSecurityRealm.appendNode {
        'authentication'(kerberosDefinition)
    }
}

def resolveForHosts(List<String> list) {
    StringBuilder sb = new StringBuilder("")
    for (String host : list) {
        sb.append(host)
        sb.append(" ")
    }
    sb.deleteCharAt(sb.length() - 1)
    return sb.toString()
}
