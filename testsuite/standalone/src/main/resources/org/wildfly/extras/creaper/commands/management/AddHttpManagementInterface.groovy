def socketBindingAttrs = [:]
if (atrSocketBinding != null) socketBindingAttrs['http'] = atrSocketBinding
if (atrSecureSocketBinding != null) socketBindingAttrs['https'] = atrSecureSocketBinding

def interfaceDefinition = {
    'http-interface'(['security-realm': atrSecurityRealm, 'http-upgrade-enabled': atrHttpUpgradeEnabled]) {
        'socket-binding'(socketBindingAttrs)
    }
}

def existingHttpInterface = management.'management-interfaces'.'http-interface'.find()
if (existingHttpInterface && !atrReplaceExisting) {
    throw new IllegalStateException("Management http-interface already exists in configuration.")
} else {
    if (existingHttpInterface) {
        existingHttpInterface.replaceNode interfaceDefinition
    } else {
        management.'management-interfaces'.appendNode interfaceDefinition
    }
}
