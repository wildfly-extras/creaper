def securityDomain = securitySubsystem.'security-domains'.'security-domain'
def isSecurityDomainExist = securityDomain.any { it.'@name' == atrSecurityDomainName }
if (!isSecurityDomainExist) {
    throw new IllegalStateException("Security domain with name $atrSecurityDomainName does not exist.")
}

authorizationModuleAttrs = ['name': atrName, 'code': atrCode, 'flag': atrFlag]
if (atrModule != null) authorizationModuleAttrs['module'] = atrModule

def authorizationModuleDefinition = {
    'policy-module'(authorizationModuleAttrs) {
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
def authorizationElement = usedSecurityDomain.authorization
def isAuthorizationElementExist = authorizationElement.any { it.name() == 'authorization' }

if (isAuthorizationElementExist) {
    def existingAuthorizationModule = provideAuthorizationModule()
    if (existingAuthorizationModule && !atrReplaceExisting) {
        throw new IllegalStateException("Authorization module with name $atrSecurityDomainName already exists in configuration. Use different name.")
    } else {
        if (existingAuthorizationModule) {
            existingAuthorizationModule.replaceNode authorizationModuleDefinition
        } else {
            authorizationElement.appendNode authorizationModuleDefinition
        }
    }
} else {
    usedSecurityDomain.appendNode {
        'authorization'(authorizationModuleDefinition)
    }
}

def provideAuthorizationModule() {
    def policyModule = securitySubsystem.'security-domains'.'security-domain'.'authorization'.'policy-module'.find { it.'@name' == atrName }
    if (policyModule) return policyModule
    if (atrName != atrCode) return null
    return securitySubsystem.'security-domains'.'security-domain'.'authorization'.'policy-module'.find { it.@code == atrCode && it.attributes().get('name') == null }
}

