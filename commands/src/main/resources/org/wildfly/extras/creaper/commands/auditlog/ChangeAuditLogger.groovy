def auditLogger = management.'audit-log'.'logger'.find { it.name() == 'logger' }

if (!auditLogger) {
    throw new IllegalStateException("Audit logger does not exists.")
}

if (nn(atrEnabled)) {
    auditLogger.@enabled = atrEnabled
}
if (nn(atrLogBoot)) {
    auditLogger.@'log-boot' = atrLogBoot
}
if (nn(atrLogReadOnly)) {
    auditLogger.@'log-read-only' = atrLogReadOnly
}

def newHandlersDefinition = {
    atrAddHandlers.each {
        handler(name: it)
    }
}

if (nn(atrAddHandlers)) {
    if (!atrAddHandlers.isEmpty()) {
        if (auditLogger.handlers.size() == 0) {
            auditLogger.handlers.replaceNode newHandlersDefinition
        } else {
            auditLogger.handlers.appendNode newHandlersDefinition
        }
    }
}

if (nn(atrRemoveHandlers)) {
    if (!atrRemoveHandlers.isEmpty()) {
        if (auditLogger.handlers.size() > 0) {
            atrRemoveHandlers.each { atrHandler ->
                def handlerToRemove = auditLogger.handlers.handler.find { it.'@name' == atrHandler }
                handlerToRemove.replaceNode {}
            }
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
