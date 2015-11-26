def handlerExists = logging.'periodic-rotating-file-handler'.find { it.@name == name }


handlerAttrs = ['name': name]
if (nn(autoflush)) handlerAttrs['autoflush'] = autoflush
if (nn(enabled)) handlerAttrs['enabled'] = enabled

file = filePath || fileRelativeTo
fileAttrs = [:]
if (nn(filePath)) fileAttrs['path'] = filePath
if (nn(fileRelativeTo)) fileAttrs['relative-to'] = fileRelativeTo

def newHandlerDef = {
    'periodic-rotating-file-handler'(handlerAttrs) {
        if (nn(level)) {
            'level'(name: level)
        }
        if (nn(filter)) 'filter-spec'(value: filter)
        if (nn(encoding)) 'encoding'(value: encoding)
        if (nn(namedFormatter)){
            'formatter' {
                'named-formatter'(name: namedFormatter)
            }
        }
        if (nn(patternFormatter)){
            'formatter' {
                'pattern-formatter'(pattern: patternFormatter)
            }
        }
        if (nn(file)) 'file'(fileAttrs)
        if (nn(suffix)) 'suffix'(value: suffix)
        if (nn(append)) 'append'(value:append)
    }
}


if (!handlerExists) {
    logging.appendNode newHandlerDef
} else if (replaceExisting) {
    handlerExists.replaceNode newHandlerDef
} else {
    throw new IllegalStateException(String.format("periodic file handler with name %s already exists. If You want to add this handler, please set replaceExisting.", name))
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
