def secDomain = securitySubsystem.'security-domains'.'security-domain'.find { it.@name == atrSecurityDomainName }
if (!secDomain) {
    throw new IllegalStateException("Security domain ${atrSecurityDomainName} not found")
}

def mappingModuleToRemove = secDomain.'mapping'.'mapping-module'.find { it.@name == atrMappingModuleName }
mappingModuleToRemove = mappingModuleToRemove ?: secDomain.'mapping'.'mapping-module'.find { it.@code == atrMappingModuleName && it.attributes().get('name') == null }

if (mappingModuleToRemove) {
    if ( secDomain.'mapping'.'mapping-module'.size() > 1 ) {
        mappingModuleToRemove.replaceNode {}
    } else {
        secDomain.'mapping'.find().replaceNode {}
    }
} else {
    throw new IllegalStateException("Mapping module ${atrMappingModuleName} not found => can't remove it")
}
