def handler = logging.'periodic-rotating-file-handler'.find { it.@name == name }

if (!handler) {
    throw new IllegalStateException(String.format("handler with name %s does not exists.", name))
}

fileNodeNeeded = filePath || fileRelativeTo
fileAttrs = [:]
if (nn(filePath)) fileAttrs['path'] = filePath
if (nn(fileRelativeTo)) fileAttrs['relative-to'] = fileRelativeTo

if (nn(autoflush)) {
    handler.@autoflush = autoflush
}

if (nn(enabled)) {
    handler.@enabled = enabled
}

if (nn(level)) {
    if (handler.level.size() > 0) {
        handler.level.@name = level
    } else {
        handler.appendNode {
            level(name: level)
        }
    }
}

if (nn(filter)) {
    if (handler.'filter-spec'.size() > 0) {
        handler.'filter-spec'.@value = filter
    } else {
        handler.appendNode {
            'filter-spec'(value: filter)
        }
    }
}

if (nn(encoding)) {
    if (handler.encoding.size() > 0) {
        handler.encoding.@value = encoding
    } else {
        handler.appendNode {
            'encoding'(value: encoding)
        }
    }
}

if (nn(namedFormatter)) {
    if (handler.'formatter'.size() > 0) {
        handler.formatter.replaceNode {
            'formatter' {
                'named-formatter'(name: namedFormatter)
            }
        }
    } else {
        handler.appendNode {
            'formatter' {
                'named-formatter'(name: namedFormatter)
            }
        }
    }
}

if (nn(patternFormatter)) {
    if (handler.'formatter'.size() > 0) {
        handler.formatter.replaceNode {
            'formatter' {
                'pattern-formatter'(pattern: patternFormatter)
            }
        }
    } else {
        handler.appendNode {
            'formatter' {
                'pattern-formatter'(pattern: patternFormatter)
            }
        }
    }
}

if (fileNodeNeeded) {
    if (handler.file.size() > 0) {
        if (nn(filePath)) handler.file.@path = filePath
        if (nn(fileRelativeTo)) handler.file.'@relative-to' = fileRelativeTo
    } else {
        handler.appendNode {
            'file'(fileAttrs)
        }
    }
}

if (nn(suffix)) {
    if (handler.suffix.size() > 0) {
        handler.suffix.@value = suffix
    } else {
        handler.appendNode {
            'suffix'(value: suffix)
        }
    }
}

if (nn(append)) {
    if (handler.append.size() > 0) {
        handler.append.@value = append
    } else {
        handler.appendNode {
            'append'(value: append)
        }
    }
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
