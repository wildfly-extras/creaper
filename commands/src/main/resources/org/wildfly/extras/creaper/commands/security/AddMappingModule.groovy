def securityDomain = securitySubsystem.'security-domains'.'security-domain'
def isSecurityDomainExist = securityDomain.any { it.'@name' == atrSecurityDomainName }
if (!isSecurityDomainExist) {
    throw new IllegalStateException("Security domain with name $atrSecurityDomainName does not exist.")
}

mappingModuleAttrs = ['name': atrName, 'code': atrCode, 'type': atrType]
if (atrModule != null) mappingModuleAttrs['module'] = atrModule

def mappingModuleDefinition = {
    'mapping-module'(mappingModuleAttrs) {
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
def mappingElement = usedSecurityDomain.mapping
def isMappingElementExist = mappingElement.any { it.name() == 'mapping' }

if (isMappingElementExist) {
    def existingMappingModule = provideMappingModule()
    if (existingMappingModule && !atrReplaceExisting) {
        throw new IllegalStateException("Mapping module with name $atrSecurityDomainName already exists in configuration. Use different name.")
    } else {
        if (existingMappingModule) {
            existingMappingModule.replaceNode mappingModuleDefinition
        } else {
            mappingElement.appendNode mappingModuleDefinition
        }
    }
} else {
    usedSecurityDomain.appendNode {
        'mapping'(mappingModuleDefinition)
    }
}

def provideMappingModule() {
    def mappingModule = securitySubsystem.'security-domains'.'security-domain'.'mapping'.'mapping-module'.find { it.'@name' == atrName }
    if (mappingModule) return mappingModule
    if (atrName != atrCode) return null
    return securitySubsystem.'security-domains'.'security-domain'.'mapping'.'mapping-module'.find { it.@code == atrCode && it.attributes().get('name') == null }
}

