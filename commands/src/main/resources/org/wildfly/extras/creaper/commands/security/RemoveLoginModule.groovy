def secDomain = securitySubsystem.'security-domains'.'security-domain'.find { it.@name == atrSecurityDomainName }
if (!secDomain) {
    throw new IllegalStateException("Security domain ${atrSecurityDomainName} not found")
}

def loginModuleToRemove = secDomain.'authentication'.'login-module'.find { it.@name == atrLoginModuleName }
loginModuleToRemove = loginModuleToRemove ?: secDomain.'authentication'.'login-module'.find { it.@code == atrLoginModuleName && it.attributes().get('name') == null }

if (loginModuleToRemove) {
    if ( secDomain.'authentication'.'login-module'.size() > 1 ) {
        loginModuleToRemove.replaceNode {}
    } else {
        secDomain.'authentication'.find().replaceNode {}
    }
} else {
    throw new IllegalStateException("Login module ${atrLoginModuleName} not found => can't remove it")
}

