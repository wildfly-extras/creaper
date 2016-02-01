def secDomain = securitySubsystem.'security-domains'.'security-domain'.find { it.@name == atrSecurityDomainName }
if (!secDomain) {
    throw new IllegalStateException("Security domain ${atrSecurityDomainName} not found")
}

def authorizationModuleToRemove = secDomain.'authorization'.'policy-module'.find { it.@name == atrAuthorizationModuleName }
authorizationModuleToRemove = authorizationModuleToRemove ?: secDomain.'authorization'.'policy-module'.find { it.@code == atrAuthorizationModuleName && it.attributes().get('name') == null }

if (authorizationModuleToRemove) {
    if ( secDomain.'authorization'.'policy-module'.size() > 1 ) {
        authorizationModuleToRemove.replaceNode {}
    } else {
        secDomain.'authorization'.find().replaceNode {}
    }
} else {
    throw new IllegalStateException("Authorization module ${atrAuthorizationModuleName} not found => can't remove it")
}
