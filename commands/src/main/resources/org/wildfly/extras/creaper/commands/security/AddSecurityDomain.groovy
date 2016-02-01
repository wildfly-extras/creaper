domainAttrs = ['name': atrSecurityDomainName]
if (atrCacheType != null) domainAttrs['cache-type'] = atrCacheType

def domainDefinition = {
    'security-domain'(domainAttrs)
}

def existingSecurityDomain = securitySubsystem.'security-domains'.'security-domain'.find { it.'@name' == atrSecurityDomainName }
if (existingSecurityDomain && !atrReplaceExisting) {
    throw new IllegalStateException("Security domain with name $atrSecurityDomainName already exists in configuration. Use different name.")
} else {
    if (existingSecurityDomain) {
        existingSecurityDomain.replaceNode domainDefinition
    } else {
        securitySubsystem.'security-domains'.appendNode domainDefinition
    }
}
