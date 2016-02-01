def securityDomain = securitySubsystem.'security-domains'.'security-domain'
def isSecurityDomainExist = securityDomain.any { it.'@name' == atrSecurityDomainName }
if (!isSecurityDomainExist) {
    throw new IllegalStateException("Security domain with name $atrSecurityDomainName does not exist.")
}

loginModuleAttrs = ['name': atrName, 'code': atrCode, 'flag': atrFlag]
if (atrModule != null) loginModuleAttrs['module'] = atrModule

def loginModuleDefinition = {
    'login-module'(loginModuleAttrs) {
        if (atrModuleOptions != null) {
            for (Map.Entry<String, String> entry : atrModuleOptions.entrySet()) {
                if (entry.key != null && entry.value != null) {
                    'module-option'('name': entry.key, 'value': entry.value)
                }
            }
        }
    }
}

def usedSecurityDomain = securityDomain.find { it.'@name' == atrSecurityDomainName }
def authenticationElement = usedSecurityDomain.authentication
def isAuthenticationElementExist = authenticationElement.any { it.name() == 'authentication' }

if (isAuthenticationElementExist) {
    def existingLoginModule = provideLoginModule()
    if (existingLoginModule && !atrReplaceExisting) {
        throw new IllegalStateException("Login module with name $atrSecurityDomainName already exists in configuration. Use different name.")
    } else {
        if (existingLoginModule) {
            existingLoginModule.replaceNode loginModuleDefinition
        } else {
            authenticationElement.appendNode loginModuleDefinition
        }
    }
} else {
    usedSecurityDomain.appendNode {
        'authentication'(loginModuleDefinition)
    }
}

def provideLoginModule() {
    def loginModule = securitySubsystem.'security-domains'.'security-domain'.'authentication'.'login-module'.find { it.'@name' == atrName }
    if (loginModule) return loginModule
    if (atrName != atrCode) return null
    return securitySubsystem.'security-domains'.'security-domain'.'authentication'.'login-module'.find { it.@code == atrCode && it.attributes().get('name') == null }
}

