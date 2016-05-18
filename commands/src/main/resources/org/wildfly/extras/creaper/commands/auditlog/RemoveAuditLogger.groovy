def loggerToRemove = management.'audit-log'.'logger'

if (loggerToRemove) {
    loggerToRemove.replaceNode {}
} else {
    throw new IllegalStateException("Logger was not found => cannot be removed")
}
