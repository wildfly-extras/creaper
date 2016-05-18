def existingLogger = management.'audit-log'.'logger'.find { it.name() == 'logger' }

loggerAttrs = [:]

if (nn(atrEnabled)) {
    loggerAttrs ['enabled'] = atrEnabled
}
if (nn(atrLogBoot)) {
    loggerAttrs['log-boot'] = atrLogBoot
}
if (nn(atrLogReadOnly)) {
    loggerAttrs['log-read-only'] = atrLogReadOnly
}

def loggerDefinition = {
    'logger'(loggerAttrs)
}

if (existingLogger && !atrReplaceExisting) {
    throw new IllegalStateException("Logger already exists. Add replaceExisting attribute to overwrite current configuration")
} else {
    if (existingLogger) {
        existingLogger.replaceNode loggerDefinition
    } else {
        management.'audit-log'.appendNode loggerDefinition
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
