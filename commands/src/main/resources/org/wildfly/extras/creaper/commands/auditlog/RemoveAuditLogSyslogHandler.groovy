def handlerToRemove = management.'audit-log'.'handlers'.'syslog-handler'.find { it.'@name' == atrSyslogHandlerName }

if (handlerToRemove) {
    handlerToRemove.replaceNode {}
} else {
    throw new IllegalStateException("Syslog handler $handlerToRemove not found => can't be removed")
}
