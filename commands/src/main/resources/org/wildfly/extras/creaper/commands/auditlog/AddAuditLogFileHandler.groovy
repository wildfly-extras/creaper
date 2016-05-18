def handlerExists = management.'audit-log'.'handlers'.'file-handler'.find { it.'@name' == atrName }

handlerAttrs = ['name': atrName]
udpAttrs = [:]

if (nn(atrFormatter)) {
    handlerAttrs['formatter'] = atrFormatter
}
if (nn(atrMaxFailureCount)) {
    handlerAttrs['max-failure-count'] = atrMaxFailureCount
}
if (nn(atrPath)) {
    handlerAttrs['path'] = atrPath
}
if (nn(atrRelativeTo)) {
    handlerAttrs['relative-to'] = atrRelativeTo
}

def newHandlerDef = {
    'file-handler'(handlerAttrs)
}

if (!handlerExists) {
    management.'audit-log'.'handlers'.appendNode newHandlerDef
} else if (atrReplaceExisting) {
    handlerExists.replaceNode newHandlerDef
} else {
    throw new IllegalStateException("File handler $atrName already exists. If you want to replace existing handler, please set replaceExisting.")
}

/**
 * Checking if parameter is not null.
 * We can't use if(object) ... as object could be null or false
 * and we need to differentiate such states
 */
def nn(Object... object) {
    if (object == null) return false
    return object.any { it != null }
}
