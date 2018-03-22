appSecDomainAttrs = ['name': atrName, 'http-authentication-factory': atrHttpAuthenticationFactory]
if (atrOverrideDeploymentConfig != null) appSecDomainAttrs['override-deployment-config'] = atrOverrideDeploymentConfig

def appSecDomainDefinition = { 'application-security-domain'(appSecDomainAttrs) }

def isExistingAppSecDomains = undertowSubsystem.'application-security-domains'.any { it.name() == 'application-security-domains' }
if (! isExistingAppSecDomains) {
    undertowSubsystem.appendNode { 'application-security-domains' appSecDomainDefinition }
    return
}

def existingAppSecDomain = undertowSubsystem.'application-security-domains'.'application-security-domain'.find { it.'@name' == atrName }
if (existingAppSecDomain && !atrReplaceExisting) {
    throw new IllegalStateException("Application security domain with name $atrName already exists in configuration. Use different name.")
} else {
    if (existingAppSecDomain) {
        existingAppSecDomain.replaceNode appSecDomainDefinition
    } else {
        undertowSubsystem.'application-security-domains'.appendNode appSecDomainDefinition
    }
}
