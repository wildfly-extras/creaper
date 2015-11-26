def loggerExist = logging.logger.find { it.@category == category }


loggerAttrs = ['category': category]
if (nn(useParentHandler)) loggerAttrs['use-parent-handlers'] = useParentHandler

def newLoggerDef = {
    logger(loggerAttrs) {
        if (nn(level)) {
            'level'(name: level)
        }
        if (nn(filter)) 'filter-spec'(value: filter)
        if (nn(handlers) && !handlers.isEmpty())
            'handlers' {
                handlers.each {
                    handler(name: it)
                }
        }
    }
}


if (!loggerExist) {
    logging.appendNode newLoggerDef
} else if (replaceExisting) {
    loggerExist.replaceNode newLoggerDef
} else {
   throw new IllegalStateException(String.format("Logger with category %s already exists. If You want to add this logger, please set replaceExisting.", category))
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
