customAttrs = ['name': atrName]
if (atrClassName != null) customAttrs['class-name'] = atrClassName
if (atrModule != null) customAttrs['module'] = atrModule

def customDefinition = {
    'custom-role-mapper'(customAttrs) {
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

def existingCustomRoleMapper = elytronSubsystem.'mappers'.'custom-role-mapper'.find { it.'@name' == atrName }
if (existingCustomRoleMapper && !atrReplaceExisting) {
    throw new IllegalStateException("CustomRoleMapper with name $atrName already exists in configuration. Use different name.")
} else {
    if (existingCustomRoleMapper) {
        existingCustomRoleMapper.replaceNode customDefinition
    } else {
        elytronSubsystem.'mappers'.appendNode customDefinition
    }
}
