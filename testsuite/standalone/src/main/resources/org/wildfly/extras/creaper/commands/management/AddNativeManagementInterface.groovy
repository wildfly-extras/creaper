def interfaceDefinition = {
    'native-interface'(['security-realm': atrSecurityRealm]) {
        'socket-binding'(['native': atrSocketBinding])
    }
}

def existingNativeInterface = management.'management-interfaces'.'native-interface'.find()
if (existingNativeInterface && !atrReplaceExisting) {
    throw new IllegalStateException("Management native-interface already exists in configuration.")
} else {
    if (existingNativeInterface) {
        existingNativeInterface.replaceNode interfaceDefinition
    } else {
        management.'management-interfaces'.appendNode interfaceDefinition
    }
}
