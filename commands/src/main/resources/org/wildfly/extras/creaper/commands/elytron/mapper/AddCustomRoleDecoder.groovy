customAttrs = ['name': atrName]
if (atrClassName != null) customAttrs['class-name'] = atrClassName
if (atrModule != null) customAttrs['module'] = atrModule

def customDefinition = {
    'custom-role-decoder'(customAttrs) {
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

def existingCustomRoleDecoder = elytronSubsystem.'mappers'.'custom-role-decoder'.find { it.'@name' == atrName }
if (existingCustomRoleDecoder && !atrReplaceExisting) {
    throw new IllegalStateException("CustomRoleDecoder with name $atrName already exists in configuration. Use different name.")
} else {
    if (existingCustomRoleDecoder) {
        existingCustomRoleDecoder.replaceNode customDefinition
    } else {
        elytronSubsystem.'mappers'.appendNode customDefinition
    }
}
