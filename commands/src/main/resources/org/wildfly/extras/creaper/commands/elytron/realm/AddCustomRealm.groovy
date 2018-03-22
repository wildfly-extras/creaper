customAttrs = ['name': atrName]
if (atrClassName != null) customAttrs['class-name'] = atrClassName
if (atrModule != null) customAttrs['module'] = atrModule

def customDefinition = {
    'custom-realm'(customAttrs) {
      if (!atrConfiguration.isEmpty()) {
       'configuration' {
             for ( item in atrConfiguration ) {
                'property'(['name': item.key, 'value': item.value])
                }
            }
      }
    }
}

def isExistingSecurityRealms = elytronSubsystem.'security-realms'.any { it.name() == 'security-realms' }
if (! isExistingSecurityRealms) {
    elytronSubsystem.appendNode { 'security-realms' customDefinition }
    return
}

def existingCustomElement = elytronSubsystem.'security-realms'.'custom-realm'.find { it.'@name' == atrName }
if (existingCustomElement && !atrReplaceExisting) {
    throw new IllegalStateException("CustomRealm with name $atrName already exists in configuration. Use different name.")
} else {
    if (existingCustomElement) {
        existingCustomElement.replaceNode customDefinition
    } else {
        elytronSubsystem.'security-realms'.appendNode customDefinition
    }
}
