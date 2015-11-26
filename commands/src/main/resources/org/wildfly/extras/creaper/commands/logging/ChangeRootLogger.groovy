def handlersDef = {
    'handlers' {
        handlers.each {
            handler(name: it)
        }
    }
}

if (nn(level)) {
    if (logging.'root-logger'.level.size() > 0) {
        logging.'root-logger'.level.@name = level
    } else {
        logging.'root-logger'.appendNode {
            level (name: level)
        }
    }
}
if (nn(filter)) {
    if (logging.'root-logger'.'filter-spec'.size() > 0) {
        logging.'root-logger'.'filter-spec'.@value = filter
    } else {
        logging.'root-logger'.appendNode {
            'filter-spec'(value: filter)
        }
    }
}

if (nn(handlers)) {
    if (handlers.isEmpty()) {
        logging.'root-logger'.handlers.replaceNode {}
    } else {
        if(logging.'root-logger'.handlers.size() > 0) {
            logging.'root-logger'.handlers.replaceNode handlersDef
        } else {
            logging.'root-logger'.appendNode handlersDef
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
