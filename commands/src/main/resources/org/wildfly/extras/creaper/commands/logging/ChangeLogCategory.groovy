def logger = logging.logger.find { it.@category == category }

if (!logger) {
    throw new IllegalStateException(String.format("Logger with category %s does not exists.", category))
}

def handlersDef = {
    'handlers' {
        handlers.each {
            handler(name: it)
        }
    }
}

if (nn(useParentHandler)) {
    logger.'@use-parent-handlers' = useParentHandler
}
if (nn(level)) {
    if (logger.level.size() > 0) {
        logger.level.@name = level
    } else {
        logger.appendNode {
            level (name: level)
        }
    }
}
if (nn(filter)) {
    if (logger.'filter-spec'.size() > 0) {
        logger.'filter-spec'.@value = filter
    } else {
        logger.appendNode {
            'filter-spec'(value: filter)
        }
    }
}
if (nn(handlers)) {
    if (handlers.isEmpty()) {
        logger.handlers.replaceNode {}
    } else {
        if(logger.handlers.size() > 0) {
            logger.handlers.replaceNode handlersDef
        } else {
            logger.appendNode handlersDef
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