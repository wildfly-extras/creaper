customAttrs = ['name': atrName]
if (atrClassName != null) customAttrs['class-name'] = atrClassName
if (atrModule != null) customAttrs['module'] = atrModule

def customDefinition = {
    'custom-credential-security-factory'(customAttrs) {
      if (!atrConfiguration.isEmpty()) {
       'configuration' {
             for ( item in atrConfiguration ) {
                'property'(['name': item.key, 'value': item.value])
                }
            }
      }
    }
}

def isExistingSecurityRealms = elytronSubsystem.'credential-security-factories'.any { it.name() == 'credential-security-factories' }
if (! isExistingSecurityRealms) {
    elytronSubsystem.appendNode { 'credential-security-factories' customDefinition }
    return
}

def existingCustomElement = elytronSubsystem.'credential-security-factories'.'custom-credential-security-factory'.find { it.'@name' == atrName }
if (existingCustomElement && !atrReplaceExisting) {
    throw new IllegalStateException("CustomCredentialSecurityFactory with name $atrName already exists in configuration. Use different name.")
} else {
    if (existingCustomElement) {
        existingCustomElement.replaceNode customDefinition
    } else {
        elytronSubsystem.'credential-security-factories'.appendNode customDefinition
    }
}
