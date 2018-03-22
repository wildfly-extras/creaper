customAttrs = ['name': atrName]
if (atrClassName != null) customAttrs['class-name'] = atrClassName
if (atrModule != null) customAttrs['module'] = atrModule

def customDefinition = {
    'custom-permission-mapper'(customAttrs) {
      if (!atrConfiguration.isEmpty()) {
       'configuration' {
             for ( item in atrConfiguration ) {
                'property'(['name': item.key, 'value': item.value])
                }
            }
      }
    }
}

def isExistingMappers = elytronSubsystem.'mappers'.any { it.name() == 'mappers' }
if (! isExistingMappers) {
    elytronSubsystem.appendNode { 'mappers' customDefinition }
    return
}

def existingCustomElement = elytronSubsystem.'mappers'.'custom-permission-mapper'.find { it.'@name' == atrName }
if (existingCustomElement && !atrReplaceExisting) {
    throw new IllegalStateException("CustomPermissionMapper with name $atrName already exists in configuration. Use different name.")
} else {
    if (existingCustomElement) {
        existingCustomElement.replaceNode customDefinition
    } else {
        elytronSubsystem.'mappers'.appendNode customDefinition
    }
}
